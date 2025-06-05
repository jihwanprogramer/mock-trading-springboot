package com.example.mockstalk.common.hantutoken;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TokenResponseDto {
	private String access_token;
	private String token_type;
	private long expires_in;
	private String access_token_token_expired;
}
