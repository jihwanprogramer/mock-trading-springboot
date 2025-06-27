package com.example.mockstalk.domain.intraday_candle;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.mockstalk.domain.price.intraday_candles.entity.CandleType;
import com.example.mockstalk.domain.price.intraday_candles.entity.IntradayCandle;
import com.example.mockstalk.domain.price.intraday_candles.repository.IntradayCandleRepository;
import com.example.mockstalk.domain.price.intraday_candles.service.IntradayCandleService;
import com.example.mockstalk.domain.stock.repository.StockRepository;

public class IntradayCandleServiceTest {

	@Mock
	private IntradayCandleRepository candleRepository;

	@Mock
	private StockRepository stockRepository;

	@InjectMocks
	private IntradayCandleService intradayCandleService;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testGetCandles_ReturnsList() {
		String stockCode = "005930";
		String date = "20250617";
		int interval = 1;

		LocalDateTime start = LocalDateTime.of(2025, 6, 17, 0, 0);
		LocalDateTime end = LocalDateTime.of(2025, 6, 17, 23, 59);

		IntradayCandle candle = IntradayCandle.builder()
			.stockCode(stockCode)
			.candleType(CandleType.MIN)
			.timeStamp(LocalDateTime.now())
			.build();

		when(candleRepository.findByStock_StockCodeAndCandleTypeAndTimeStampBetween(
			stockCode, CandleType.MIN, start, end)).thenReturn(List.of(candle));

		List<IntradayCandle> result = intradayCandleService.getCandles(stockCode, date, interval);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getStockCode()).isEqualTo(stockCode);
	}

	@Test
	void testGetCandles_InvalidInterval() {
		String stockCode = "005930";
		String date = "20250617";

		assertThatThrownBy(() -> intradayCandleService.getCandles(stockCode, date, 7))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("지원하지 않는 interval");

	}

	@Test
	void testGetCandles_EmptyResult() {
		String stockCode = "005930";
		String date = "20250617";
		int interval = 1;

		LocalDateTime start = LocalDateTime.of(2025, 6, 17, 0, 0);
		LocalDateTime end = LocalDateTime.of(2025, 6, 17, 23, 59);

		when(candleRepository.findByStock_StockCodeAndCandleTypeAndTimeStampBetween(
			stockCode, CandleType.MIN, start, end)).thenReturn(List.of());

		List<IntradayCandle> result = intradayCandleService.getCandles(stockCode, date, interval);

		assertThat(result).isEmpty();
	}

	@Test
	void testGetCandles_invalidDateFormat() {
		String stockCode = "005930";
		String date = "2025-06-17";
		int interval = 1;

		assertThatThrownBy(() -> intradayCandleService.getCandles(stockCode, date, interval))
			.isInstanceOf(DateTimeParseException.class);
	}

	@Test
	void testGetCandles_ThreeMinuteInterval() {
		String stockCode = "005930";
		String date = "20250617";
		int interval = 3;

		LocalDateTime start = LocalDateTime.of(2025, 6, 17, 0, 0);
		LocalDateTime end = LocalDateTime.of(2025, 6, 17, 23, 59);

		IntradayCandle candle = IntradayCandle.builder()
			.stockCode(stockCode)
			.candleType(CandleType.MIN3)
			.timeStamp(LocalDateTime.now())
			.build();

		when(candleRepository.findByStock_StockCodeAndCandleTypeAndTimeStampBetween(
			stockCode, CandleType.MIN3, start, end)).thenReturn(List.of(candle));

		List<IntradayCandle> result = intradayCandleService.getCandles(stockCode, date, interval);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getCandleType()).isEqualTo(CandleType.MIN3);
	}

}
