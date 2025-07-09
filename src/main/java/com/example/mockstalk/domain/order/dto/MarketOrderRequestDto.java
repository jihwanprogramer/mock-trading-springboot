package com.example.mockstalk.domain.order.dto;

import lombok.Getter;

@Getter
public class MarketOrderRequestDto {

	private Long stockId;
	private Long quantity;
}
