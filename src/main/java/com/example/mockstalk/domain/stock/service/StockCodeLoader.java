package com.example.mockstalk.domain.stock.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.example.mockstalk.domain.stock.repository.StockRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StockCodeLoader {

	private final StockRepository stockRepository;
	private final RedisTemplate<String, Object> redisTemplate;

	@PostConstruct
	public void cacheStockCodeToId() {
		stockRepository.findAll().forEach(stock -> {
			redisTemplate.opsForValue().set("stockCode:" + stock.getStockCode(), stock.getId());
		});
	}
}
