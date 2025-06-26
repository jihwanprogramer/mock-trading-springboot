package com.example.mockstalk.domain.interest_stock.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InterestRequestDto {

	private String stockName;

	private String stockCode;
}
