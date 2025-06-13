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
	private final BigDecimal initialBalance;  // 초기 설정 잔고
	private final BigDecimal currentBalance;  // 현재 잔고
	private final BigDecimal totalAsset;     // 총 자산
	private final BigDecimal profitRate;     // 수익률
	private final LocalDateTime createdAt;

	// 단건 조회용 of
	public static AccountResponseDto of(Account account, BigDecimal totalAsset, BigDecimal profitRate) {
		return AccountResponseDto.builder()
			.accountName(account.getAccountName())
			.initialBalance(account.getInitialBalance())
			.currentBalance(account.getCurrentBalance())
			.totalAsset(totalAsset)
			.profitRate(profitRate)
			.createdAt(account.getCreatedAt())
			.build();
	}

	// 다건 조회용 of
	public static AccountResponseDto of(Account account) {
		return AccountResponseDto.builder()
			.accountName(account.getAccountName())
			.initialBalance(account.getInitialBalance())
			.currentBalance(account.getCurrentBalance())
			.createdAt(account.getCreatedAt())
			.build();
	}
}
