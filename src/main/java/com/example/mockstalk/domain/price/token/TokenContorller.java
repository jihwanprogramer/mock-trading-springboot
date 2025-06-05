package com.example.mockstalk.domain.price.token;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/token")
@RequiredArgsConstructor
public class TokenContorller {
	private final TokenScheduler tokenScheduler;

	@PostMapping
	public ResponseEntity<TokenResponseDto> getToken() {
		TokenResponseDto tokenResponse = tokenScheduler.getAccessToken();
		return ResponseEntity.ok(tokenResponse);
	}
}
