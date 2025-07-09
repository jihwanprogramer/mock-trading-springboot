package com.example.mockstalk.domain.account.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UpdateAccountRequestDto {
	private final String password;
}
