package com.example.mockstalk.domain.price.intraday_candles.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.mockstalk.domain.price.intraday_candles.dto.IntradayCandleDto;
import com.example.mockstalk.domain.price.intraday_candles.entity.IntradayCandle;
import com.example.mockstalk.domain.price.intraday_candles.service.IntradayCandleService;
import com.example.mockstalk.domain.stock.repository.StockRepository;
import com.example.mockstalk.domain.user.service.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class IntradayCandleController {

	private final IntradayCandleService intradayCandleService;
	private final StockRepository stockRepository;

	/*@PostMapping("/api/candles/fetch")
	public ResponseEntity<String> fetchCandleByUser(
		@RequestParam String stockCode,
		@AuthenticationPrincipal CustomUserDetails userDetails //로그인한 유저 정보 주입
	) {
		Long userId = userDetails.getUser().getId();
		intradayCandleService.fetchAndSaveIntradayCandles(stockCode, userId);

		return ResponseEntity.ok(stockCode + " 캔들 데이터 수집 완료");
	}*/

	@PostMapping("/api/candles/fetch/all")
	public ResponseEntity<String> fetchCandlesForAllStocks(@AuthenticationPrincipal CustomUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		List<String> stockCodes = stockRepository.findAllStockCodes();

		for (String stockCode : stockCodes) {
			try {
				intradayCandleService.fetchAndSaveIntradayCandles(stockCode, userId);
			} catch (Exception e) {
				System.err.println("❌ 실패: " + stockCode + " → " + e.getMessage());
			}
		}

		return ResponseEntity.ok("📊 모든 종목 캔들 수집 완료");
	}

	@GetMapping("/api/stocks/{stockCode}/candles")
	public List<IntradayCandleDto> getCandles(@PathVariable String stockCode,
		@RequestParam String date,
		@RequestParam(defaultValue = "1") int interval) {
		List<IntradayCandle> candles = intradayCandleService.getCandles(stockCode, date, interval);
		return candles.stream()
			.map(IntradayCandleDto::fromEntity)
			.toList();
	}
}
