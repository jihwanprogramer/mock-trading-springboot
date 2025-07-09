package com.example.mockstalk.domain.account.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AccountSignInRequestDto {
	private final Long accountId;
	private final String password;
}
