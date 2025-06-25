package com.example.mockstalk.common.jwttoken;

import com.example.mockstalk.common.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;

    // RefreshToken 저장
    public void storeRefreshToken(Long userId, String refreshToken) {
        String tokenToStore = jwtUtil.substringToken(refreshToken);

        redisTemplate.opsForValue().set("RT:" + userId, tokenToStore, Duration.ofDays(14));
    }

    // RefreshToken 조회
    public String getStoredRefreshToken(Long userId) {
        return redisTemplate.opsForValue().get("RT:" + userId);
    }

    // RefreshToken 삭제
    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete("RT:" + userId);
    }
    // AccessToken 블랙리스트에 등록
    public void blacklistAccessToken(String accessToken, long expirationMillis) {
        String cleanToken = jwtUtil.substringToken(accessToken);
        redisTemplate.opsForValue().set("BL:" + cleanToken, "true", Duration.ofMillis(expirationMillis));
    }
    // 블랙리스트 여부 확인
    public boolean isBlacklisted(String accessToken) {
        String cleanAccessToken = jwtUtil.substringToken(accessToken);
        return redisTemplate.hasKey("BL:" + cleanAccessToken);
    }

}
