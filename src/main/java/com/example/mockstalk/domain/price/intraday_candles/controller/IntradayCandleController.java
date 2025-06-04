/*
package com.example.mockstalk.domain.price.intraday_candles.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.mockstalk.domain.price.intraday_candles.entity.IntradayCandle;
import com.example.mockstalk.domain.price.intraday_candles.service.IntradayCandleService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class IntradayCandleController {

	private final IntradayCandleService intradayCandleService;

	@GetMapping("/api/stocks/{stockCode}/candles")
	public List<IntradayCandle> getCandles(@PathVariable String stockCode, @RequestParam String date,
		@RequestParam(defaultValue = "1") int interval) {
		return intradayCandleService.getCandles(stockCode, date, interval);
	}
}
*/
