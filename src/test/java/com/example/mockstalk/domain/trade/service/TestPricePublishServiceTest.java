package com.example.mockstalk.domain.trade.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.example.mockstalk.common.config.RabbitConfig;
import com.example.mockstalk.common.error.CustomRuntimeException;
import com.example.mockstalk.common.error.ExceptionCode;
import com.example.mockstalk.domain.stock.entity.Stock;
import com.example.mockstalk.domain.stock.repository.StockRepository;
import com.example.mockstalk.domain.trade.dto.StockPriceEventDto;

@ExtendWith(MockitoExtension.class)
class TestPricePublishServiceTest {

	@Mock
	private StockRepository stockRepository;

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	@Mock
	private ValueOperations<String, Object> valueOperations;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private TestPricePublishService testPricePublishService;

	@Test
	@DisplayName("테스트 시세: Redis stockPrice:: 저장 후 RabbitMQ stock.exchange / stock.price 발행")
	void publishTestPrice_savesRedisAndPublishesRabbit() {
		Long stockId = 1L;
		BigDecimal price = new BigDecimal("72500");
		Stock stock = Stock.builder().id(stockId).stockCode("005930").stockName("삼성전자").build();
		when(stockRepository.findById(stockId)).thenReturn(Optional.of(stock));
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);

		testPricePublishService.publishTestPrice(stockId, price);

		verify(valueOperations).set(
			eq("stockPrice::005930"),
			eq("72500"),
			eq(Duration.ofMinutes(5))
		);

		ArgumentCaptor<StockPriceEventDto> eventCaptor = ArgumentCaptor.forClass(StockPriceEventDto.class);
		verify(rabbitTemplate).convertAndSend(
			eq(RabbitConfig.EXCHANGE_NAME),
			eq(RabbitConfig.ROUTING_KEY),
			eventCaptor.capture()
		);
		assertThat(eventCaptor.getValue().getStockId()).isEqualTo(stockId);
		assertThat(eventCaptor.getValue().getCurrentPrice()).isEqualByComparingTo(price);
	}

	@Test
	@DisplayName("존재하지 않는 stockId면 STOCK_NOT_FOUND")
	void publishTestPrice_stockNotFound() {
		when(stockRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> testPricePublishService.publishTestPrice(99L, BigDecimal.ONE))
			.isInstanceOf(CustomRuntimeException.class)
			.extracting(ex -> ((CustomRuntimeException)ex).getExceptionCode())
			.isEqualTo(ExceptionCode.STOCK_NOT_FOUND);

		verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
	}
}
