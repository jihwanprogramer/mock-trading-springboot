package com.example.mockstalk.domain.price.intraday_candles.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.mockstalk.common.hantutoken.TokenResponseDto;
import com.example.mockstalk.common.hantutoken.TokenService;
import com.example.mockstalk.domain.price.intraday_candles.dto.IntradayCandleApiResponseDto;
import com.example.mockstalk.domain.price.intraday_candles.entity.CandleType;
import com.example.mockstalk.domain.price.intraday_candles.entity.IntradayCandle;
import com.example.mockstalk.domain.price.intraday_candles.repository.IntradayCandleRepository;
import com.example.mockstalk.domain.stock.entity.Stock;
import com.example.mockstalk.domain.stock.repository.StockRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntradayCandleService {

	private final RestTemplate restTemplate;
	private final IntradayCandleRepository intradayCandleRepository;
	private final TokenService tokenService;
	private final StockRepository stockRepository;
	private final Queue<String> errorQueue = new ConcurrentLinkedQueue<>();

	@Value("${hantu-openapi.appkey}")
	private String appKey;

	@Value("${hantu-openapi.appsecret}")
	private String appSecret;

	@Value("${hantu-openapi.domain}")
	private String baseUrl;

	@Value("#{'${hantu-openapi.candle-intervals}'.split(',')}")
	private List<String> intervals;

	// 스케줄링: 기본 종목코드로 자동 실행
	@Scheduled(cron = "0 0 9,11,13 * * MON-FRI")
	public void fetchDefaultCandleJob() {
		List<String> stockCodes = stockRepository.findAllStockCodes();
		for (String stockCode : stockCodes) {
			try {
				fetchAndSaveIntradayCandles(stockCode);  // 각각의 종목코드에 대해 분봉 데이터 수집
			} catch (Exception e) {
				log.error("{} 종목 처리 중 오류 발생: {}", stockCode, e.getMessage());
				errorQueue.add(stockCode);
			}
		}
	}

	@Scheduled(cron = "0 30 9,11,13 * * MON-FRI")
	public void retryFaildStocks() {
		while (!errorQueue.isEmpty()) {
			String failedStockCode = errorQueue.poll();
			try {
				fetchAndSaveIntradayCandles(failedStockCode);
				log.info("재시도 성공: {}", failedStockCode);
			} catch (Exception e) {
				log.error("재시도 실패: {} - {}", failedStockCode, e.getMessage());
			}
		}
	}

	// API 요청 -> 저장
	@Transactional
	public void fetchAndSaveIntradayCandles(String stockCode) {

		TokenResponseDto token = tokenService.getAccessToken();

		for (String interval : intervals) {
			List<String> startTimes = getStartTimesByInterval(interval);

			for (String startTime : startTimes) {
				log.info("종목코드: {}, 인터벌: {}, 시작시간: {}", stockCode, interval, startTime);

				String url = buildApiUrl(stockCode, startTime);
				HttpHeaders headers = createHeaders(token);

				try {
					ResponseEntity<Map> response = sendApiRequest(url, headers);

					saveOrUpdateCandles(response, stockCode);

				} catch (Exception e) {
					log.error("API 호출 실패: {}-{}", stockCode, e.getMessage());
				}
			}
		}
	}

	private void saveOrUpdateCandles(ResponseEntity<Map> response, String stockCode) {

		List<Map<String, Object>> output2 = (List<Map<String, Object>>)response.getBody().get("output2");

		List<IntradayCandleApiResponseDto> dtoList = output2.stream()
			.map(data -> new IntradayCandleApiResponseDto(
				(String)data.get("stck_bsop_date"),
				(String)data.get("stck_cntg_hour"),
				String.valueOf(data.get("stck_prpr")),
				String.valueOf(data.get("stck_oprc")),
				String.valueOf(data.get("stck_hgpr")),
				String.valueOf(data.get("stck_lwpr")),
				String.valueOf(data.get("cntg_vol")),
				String.valueOf(data.get("acml_tr_pbmn"))
			))
			.toList();

		Stock stock = stockRepository.findByStockCode(stockCode);
		if (stock == null) {
			log.error("stock 엔티티를 찾을 수 없습니다: {} ", stockCode);
			return;
		}

		List<IntradayCandle> newCandles = new ArrayList<>();
		for (IntradayCandleApiResponseDto dto : dtoList) {
			LocalDateTime timestamp = LocalDateTime.parse(
				dto.getStck_bsop_date() + dto.getStck_cntg_hour(),
				DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
			);

			Optional<IntradayCandle> existing = intradayCandleRepository
				.findByStock_StockCodeAndTimeStamp(stockCode, timestamp);

			IntradayCandle entity = dto.toEntity(stock);

			if (existing.isPresent()) {
				IntradayCandle candle = existing.get();
				candle.updateFrom(entity);
			} else {
				newCandles.add(entity);
			}
		}
		intradayCandleRepository.saveAll(newCandles);
	}

	private String buildApiUrl(String stockCode, String startTime) {
		String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String hour = startTime + "00";

		return baseUrl + "/uapi/domestic-stock/v1/quotations/inquire-time-dailychartprice"
			+ "?FID_COND_MRKT_DIV_CODE=J"
			+ "&FID_INPUT_ISCD=" + stockCode
			+ "&FID_INPUT_DATE_1=" + today
			+ "&FID_INPUT_HOUR_1=" + hour
			+ "&FID_PW_DATA_INCU_YN=Y"
			+ "&FID_FAKE_TICK_INCU_YN=N";
	}

	private HttpHeaders createHeaders(TokenResponseDto token) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("authorization", "Bearer " + token.getAccess_token());
		headers.set("appKey", appKey);
		headers.set("appSecret", appSecret);
		headers.set("tr_id", "FHKST03010230");
		return headers;
	}

	private ResponseEntity<Map> sendApiRequest(String url, HttpHeaders headers) {
		HttpEntity<Void> entity = new HttpEntity<>(headers);
		return restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
	}

	private List<String> getStartTimesByInterval(String interval) {
		switch (interval) {
			case "1":
				return List.of("0900", "1000", "1100", "1200", "1300");
			case "3":
				return List.of("0900", "0930", "1000", "1030", "1100", "1130", "1200", "1230", "1300");
			case "5":
				return List.of("0900", "0930", "1000", "1030", "1100", "1130", "1200", "1230", "1300");
			default:
				return List.of();
		}
	}

	private CandleType getCandleTypeByInterval(int interval) {
		return switch (interval) {
			case 1 -> CandleType.MIN;
			case 3 -> CandleType.MIN3;
			case 5 -> CandleType.MIN5;
			default -> throw new IllegalArgumentException("지원하지 않는 interval: " + interval);
		};
	}

	public List<IntradayCandle> getCandlesByName(String stockName, String date, int interval) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
		LocalDateTime start = LocalDateTime.parse(date + "0000", formatter);
		LocalDateTime end = LocalDateTime.parse(date + "2359", formatter);

		CandleType type = getCandleTypeByInterval(interval);

		return intradayCandleRepository.findByStock_StockNameAndCandleTypeAndTimeStampBetween(
			stockName, type, start, end
		);
	}

	private List<IntradayCandle> aggregateCandles(List<IntradayCandle> sourceCandles, int interval,
		CandleType targetType) {
		List<IntradayCandle> result = new ArrayList<>();

		for (int i = 0; i < sourceCandles.size(); i += interval) {
			List<IntradayCandle> group = sourceCandles.subList(i, Math.min(i + interval, sourceCandles.size()));

			IntradayCandle first = group.get(0);
			IntradayCandle last = group.get(group.size() - 1);

			IntradayCandle aggregated = new IntradayCandle();
			aggregated.setStock(first.getStock());
			aggregated.setTimeStamp(first.getTimeStamp());
			aggregated.setCandleType(targetType);

			aggregated.setOpeningPrice(first.getOpeningPrice());
			aggregated.setClosingPrice(last.getClosingPrice());
			aggregated.setHighPrice(
				group.stream().map(IntradayCandle::getHighPrice).max(Long::compareTo).orElse(first.getHighPrice()));
			aggregated.setLowPrice(
				group.stream().map(IntradayCandle::getLowPrice).min(Long::compareTo).orElse(first.getLowPrice()));

			aggregated.setTradingVolume(group.stream().mapToLong(IntradayCandle::getTradingVolume).sum());
			aggregated.setTradingValue(group.stream().mapToLong(IntradayCandle::getTradingValue).sum());

			aggregated.setStockCode(first.getStockCode());
			aggregated.setStockName(first.getStockName());

			result.add(aggregated);
		}

		return result;
	}

	@Transactional
	public void generateAndSaveMultiIntervalCandlesByCode(String stockCode, String date) {
		Stock stock = stockRepository.findByStockCode(stockCode);
		if (stock == null) {
			throw new IllegalArgumentException("해당 코드의 종목을 찾을 수 없습니다: " + stockCode);
		}

		CandleType baseType = CandleType.MIN;

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
		LocalDateTime start = LocalDateTime.parse(date + "0000", formatter);
		LocalDateTime end = LocalDateTime.parse(date + "2359", formatter);

		List<IntradayCandle> oneMinCandles =
			intradayCandleRepository.findByStock_StockCodeAndCandleTypeAndTimeStampBetween(
				stockCode, baseType, start, end
			);

		List<IntradayCandle> candles3m = aggregateCandles(oneMinCandles, 3, CandleType.MIN3);
		intradayCandleRepository.saveAll(candles3m);

		List<IntradayCandle> candles5m = aggregateCandles(oneMinCandles, 5, CandleType.MIN5);
		intradayCandleRepository.saveAll(candles5m);
	}
}
