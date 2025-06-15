package com.example.mockstalk.domain.price.intraday_candles.websocket;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.mockstalk.domain.price.intraday_candles.entity.CandleType;
import com.example.mockstalk.domain.price.intraday_candles.entity.IntradayCandle;
import com.example.mockstalk.domain.price.intraday_candles.repository.IntradayCandleRepository;
import com.example.mockstalk.domain.stock.entity.Stock;
import com.example.mockstalk.domain.stock.repository.StockRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CandleBuffer {

	private final IntradayCandleRepository candleRepository;
	private final StockRepository stockRepository;

	// 종목코드별 Tick 리스트 버퍼
	private final Map<String, List<Tick>> tickBuffer = new ConcurrentHashMap<>();

	public void addTick(String stockCode, Tick tick) {
		tickBuffer.computeIfAbsent(stockCode, k -> Collections.synchronizedList(new ArrayList<>())).add(tick);
	}

	@Scheduled(cron = "0 * * * * *")  // 매 분마다
	public void aggregateCandles() {
		LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

		for (Map.Entry<String, List<Tick>> entry : tickBuffer.entrySet()) {
			String stockCode = entry.getKey();
			List<Tick> ticks = entry.getValue();

			List<Tick> oneMinuteTicks = filterTicks(ticks, now.minusMinutes(1), now);
			saveCandle(stockCode, oneMinuteTicks, now.minusMinutes(1), CandleType.MIN);

			if (now.getMinute() % 3 == 0) {
				List<Tick> threeMinuteTicks = filterTicks(ticks, now.minusMinutes(3), now);
				saveCandle(stockCode, threeMinuteTicks, now.minusMinutes(3), CandleType.MIN3);
			}

			if (now.getMinute() % 5 == 0) {
				List<Tick> fiveMinuteTicks = filterTicks(ticks, now.minusMinutes(5), now);
				saveCandle(stockCode, fiveMinuteTicks, now.minusMinutes(5), CandleType.MIN5);
			}

			// 오래된 Tick 제거(10분 초과)
			ticks.removeIf(tick -> tick.timestamp().isBefore(now.minusMinutes(10)));
		}
	}

	private List<Tick> filterTicks(List<Tick> ticks, LocalDateTime start, LocalDateTime end) {
		return ticks.stream()
			.filter(t -> !t.timestamp().isBefore(start) && t.timestamp().isBefore(end))
			.sorted(Comparator.comparing(Tick::timestamp))
			.collect(Collectors.toList());
	}

	private void saveCandle(String stockCode, List<Tick> ticks, LocalDateTime timestamp, CandleType candleType) {
		if (ticks.isEmpty())
			return;

		long open = ticks.get(0).price();
		long close = ticks.get(ticks.size() - 1).price();
		long high = ticks.stream().mapToLong(Tick::price).max().orElse(open);
		long low = ticks.stream().mapToLong(Tick::price).min().orElse(open);

		Stock stock = stockRepository.findByStockCode(stockCode);
		if (stock == null)
			return;

		boolean exists = candleRepository.existsByStock_StockCodeAndTimeStamp(stockCode, timestamp);
		if (exists)
			return;

		IntradayCandle candle = IntradayCandle.builder()
			.stockCode(stockCode)
			.stockName(stock.getStockName())
			.stock(stock)
			.timeStamp(timestamp)
			.openingPrice(open)
			.closingPrice(close)
			.highPrice(high)
			.lowPrice(low)
			.tradingVolume(0L)
			.tradingValue(0L)
			.candleType(candleType)
			.build();

		candleRepository.save(candle);
		System.out.println("캔들 저장됨: " + stockCode + " [" + candleType + "] " + timestamp);
	}
}

