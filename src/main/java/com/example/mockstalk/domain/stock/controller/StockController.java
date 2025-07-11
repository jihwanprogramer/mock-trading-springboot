package com.example.mockstalk.domain.stock.controller;

import java.io.InputStream;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mockstalk.common.response.ResponseMessage;
import com.example.mockstalk.domain.stock.dto.StockResponseDto;
import com.example.mockstalk.domain.stock.service.StockService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class StockController {

	private final StockService stockService;

	@PostMapping("/savestock")
	public ResponseEntity<ResponseMessage<String>> saveStockCsv() {
		InputStream is = getClass().getClassLoader().getResourceAsStream("kospi_code.csv");
		stockService.saveStockCsv(is);
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(ResponseMessage.success());
	}

	@GetMapping("/v1/stocks")
	public ResponseEntity<ResponseMessage<List<StockResponseDto>>> getStocks() {
		List<StockResponseDto> result = stockService.getAllStocks();
		return ResponseEntity.ok(ResponseMessage.success(result));
	}
}
