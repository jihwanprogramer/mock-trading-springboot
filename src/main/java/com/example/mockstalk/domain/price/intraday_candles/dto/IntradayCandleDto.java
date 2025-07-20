package com.example.mockstalk.domain.price.intraday_candles.dto;

import java.time.LocalDateTime;

import com.example.mockstalk.domain.price.intraday_candles.entity.CandleType;
import com.example.mockstalk.domain.price.intraday_candles.entity.IntradayCandle;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IntradayCandleDto {
	private String stockCode;
	private String stockName;
	private Long openingPrice;
	private Long closingPrice;
	private Long highPrice;
	private Long lowPrice;
	private Long tradingVolume;
	private Long tradingValue;
	private LocalDateTime timeStamp;
	private CandleType candleType;

	public static IntradayCandleDto fromEntity(IntradayCandle candle) {
		return new IntradayCandleDto(
			candle.getStock().getStockCode(),
			candle.getStock().getStockName(),
			candle.getOpeningPrice(),
			candle.getClosingPrice(),
			candle.getHighPrice(),
			candle.getLowPrice(),
			candle.getTradingVolume(),
			candle.getTradingValue(),
			candle.getTimeStamp(),
			candle.getCandleType()
		);
	}
}
