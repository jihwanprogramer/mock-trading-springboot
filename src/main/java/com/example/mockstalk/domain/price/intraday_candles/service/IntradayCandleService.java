package com.example.mockstalk.domain.price.intraday_candles.service;

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

import com.example.mockstalk.domain.price.intraday_candles.entity.CandleType;
import com.example.mockstalk.domain.price.intraday_candles.entity.IntradayCandle;
import com.example.mockstalk.domain.price.intraday_candles.repository.IntradayCandleRepository;
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

	@Value("${hantu-openapi.appkey}")
	private String appKey;

	@Value("${hantu-openapi.appsecret}")
	private String appSecret;

	@Value("${hantu-openapi.accesstoken}")
	private String accessToken;

	@Value("${hantu-openapi.base-url}")
	private String baseUrl;

	@Value("#{'${hantu-openapi.stock-codes}'.split(',')}")
	private List<String> stockCodes;

	@Value("#{'${hantu-openapi.candle-intervals}'.split(',')}")
	private List<String> intervals;

	//스케줄링:기본 종목코드로 자동 실행
	@Scheduled(cron = "0 0 9,11,13 * * MON-FRI")
	public void fetchDefaultCandleJob() {
		for (String stockCode : stockCodes) {
			fetchAndSaveIntradayCandles(stockCode, null);
		}
	}

	// API 요청 -> 저장
	@Transactional
	public void fetchAndSaveIntradayCandles(String stockCode, Long userId) {

		System.out.println("🔍 현재 accessToken: " + accessToken);
		System.out.println("🔍 현재 appKey: " + appKey);
		System.out.println("🔍 현재 appSecret: " + appSecret);

		User user = null;
		if (userId != null) {
			user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("유저가 존재하지않습니다"));
		}
		for (String interval : intervals) {
			List<String> startTimes = getStartTimesByInterval(interval);

			for (String startTime : startTimes) {
				try {
					String url = baseUrl + "/uapi/domestic-stock/v1/quotations/inquire-time-itemchartprice"
						+ "?fid_cond_mrkt_div_code=J"
						+ "&fid_input_iscd=" + stockCode
						+ "&fid_period_div_code=M"
						+ "&fid_time_interval=" + interval
						+ "&fid_org_adj_prc=0"
						+ "&fid_input_hour_1=" + startTime;

					HttpHeaders headers = new HttpHeaders();
					headers.set("authorization", "Bearer " + accessToken);
					headers.set("appKey", appKey);
					headers.set("appSecret", appSecret);
					headers.set("tr_id", "FHKST03010100");

					System.out.println("🧾 최종 헤더: " + headers);

					HttpEntity<Void> entity = new HttpEntity<>(headers);
					ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity,
						JsonNode.class);

					System.out.println("🔍 요청 URL: " + url);
					System.out.println("🔍 한투 응답 전체: " + response.getBody());

					JsonNode candles = response.getBody().get("output2");
					for (JsonNode candle : candles) {
						try {
							IntradayCandle candleEntity = new IntradayCandle(
								null,
								user,
								stockCode,
								candle.get("stck_oprc").asLong(), // openingPrice
								candle.get("stck_clpr").asLong(), // closingPrice
								candle.get("stck_hgpr").asLong(), // highPrice
								candle.get("stck_lwpr").asLong(), // lowPrice
								candle.get("acml_vol").asLong(), // tradingVolume
								candle.get("acml_tr_pbmn").asLong(), // tradingValue
								candle.get("hts_avls").asLong(), // marketCap
								parseDateTime(
									candle.get("stck_bsop_date").asText(),
									candle.get("stck_bsop_hour").asText()
								),
								CandleType.MIN
							);
							intradayCandleRepository.save(candleEntity);
						} catch (Exception e) {
							System.err.println("\u274c 개별 캔들 저장 실패: " + e.getMessage());
						}
					}
				} catch (Exception e) {
					System.err.println("\u274c API 호출 실패: " + e.getMessage());
				}
			}
		}
	}

	private List<String> getStartTimesByInterval(String interval) {
		switch (interval) {
			case "1":
				return List.of("0900", "1100", "1300");
			case "3":
				return List.of("0900", "1130");
			case "5":
				return List.of("0900", "1200");
			default:
				return List.of("0900");
		}
	}

	private LocalDateTime parseDateTime(String date, String hour) {
		try {
			String combined = date + hour;
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
			return LocalDateTime.parse(combined, formatter);
		} catch (Exception e) {
			System.err.println("\u274c 시간 파싱 실패: " + e.getMessage());
			return LocalDateTime.now();
		}
	}

	// 조회용
	public List<IntradayCandle> getCandles(String stockCode, String date, int interval) {
		LocalDateTime start = LocalDateTime.parse(date + "0000", DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
		LocalDateTime end = LocalDateTime.parse(date + "2359", DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
		return intradayCandleRepository.findByStockCodeAndTimeStampBetween(stockCode, start, end);
	}
}
