package com.example.mockstalk.domain.account.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserProfitRankDto {
	private Long userId;
	private BigDecimal profitRate;
}