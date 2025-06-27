package com.example.mockstalk.domain.intraday_candle;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.mockstalk.domain.price.intraday_candles.controller.IntradayCandleController;
import com.example.mockstalk.domain.price.intraday_candles.entity.CandleType;
import com.example.mockstalk.domain.price.intraday_candles.entity.IntradayCandle;
import com.example.mockstalk.domain.price.intraday_candles.service.IntradayCandleService;
import com.example.mockstalk.domain.stock.entity.Stock;
import com.example.mockstalk.domain.stock.repository.StockRepository;

class IntradayCandleControllerTest {

	private IntradayCandleService intradayCandleService;
	private StockRepository stockRepository;
	private IntradayCandleController intradayCandleController;

	@BeforeEach
	void setUp() {
		intradayCandleService = mock(IntradayCandleService.class);
		stockRepository = mock(StockRepository.class);
		intradayCandleController = new IntradayCandleController(intradayCandleService, stockRepository);
	}

	@Test
	void testGetCandles_ReturnsData() {
		String stockCode = "005930";
		String date = "20250617";
		int interval = 1;

		Stock stock = Stock.builder()
			.stockCode(stockCode)
			.stockName("삼성전자")
			.build();

		IntradayCandle candle = IntradayCandle.builder()
			.stockCode(stockCode)
			.stockName("삼성전자")
			.stock(stock)
			.timeStamp(LocalDateTime.now())
			.candleType(CandleType.MIN)
			.build();

		when(intradayCandleService.getCandles(stockCode, date, interval))
			.thenReturn(List.of(candle));

		var response = intradayCandleController.getCandles(stockCode, date, interval);

		assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(response.getBody()).hasSize(1);
	}
}