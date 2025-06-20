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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
			System.out.println("캐싱 데이터 가져옴");
			return cached;
		}

		// 없으면 토큰 발급 요청
		try {
			System.out.println("토큰 발급 시작");
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

	public String getApprovalKey() {
		// Redis에 키 존재하는지 확인
		String cached = (String)redisTemplate.opsForValue().get("approvalKey::koreainvestment");
		if (cached != null) {
			System.out.println("캐싱 데이터 가져옴");
			return cached;
		}

		// 없으면 키 발급 요청
		try {
			System.out.println("키 발급 시작");
			RestTemplate restTemplate = new RestTemplate();

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			String requestBody = String.format(
				"{\"grant_type\":\"client_credentials\",\"appkey\":\"%s\",\"secretkey\":\"%s\"}",
				appKey, appSecret
			);

			HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

			ResponseEntity<String> response = restTemplate.exchange(
				apiDomain + "/oauth2/Approval",
				HttpMethod.POST,
				entity,
				String.class
			);

			if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
				ObjectMapper objectMapper = new ObjectMapper();
				JsonNode root = objectMapper.readTree(response.getBody());
				String approvalKey = root.path("approval_key").asText();

				// approval_key만 Redis에 저장
				redisTemplate.opsForValue().set("approvalKey::koreainvestment", approvalKey, Duration.ofHours(24));
				return approvalKey;
			}

			throw new CustomRuntimeException(ExceptionCode.HANTU_TOKEN_REQUIRED);
		} catch (Exception e) {
			log.error("키 발급 실패 - 사유: {}", e.getMessage(), e);
			throw new CustomRuntimeException(ExceptionCode.HANTU_TOKEN_REQUIRED);
		}
	}
}


