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
import com.example.mockstalk.domain.stock.repository.StockRepository;
import com.example.mockstalk.domain.user.entity.User;
import com.example.mockstalk.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IntradayCandleService {

	private final RestTemplate restTemplate;
	private final IntradayCandleRepository intradayCandleRepository;
	private final UserRepository userRepository;
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
		Long userId = 0L;

		for (String stockCode : stockCodes) {
			try {
				fetchAndSaveIntradayCandles(stockCode, userId);
				Thread.sleep(700); // 0.7초 지연
			} catch (Exception e) {
				System.err.println("❌ 실패: " + stockCode + " → " + e.getMessage());
			}
		}
	}

	// API 요청 -> 저장
	@Transactional
	public void fetchAndSaveIntradayCandles(String stockCode, Long userId) {

		User user = getUserById(userId);

		TokenResponseDto token = tokenService.getAccessToken();

		for (String interval : intervals) {
			List<String> startTimes = getStartTimesByInterval(interval);

			for (String startTime : startTimes) {
				System.out.println("📌 [API 요청 정보] 종목코드: " + stockCode + ", 인터벌: " + interval + ", 시작시간: " + startTime);

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
		return baseUrl + "/uapi/domestic-stock/v1/quotations/inquire-time-dailychartprice"
			+ "?FID_COND_MRKT_DIV_CODE=J"
			+ "&FID_INPUT_ISCD=" + stockCode
			+ "&FID_INPUT_DATE_1=" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"))
			+ "&FID_INPUT_HOUR_1=" + startTime
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

	private void handleApiResponse(JsonNode body) {
		if (body != null) {
			System.out.println("전체 API 응답: " + body.toString());
			System.out.println("✅ output1: " + body.get("output1"));
			System.out.println("✅ output2: " + body.get("output2"));
			System.out.println("✅ output: " + body.get("output"));
			System.out.println("✅ rt_cd: " + body.get("rt_cd"));
			System.out.println("✅ msg_cd: " + body.get("msg_cd"));
			System.out.println("✅ msg1: " + body.get("msg1"));
		}

	}

	private void saveCandlesIfNotExist(JsonNode body, String stockCode, User user) {
		JsonNode candles = (body != null) ? body.get("output2") : null;

		if (candles == null || !candles.isArray() || candles.size() == 0) {
			System.err.println("📭 캔들 데이터가 없거나 비어있습니다. stockCode: " + stockCode);
			return;
		}

		for (JsonNode candle : candles) {
			try {
				// get()을 호출하기 전에 null 체크를 해주는 것이 중요
				JsonNode stckBsopDateNode = candle.get("stck_bsop_date");
				JsonNode stckBsopHourNode = candle.get("stck_bsop_hour");

			/*	// 필드가 null인 경우 처리
				if (stckBsopDateNode == null || stckBsopHourNode == null) {
					System.err.println("📭 캔들 데이터의 날짜 또는 시간이 누락되었습니다. stockCode: " + stockCode);
					continue;
				}*/

				LocalDateTime candleTime = parseDateTime(stckBsopDateNode.asText(), stckBsopHourNode.asText());
				boolean exists = intradayCandleRepository.existsByStockCodeAndTimeStamp(stockCode, candleTime);

				if (exists) {
					System.out.println("⚠️ 이미 존재하는 캔들: " + stockCode + " - " + candleTime);
					continue;
				}

				IntradayCandle candleEntity = new IntradayCandle(
					null,
					user,
					stockCode,
					candle.get("stck_oprc").asLong(),
					candle.get("stck_clpr").asLong(),
					candle.get("stck_hgpr").asLong(),
					candle.get("stck_lwpr").asLong(),
					candle.get("acml_vol").asLong(),
					candle.get("acml_tr_pbmn").asLong(),
					candle.get("hts_avls").asLong(),
					candleTime,
					CandleType.MIN
				);
				intradayCandleRepository.save(candleEntity);
			} catch (Exception e) {
				System.err.println("개별 캔들 저장 실패: " + e.getMessage());
			}
		}
	}

	private LocalDateTime parseDateTime(String date, String hour) {
		try {
			return LocalDateTime.parse(date + hour, DateTimeFormatter.ofPattern("yyyyMMdd HHmm"));
		} catch (Exception e) {
			System.err.println("시간 파싱 실패: " + e.getMessage());
			return LocalDateTime.now();
		}
	}

	private User getUserById(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("유저가 존재하지않습니다"));
	}

	private List<String> getStartTimesByInterval(String interval) {
		if (!interval.equals("1")) {
			return List.of(); // 빈 리스트 반환
		}
		return List.of("0900", "1000", "1100", "1200", "1300");
	}

	// 조회용
	public List<IntradayCandle> getCandles(String stockCode, String date, int interval) {
		LocalDateTime start = LocalDateTime.parse(date + "0000", DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
		LocalDateTime end = LocalDateTime.parse(date + "2359", DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
		return intradayCandleRepository.findByStockCodeAndTimeStampBetween(stockCode, start, end);
	}
}
