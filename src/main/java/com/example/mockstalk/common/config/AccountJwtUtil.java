package com.example.mockstalk.common.config;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "AccountJwtUtil")
@Component
public class AccountJwtUtil {

	private static final String BEARER_PREFIX = "Bearer ";
	private static final long ACCOUNT_TOKEN_TIME = 60 * 60 * 1000L; // 1시간

	@Value("${account.jwt.secret.key}")
	private String secretKeyPlain;

	private Key secretKey;
	private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

	@PostConstruct
	public void init() {
		byte[] keyBytes = Base64.getDecoder().decode(secretKeyPlain);
		this.secretKey = Keys.hmacShaKeyFor(keyBytes);
	}

	// 계좌 토큰 생성
	public String createAccountToken(Long accountId) {
		Date now = new Date();

		return BEARER_PREFIX +
			Jwts.builder()
				.setSubject("accountLogin")
				.claim("accountId", accountId)
				.claim("tokenType", "account")
				.setIssuedAt(now)
				.setExpiration(new Date(now.getTime() + ACCOUNT_TOKEN_TIME))
				.signWith(secretKey, signatureAlgorithm)
				.compact();
	}

	// "Bearer " 접두어 제거
	public String substringToken(String tokenValue) {
		if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {
			return tokenValue.substring(7);
		}
		throw new IllegalArgumentException("Not Found Token");
	}

	// 토큰에서 Claims 추출
	public Claims extractClaims(String token) {
		return Jwts.parserBuilder()
			.setSigningKey(secretKey)
			.build()
			.parseClaimsJws(token)
			.getBody();
	}

	// 계좌 ID 추출
	public Long extractAccountId(String token) {
		String substrToken = substringToken(token);
		return extractClaims(token).get("accountId", Long.class);
	}
}