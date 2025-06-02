package com.example.mockstalk.domain.order.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderListResponseDto {

	private List<LimitOrderResponseDto> limitList;
	private List<MarketOrderResponseDto> marketList;

}
