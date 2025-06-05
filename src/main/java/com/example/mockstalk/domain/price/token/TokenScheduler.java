package com.example.mockstalk.domain.price.token;

import java.time.Duration;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.example.mockstalk.common.error.CustomRuntimeException;
import com.example.mockstalk.common.error.ExceptionCode;

@Service
@Component
@RequiredArgsConstructor
public class TokenScheduler {

	@Value("${hantu-openapi.domain}")
	private String apiDomain;

	@Value("${hantu-openapi.appkey}")
	private String appKey;

	@Value("${hantu-openapi.appsecret}")
	private String appSecret;

	private final RedisTemplate<String, Object> redisTemplate;

	// @Scheduled(initialDelay = 0, fixedRate = 1000 * 60 * 60 * 24)
	// 나중에 프로젝트 정상 실행될때 스케줄드 적용해서 자동 토큰 생성으로 변경 예정
	public TokenResponseDto getAccessToken() {
		// Redis에 토큰 존재하는지 확인
		TokenResponseDto cached = (TokenResponseDto)redisTemplate.opsForValue().get("accessToken::koreainvestment");
		if (cached != null) {
			return cached;
		}

		// 없으면 토큰 발급 요청
		try {
			RestTemplate restTemplate = new RestTemplate();

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			String requestBody = String.format(
				"{\"grant_type\":\"client_credentials\",\"appkey\":\"%s\",\"appsecret\":\"%s\"}",
				appKey, appSecret
			);

			HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

			ResponseEntity<TokenResponseDto> response = restTemplate.exchange(
				apiDomain + "/oauth2/tokenP",
				HttpMethod.POST,
				entity,
				TokenResponseDto.class
			);

			if (response.getStatusCode() == HttpStatus.OK) {
				TokenResponseDto newToken = response.getBody();

				// Redis에 저장 (24시간 TTL)
				if (newToken != null) {
					redisTemplate.opsForValue().set("accessToken::koreainvestment", newToken, Duration.ofHours(24));
					return newToken;
				}
			}

			throw new CustomRuntimeException(ExceptionCode.HANTU_TOKEN_REQUIRED);
		} catch (Exception e) {
			throw new CustomRuntimeException(ExceptionCode.HANTU_TOKEN_REQUIRED);
		}
	}
}


