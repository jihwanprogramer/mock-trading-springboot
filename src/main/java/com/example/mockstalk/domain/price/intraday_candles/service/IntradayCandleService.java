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

import jakarta.transaction.Transactional;
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

	@Value("${hantu-openapi.base-url}")
	private String baseUrl;

	@Value("#{'${hantu-openapi.stock-codes}'.split(',')}")
	private List<String> stockCodes;

	@Value("${hantu-openapi.candle-interval}")
	private String interval;

	@Value("${hantu-openapi.intervals}")
	private List<String> requestTimes;

	// 스케줄링: 기본 종목코드로 자동 실행
	@Scheduled(cron = "0 0 9,11,13 * * MON-FRI")
	public void fetchDefaultCandleJob() {
		for (String stockCode : stockCodes) {
			fetchAndSaveIntradayCandles(stockCode);
		}
	}

	// API 요청 -> 저장
	@Transactional
	public void fetchAndSaveIntradayCandles(String stockCode) {
		for (String startTime : requestTimes) {
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
							stockCode,
							candle.get("stck_oprc").asLong(), //openingPrice: 시가
							candle.get("stck_clpr").asLong(), //closingPrice: 종가
							candle.get("stck_hgpr").asLong(), //highPrice: 고가
							candle.get("stck_lwpr").asLong(), //lowPrice: 저가
							candle.get("acml_vol").asLong(), //tradingVolume: 누적 거래량
							candle.get("acml_tr_pbmn").asLong(), //tradingValue: 누적 거래대금
							candle.get("hts_avls").asLong(), //marketCap :HTS 체결강도
							parseDateTime(
								candle.get("stck_bsop_date").asText(), // 날짜
								candle.get("stck_bsop_hour").asText() //시간
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

	private LocalDateTime parseDateTime(String date, String hour) {
		try {
			String combined = date + hour;
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
			return LocalDateTime.parse(combined, formatter);
		} catch (Exception e) {
			System.err.println("시간 파싱 실패: " + e.getMessage());
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
