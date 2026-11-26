package com.example.mockstalk.domain.trade.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * RabbitMQ payload for stock price updates (test path and production listener share this DTO).
 * No-arg constructor + setters required for Jackson2JsonMessageConverter deserialization.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockPriceEventDto {
	private Long stockId;
	private BigDecimal currentPrice;
}
