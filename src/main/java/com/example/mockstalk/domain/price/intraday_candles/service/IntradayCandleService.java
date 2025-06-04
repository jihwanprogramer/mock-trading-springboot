/*
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
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IntradayCandleService {

	private final RestTemplate restTemplate;
	private final IntradayCandleRepository intradayCandleRepository;

	@Value("${hantu-openapi.appkey}")
	private String appKey;

	@Value("${hantu-openapi.appsecret}")
	private String appSecret;

	@Value("${hantu-openapi.accesstoken}")
	private String accessToken;

	@Scheduled(cron = "0 0 9,11,13 * * MON-FRI")
	public void fetchAndSaveIntradayCandles() {
		String stockCode = "005930"; // 삼성전자
		String interval = "1"; // 1분봉

		for (int i = 0; i < 3; i++) {
			try {
				String startTime = getStartTime(i);
				String url =
					"https://openapivts.koreainvestment.com:29443/uapi/domestic-stock/v1/quotations/inquire-time-itemchartprice"
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
				headers.set("tr_id", "VTTCY03010100");

				HttpEntity<Void> entity = new HttpEntity<>(headers);
				ResponseEntity<JsonNode> response = restTemplate.exchange(
					url, HttpMethod.GET, entity, JsonNode.class
				);

				JsonNode candles = response.getBody().get("output2");
				for (JsonNode candle : candles) {
					try {
						IntradayCandle candleEntity = new IntradayCandle(
							null,
							candle.get("stck_oprc").asLong(),
							candle.get("stck_clpr").asLong(),
							candle.get("stck_hgpr").asLong(),
							candle.get("stck_lwpr").asLong(),
							candle.get("acml_vol").asLong(),
							candle.get("acml_tr_pbmn").asLong(),
							candle.get("hts_avls").asLong(),
							parseDateTime(
								candle.get("stck_bsop_date").asText(),
								candle.get("stck_bsop_hour").asText()
							),
							CandleType.MIN
						);
						intradayCandleRepository.save(candleEntity);
					} catch (Exception e) {
						System.err.println("개별 캔들 저장 실패: " + e.getMessage());
					}
				}
			} catch (Exception e) {
				System.err.println("API 호출 실패: " + e.getMessage());
			}
		}
	}

	private String getStartTime(int idx) {
		return switch (idx) {
			case 0 -> "0900";
			case 1 -> "1100";
			case 2 -> "1300";
			default -> "0900";
		};
	}

	private LocalDateTime parseDateTime(String date, String hour) {
		try {
			String combined = date + hour; // "202506041300"
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
			return LocalDateTime.parse(combined, formatter);
		} catch (Exception e) {
			System.err.println("시간 파싱 실패: " + e.getMessage());
			return LocalDateTime.now();
		}
	}

	public List<IntradayCandle> getCandles(String stockCode, String date, int interval) {
		LocalDateTime start = LocalDateTime.parse(date + "0000", DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
		LocalDateTime end = LocalDateTime.parse(date + "2359", DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
		return intradayCandleRepository.findByStockCodeAndTimeStampBetween(stockCode, start, end);
	}
}
*/
