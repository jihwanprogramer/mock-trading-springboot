package com.example.mockstalk.domain.stock.dto;

import com.example.mockstalk.domain.stock.entity.Stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockResponseDto {
	private String name;
	private String code;

	public static StockResponseDto from(Stock stock) {
		return StockResponseDto.builder()
			.name(stock.getStockName())
			.code(stock.getStockCode())
			.build();
	}
}