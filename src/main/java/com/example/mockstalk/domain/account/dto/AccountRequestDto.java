package com.example.mockstalk.domain.account.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AccountRequestDto {
	private final String accountName;
	private final String password;
	private final BigDecimal initialBalance;
}
