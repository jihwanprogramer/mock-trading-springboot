package com.example.mockstalk.domain.auth.jwt;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.mockstalk.domain.user.entity.UserRole;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "JwtUtil")
@Component
public class JwtUtil {
	private static final String BEARER_PREFIX = "Bearer ";
	private static final long TOKEN_TIME = 60 * 60 * 1000L; // 60분
	private static final long REFRESH_TOKEN_TIME = 7 * 24 * 60 * 60 * 1000L; // 리프레시 토큰 7일

	@Value("${jwt.secret.key}")
	private String secretKeyPlain;
	private Key secretKey;
	// 알고리즘 지정
	private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

	// 비밀키 초기화(Key 객체로 변환)
	@PostConstruct
	public void init() {

		byte[] keyBytes = Base64.getDecoder().decode(secretKeyPlain);
		this.secretKey = Keys.hmacShaKeyFor(keyBytes);
	}

	// Access Token 생성
	public String createToken(Long userId, String email, UserRole userRole) {
		Date date = new Date();
		return BEARER_PREFIX +
			Jwts.builder()
				.setSubject(String.valueOf(userId))
				.claim("tokenType", "access")
				.claim("email", email)
				.claim("userRole", userRole.name())
				.setExpiration(new Date(date.getTime() + TOKEN_TIME))
				.setIssuedAt(date) // 발급일
				.signWith(secretKey, signatureAlgorithm) // 암호화 알고리즘
				.compact();
	}

	// Refresh Token 생성
	public String createRefreshToken(Long userId) {
		Date date = new Date();

		return BEARER_PREFIX +
			Jwts.builder()
				.setSubject(String.valueOf(userId))
				.claim("tokenType", "refresh")
				.setExpiration(new Date(date.getTime() + REFRESH_TOKEN_TIME))
				.setIssuedAt(date)
				.signWith(secretKey, signatureAlgorithm)
				.compact();
	}

	// "Bearer" 접두어 제거
	public String substringToken(String token) {
		if (token != null && token.startsWith("Bearer ")) {
			return token.substring(7);
		}
		return token; // Bearer 없으면 그대로 반환
	}

	// 토큰에서 claims을 추출
	public Claims extractClaims(String token) {
		Claims body = Jwts.parserBuilder()
			.setSigningKey(secretKey)
			.build()
			.parseClaimsJws(token)
			.getBody();

		return body;
	}

	// 토큰 유효성 검사
	public boolean validateToken(String token) {
		try {
			if (token.startsWith("Bearer ")) {
				token = token.substring(7);
			}
			Jwts.parserBuilder()
				.setSigningKey(secretKey)
				.build()
				.parseClaimsJws(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			log.warn("JWT 유효성 검사 실패: {}", e.getMessage());
			return false;
		}
	}

	// 사용자 ID 추출
	public Long extractUserId(String token) {
		return Long.parseLong(extractClaims(token).getSubject());
	}

	// 닉네임 추출
	public String extractNickname(String token) {
		Claims claims = extractClaims(token);
		return claims.get("nickname", String.class);
	}

	// 이메일 추출
	public String extractEmail(String token) {
		return extractClaims(token).get("email", String.class);
	}

	// 권한(Role) 추출
	public UserRole extractRole(String token) {
		String roleName = extractClaims(token).get("user", String.class);
		return UserRole.valueOf(roleName);
	}

	// 토큰 만료시간 계산
	public long getRemainTime(String token) {
		Date expiration = extractClaims(token).getExpiration();
		return expiration.getTime() - System.currentTimeMillis();
	}

}
