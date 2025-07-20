package com.example.mockstalk.domain.trade.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StockPriceEventDto {
	private Long stockId;
	private BigDecimal currentPrice;
}
