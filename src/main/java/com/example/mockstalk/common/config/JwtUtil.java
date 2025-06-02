package com.example.mockstalk.common.config;

import com.example.mockstalk.domain.user.entity.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

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
                        .claim("email", email)
                        .claim("userRole", userRole)
                        .claim("auth", userRole.name())
                        .claim("tokenType","access")
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
                        .claim("tokenType","refresh")
                        .setExpiration(new Date(date.getTime() + REFRESH_TOKEN_TIME))
                        .setIssuedAt(date)
                        .signWith(secretKey, signatureAlgorithm)
                        .compact();
    }

    // "Bearer" 접두어 제거
    public String substringToken(String tokenValue) {
        if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {
            return tokenValue.substring(7);
        }
        throw new IllegalArgumentException("Not Found Token");
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
}
