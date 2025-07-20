package com.example.mockstalk.domain.order.cache;

import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.example.mockstalk.common.error.CustomRuntimeException;
import com.example.mockstalk.common.error.ExceptionCode;
import com.example.mockstalk.domain.order.entity.Order;
import com.example.mockstalk.domain.order.entity.OrderCacheDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CompleteOrderRedisCache {

	private final RedisTemplate<String, Object> redisTemplate;
	private static final String PREFIX = "completeOrders:";

	public void add(Order order) {
		String key = getKey(order.getStock().getId());
		redisTemplate.opsForList()
			.rightPush(key, OrderCacheDto.from(order));
	}

	public List<OrderCacheDto> getOrders(Long stockId) {
		String key = getKey(stockId);
		List<Object> raw = redisTemplate.opsForList().range(key, 0, -1);
		if (raw == null || raw.isEmpty())
			return List.of();

		try {
			return raw.stream()
				.map(order -> (OrderCacheDto)order)
				.toList();
		} catch (ClassCastException e) {
			throw new CustomRuntimeException(ExceptionCode.REDIS_CACHE_CORRUPTED);
		}
	}

	public void remove(OrderCacheDto dto) {
		String key = getKey(dto.getStockId());
		redisTemplate.opsForList()
			.remove(key, 1, dto);
	}

	private String getKey(Long stockId) {
		return PREFIX + stockId;
	}
}

