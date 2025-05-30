package com.example.mockstalk.domain.account.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AccounResponseDto {
	private final String accountName;
	private final BigDecimal initialBalance;
	private final BigDecimal currentBalance;
	// 캐싱으로 구현 예정이므로 보류
	// private final BigDecimal profitRate;
}
