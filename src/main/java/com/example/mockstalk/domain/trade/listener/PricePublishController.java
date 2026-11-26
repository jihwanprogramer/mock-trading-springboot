package com.example.mockstalk.domain.trade.listener;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.mockstalk.domain.trade.service.TestPricePublishService;

import lombok.RequiredArgsConstructor;

/**
 * Manual / local verification endpoint for the internal price → matching pipeline.
 * Does not connect to Korea Investment WebSocket; delegates to {@link TestPricePublishService}.
 */
@RestController
@RequestMapping("/api/test/price")
@RequiredArgsConstructor
public class PricePublishController {

	private final TestPricePublishService testPricePublishService;

	@PostMapping
	public ResponseEntity<String> publishPrice(@RequestParam Long stockId,
		@RequestParam BigDecimal currentPrice) {
		testPricePublishService.publishTestPrice(stockId, currentPrice);
		return ResponseEntity.ok("테스트 시세 Redis 저장 및 RabbitMQ 발행 완료");
	}
}
