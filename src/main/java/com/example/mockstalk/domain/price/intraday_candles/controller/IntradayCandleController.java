package com.example.mockstalk.domain.price.intraday_candles.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.mockstalk.domain.price.intraday_candles.dto.IntradayCandleDto;
import com.example.mockstalk.domain.price.intraday_candles.entity.IntradayCandle;
import com.example.mockstalk.domain.price.intraday_candles.service.IntradayCandleService;
import com.example.mockstalk.domain.stock.repository.StockRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/intra")
@Slf4j
public class IntradayCandleController {

	private final IntradayCandleService intradayCandleService;
	private final StockRepository stockRepository;

	@PostMapping("/candles/fetch/all")
	public ResponseEntity<String> fetchCandlesForAllStocks() {
		List<String> stockCodes = stockRepository.findAllStockCodes();

		for (String stockCode : stockCodes) {
			try {
				intradayCandleService.fetchAndSaveIntradayCandles(stockCode);
			} catch (Exception e) {
				log.error("수동 수집 실패: {} -> {}", stockCode, e.getMessage());
			}
		}
		return ResponseEntity.ok("모든 종목 캔들 수집 완료");
	}

	@GetMapping("/stocks/{stockName}/candles")
	public ResponseEntity<List<IntradayCandleDto>> getCandlesByName(
		@PathVariable String stockName,
		@RequestParam String date,
		@RequestParam(defaultValue = "1") int interval) {

		List<IntradayCandle> candles = intradayCandleService.getCandlesByName(stockName, date, interval);
		List<IntradayCandleDto> dtoList = candles.stream()
			.map(IntradayCandleDto::fromEntity)
			.toList();

		log.info("조회완료: {}-{}건 (interval: {})", stockName, dtoList.size(), interval);

		return ResponseEntity.ok(dtoList);
	}

}
