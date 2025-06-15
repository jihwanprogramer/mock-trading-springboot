package com.example.mockstalk.domain.price.intraday_candles.websocket;

import java.time.LocalDateTime;

//Tick데이터: 체결가와시각
public record Tick(
	long price,
	long openingPrice,
	long highPrice,
	long lowPrice,
	long volume,
	long tradingValue,
	LocalDateTime timestamp
) {
}

