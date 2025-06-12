package com.example.mockstalk.domain.price.intraday_candles.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
import com.example.mockstalk.domain.price.intraday_candles.entity.CandleType;
import com.example.mockstalk.domain.price.intraday_candles.entity.IntradayCandle;
import com.example.mockstalk.domain.price.intraday_candles.repository.IntradayCandleRepository;
import com.example.mockstalk.domain.stock.entity.Stock;
import com.example.mockstalk.domain.stock.repository.StockRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IntradayCandleService {

	private final RestTemplate restTemplate;
	private final IntradayCandleRepository intradayCandleRepository;
	private final TokenService tokenService;
	private final StockRepository stockRepository;

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
				System.err.println(stockCode + "종목 처리 중 오류 발생: " + e.getMessage());
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
				System.out.println("종목코드: " + stockCode + ", 인터벌: " + interval + ", 시작시간: " + startTime);

				String url = buildApiUrl(stockCode, startTime);
				HttpHeaders headers = createHeaders(token);

				try {
					ResponseEntity<JsonNode> response = sendApiRequest(url, headers);

					JsonNode body = response.getBody();
					handleApiResponse(body);

					saveCandlesIfNotExist(body, stockCode, user);

				} catch (Exception e) {
					System.err.println("API 호출 실패: " + e.getMessage());
				}
			}
		}
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

	private ResponseEntity<JsonNode> sendApiRequest(String url, HttpHeaders headers) {
		HttpEntity<Void> entity = new HttpEntity<>(headers);
		return restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
	}

	private void saveCandlesIfNotExist(ResponseEntity<Map> response, String stockCode) {

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
			System.err.println("stock 엔티티를 찾을 수 없습니다: " + stockCode);
			return;
		}
		try {
			List<IntradayCandle> candleEntityList =
				dtoList.stream().map(dto -> dto.toEntity(stock)).toList();
			intradayCandleRepository.saveAll(candleEntityList);
		} catch (Exception e) {
			System.err.println("개별 캔들 저장 실패: " + e.getMessage());
		}

	}

	private List<String> getStartTimesByInterval(String interval) {
		switch (interval) {
			case "1":
				return List.of("0900", "1000", "1100", "1200", "1300");
			case "3":
				return List.of("0900", "1200");
			case "5":
				return List.of("0900");
			default:
				return List.of();
		}
	}

	// 조회용
	public List<IntradayCandle> getCandles(String stockCode, String date, int interval) {
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
			LocalDateTime start = LocalDateTime.parse(date + "0000", formatter);
			LocalDateTime end = LocalDateTime.parse(date + "2359", formatter);
			
			CandleType type = getCandleTypeByInterval(interval);

			System.out.println("🕐 조회 요청 → 종목: " + stockCode + ", 타입: " + type + ", 기간: " + start + " ~ " + end);

			// DB 조회
			return intradayCandleRepository.findByStock_StockCodeAndCandleTypeAndTimeStampBetween(
				stockCode, type, start, end
			);

		} catch (Exception e) {
			System.err.println("조회 실패: " + e.getMessage());
			return List.of();
		}
	}

	private CandleType getCandleTypeByInterval(int interval) {
		switch (interval) {
			case 1:
				return CandleType.MIN;
			case 3:
				return CandleType.MIN3;
			case 5:
				return CandleType.MIN5;
			default:
				throw new IllegalArgumentException("지원하지 않는 interval: " + interval);
		}
	}

}
