package com.example.mockstalk.domain.trade.listener;

import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.mockstalk.domain.trade.dto.StockPriceEventDto;
import com.example.mockstalk.domain.trade.service.TradeService;

@ExtendWith(MockitoExtension.class)
class StockPriceListenerTest {

	@Mock
	private TradeService tradeService;

	@InjectMocks
	private StockPriceListener stockPriceListener;

	@Test
	@DisplayName("Rabbit 수신 시 TradeService.onPriceUpdated 위임")
	void onPriceUpdated_delegatesToTradeService() {
		StockPriceEventDto event = new StockPriceEventDto(1L, new BigDecimal("10000"));

		stockPriceListener.onPriceUpdated(event);

		verify(tradeService).onPriceUpdated(1L, new BigDecimal("10000"));
	}
}
