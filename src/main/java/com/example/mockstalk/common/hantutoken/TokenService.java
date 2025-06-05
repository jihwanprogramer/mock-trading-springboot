package com.example.mockstalk.common.hantutoken;

import java.time.Duration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class TokenService {

	@Value("${hantu-openapi.domain}")
	private String apiDomain;

	@Value("${hantu-openapi.appkey}")
	private String appKey;

	@Value("${hantu-openapi.appsecret}")
	private String appSecret;

	private final RedisTemplate<String, Object> redisTemplate;

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
			log.error("토큰 발급 실패 - 사유: {}", e.getMessage(), e);
			throw new CustomRuntimeException(ExceptionCode.HANTU_TOKEN_REQUIRED);
		}
	}
}


