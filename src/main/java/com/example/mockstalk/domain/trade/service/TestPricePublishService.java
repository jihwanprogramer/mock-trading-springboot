package com.example.mockstalk.domain.trade.service;

import java.math.BigDecimal;
import java.time.Duration;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.mockstalk.common.config.RabbitConfig;
import com.example.mockstalk.common.error.CustomRuntimeException;
import com.example.mockstalk.common.error.ExceptionCode;
import com.example.mockstalk.domain.stock.entity.Stock;
import com.example.mockstalk.domain.stock.repository.StockRepository;
import com.example.mockstalk.domain.trade.dto.StockPriceEventDto;

import lombok.RequiredArgsConstructor;

/**
 * Test-only price injection path (no Korea Investment WebSocket).
 * Mirrors the historical WS pipeline step: Redis stockPrice:: save → RabbitMQ publish,
 * so StockPriceListener → TradeService.onPriceUpdated can run without broker auth.
 */
@Service
@RequiredArgsConstructor
public class TestPricePublishService {

	static final String STOCK_PRICE_KEY_PREFIX = "stockPrice::";
	static final Duration PRICE_TTL = Duration.ofMinutes(5);

	private final StockRepository stockRepository;
	private final RedisTemplate<String, Object> redisTemplate;
	private final RabbitTemplate rabbitTemplate;

	public void publishTestPrice(Long stockId, BigDecimal currentPrice) {
		Stock stock = stockRepository.findById(stockId)
			.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.STOCK_NOT_FOUND));

		String stockCode = stock.getStockCode();
		redisTemplate.opsForValue()
			.set(STOCK_PRICE_KEY_PREFIX + stockCode, currentPrice.toPlainString(), PRICE_TTL);

		StockPriceEventDto event = new StockPriceEventDto(stockId, currentPrice);
		rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_NAME, RabbitConfig.ROUTING_KEY, event);
	}
}
