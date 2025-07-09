package com.example.mockstalk.domain.interest_stock.dto.response;

import lombok.Getter;

@Getter
public class InterestResponseDto {

	private Long id;
	private String stockName;

	private String stockCode;

	public InterestResponseDto(Long id, String stockName, String stockCode) {
		this.id = id;
		this.stockName = stockName;
		this.stockCode = stockCode;
	}
}
