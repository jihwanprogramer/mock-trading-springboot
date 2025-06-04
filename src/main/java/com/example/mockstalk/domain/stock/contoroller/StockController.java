package com.example.mockstalk.domain.stock.contoroller;

import java.io.InputStream;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mockstalk.common.response.ResponseMessage;
import com.example.mockstalk.domain.stock.service.StockService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class StockController {

	private final StockService stockService;

	@PostMapping("/savestock")
	public ResponseEntity<ResponseMessage<String>> saveStockCsv() {
		String filePath = "src/main/resources/kospi_code_csv.csv";
		stockService.saveStockCsv(filePath);
		return ResponseEntity.ok(ResponseMessage.success());
	}
}
