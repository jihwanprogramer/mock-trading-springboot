package com.example.mockstalk.domain.order.dto;

import java.math.BigDecimal;

import lombok.Getter;

@Getter
public class LimitOrderRequestDto {
	private Long stockId;
	private Long quantity;
	private BigDecimal limitPrice;
}
