package com.example.mockstalk.domain.trade.service;

import java.math.BigDecimal;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.example.mockstalk.common.config.RabbitConfig;
import com.example.mockstalk.domain.trade.dto.StockPriceEventDto;

import lombok.RequiredArgsConstructor;

@Component
@Profile("local")
@RequiredArgsConstructor
public class PricePublisherTest implements CommandLineRunner {

	private final RabbitTemplate rabbitTemplate;

	@Override
	public void run(String... args) throws Exception {
		StockPriceEventDto event = new StockPriceEventDto(1L, new BigDecimal("10250.00"));
		rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_NAME, RabbitConfig.ROUTING_KEY, event);
		System.out.println("테스트 메시지 발행 완료");
	}
}
