package com.example.mockstalk.domain.trade.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.mockstalk.common.config.RabbitConfig;
import com.example.mockstalk.domain.trade.dto.StockPriceEventDto;
import com.example.mockstalk.domain.trade.service.TradeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockPriceListener {

	private final TradeService tradeService;

	@RabbitListener(queues = RabbitConfig.QUEUE_NAME)
	public void onPriceUpdated(StockPriceEventDto event) {
		tradeService.onPriceUpdated(event.getStockId(), event.getCurrentPrice());
	}

}