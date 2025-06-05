package com.example.mockstalk.common.hantutoken;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/token")
@RequiredArgsConstructor
public class TokenContorller {
	private final TokenService tokenService;

	@PostMapping
	public ResponseEntity<TokenResponseDto> getToken() {
		TokenResponseDto tokenResponse = tokenService.getAccessToken();
		return ResponseEntity.ok(tokenResponse);
	}
}
