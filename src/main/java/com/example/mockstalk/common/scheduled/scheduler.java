package com.example.mockstalk.common.scheduled;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.mockstalk.common.hantutoken.TokenService;
import com.example.mockstalk.common.websocket.WebSocketClientManager;
import com.example.mockstalk.domain.price.livePrice.service.LivePriceService;
import com.example.mockstalk.domain.stock.repository.StockRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class scheduler {
	private final LivePriceService livePriceService;

	private final StockRepository stockRepository;
	private final RedisTemplate<String, Object> redisTemplate;
	private final TokenService tokenService;
	private final Object tokenLock = new Object();
	private final WebSocketClientManager webSocketClientManager;

	private final RedisTemplate<String, String> redisTemplates;

	@PostConstruct
	public void init() throws Exception {
		System.out.println("프로젝트 시작 시 실행");
		tokenService.getAccessToken(); // 시작 시 1회 실행
		tokenService.getApprovalKey(); // 시작 시 1회 실행
		// livePriceService.cacheAllStockPrices();
		webSocketClientManager.connect();
		System.out.println("실행 완료");
	}

	// 실시간 통신으로 체결가 불러옴 프로젝트 시작시 현재가 한번 불러오는거로 변경
	// @Scheduled(cron = "0 */5 * * * *") // 매 5분마다
	// public void updateStockPrices() {
	// 	synchronized (tokenLock) {
	// 		livePriceService.cacheAllStockPrices();
	// 	}
	// }

	@Scheduled(fixedRate = 1000 * 60 * 60 * 24) // 24시간마다 강제로 발급
	public void refreshToken() {
		synchronized (tokenLock) {
			Boolean exists = redisTemplate.hasKey("accessToken::koreainvestment");
			if (Boolean.TRUE.equals(exists))
				return;

			redisTemplate.delete("accessToken::koreainvestment");
			tokenService.getAccessToken();
		}
	}
}
