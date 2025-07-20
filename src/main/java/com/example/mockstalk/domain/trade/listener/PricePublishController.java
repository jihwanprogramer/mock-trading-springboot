package com.example.mockstalk.domain.trade.listener;

import java.math.BigDecimal;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.mockstalk.common.config.RabbitConfig;
import com.example.mockstalk.domain.trade.dto.StockPriceEventDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/test/price")
@RequiredArgsConstructor
public class PricePublishController {

	private final RabbitTemplate rabbitTemplate;

	@PostMapping
	public ResponseEntity<String> publishPrice(@RequestParam Long stockId,
		@RequestParam BigDecimal currentPrice) {
		StockPriceEventDto event = new StockPriceEventDto(stockId, currentPrice);
		rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_NAME, RabbitConfig.ROUTING_KEY, event);
		return ResponseEntity.ok("테스트 메시지 발행 완료");
	}
}
