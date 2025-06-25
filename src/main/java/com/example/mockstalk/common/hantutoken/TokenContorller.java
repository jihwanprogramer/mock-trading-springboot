package com.example.mockstalk.common.hantutoken;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TokenContorller {
	private final TokenService tokenService;

	@PostMapping("/token")
	public ResponseEntity<TokenResponseDto> getToken() {
		TokenResponseDto tokenResponse = tokenService.getAccessToken();
		return ResponseEntity.ok(tokenResponse);
	}

	@PostMapping("/key")
	public ResponseEntity<String> getKey() {
		String response = tokenService.getApprovalKey();
		return ResponseEntity.ok(response);
	}
}
