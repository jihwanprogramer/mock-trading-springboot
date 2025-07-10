package com.example.mockstalk.domain.price.livePrice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.mockstalk.domain.price.livePrice.service.LivePriceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LivePriceController {

	private final LivePriceService livePriceService;

	@GetMapping("/price")
	public ResponseEntity<String> getPriceByStockName(@RequestParam String stockName) {
		System.out.println("컨트롤러 진입");
		String price = livePriceService.getCurrentPriceByStockName(stockName);
		return ResponseEntity.ok(price);
	}
}
