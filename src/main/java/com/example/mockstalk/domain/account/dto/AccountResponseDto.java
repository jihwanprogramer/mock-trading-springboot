package com.example.mockstalk.domain.account.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.mockstalk.domain.account.entity.Account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class AccountResponseDto {
	private final String accountName;
	private final BigDecimal initialBalance;
	private final BigDecimal currentBalance;
	private final LocalDateTime createdAt;
	// 캐싱으로 구현 예정이므로 보류
	// private final BigDecimal profitRate;

	public static AccountResponseDto of(Account account) {
		return AccountResponseDto.builder()
			.accountName(account.getAccountName())
			.initialBalance(account.getInitialBalance())
			.currentBalance(account.getCurrentBalance())
			.createdAt(account.getCreatedAt())
			.build();
	}

}
