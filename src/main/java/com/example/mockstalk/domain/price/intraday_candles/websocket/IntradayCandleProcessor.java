package com.example.mockstalk.domain.price.intraday_candles.websocket;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class IntradayCandleProcessor {

	private final CandleBuffer candleBuffer;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public void process(String message) {
		try {
			JsonNode root = objectMapper.readTree(message);
			JsonNode body = root.path("body");

			String stockCode = body.path("stck_shrn_iscd").asText();
			String date = body.path("stck_bsop_date").asText();
			String time = body.path("stck_cntg_hour").asText();

			LocalDateTime timestamp = LocalDateTime.parse(date + time, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

			long price = body.path("stck_prpr").asLong();
			long opening = body.path("stck_oprc").asLong();
			long high = body.path("stck_hgpr").asLong();
			long low = body.path("stck_lwpr").asLong();
			long vol = body.path("cntg_vol").asLong();
			long value = body.path("acml_tr_pbmn").asLong();

			Tick tick = new Tick(price, opening, high, low, vol, value, timestamp);
			candleBuffer.addTick(stockCode, tick);

		} catch (Exception e) {
			System.out.println("Tick 파싱 오류: " + e.getMessage());
		}
	}
}
