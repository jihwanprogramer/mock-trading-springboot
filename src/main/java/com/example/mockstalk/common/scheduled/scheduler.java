package com.example.mockstalk.common.scheduled;

import jakarta.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.example.mockstalk.common.hantutoken.TokenService;
import com.example.mockstalk.domain.price.livePrice.service.LivePriceService;

@Component
@RequiredArgsConstructor
public class scheduler {
	private final LivePriceService livePriceService;

	private final RedisTemplate<String, Object> redisTemplate;
	private final TokenService tokenService;

	@PostConstruct
	public void init() {
		livePriceService.cacheAllStockPrices();
		tokenService.getAccessToken(); // 시작 시 1회 실행
	}

	@Scheduled(cron = "0 */5 * * * *") // 매 5분마다
	public void updateStockPrices() {
		livePriceService.cacheAllStockPrices();
	}

	@Scheduled(fixedRate = 1000 * 60 * 60 * 24) // 24시간마다 강제로 발급
	public void refreshToken() {
		// 강제로 재발급 → 기존 캐시 삭제
		redisTemplate.delete("accessToken::koreainvestment");
		tokenService.getAccessToken(); // 다시 발급 + 캐싱
	}
}
