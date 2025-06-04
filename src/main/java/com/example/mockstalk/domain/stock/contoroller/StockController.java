package com.example.mockstalk.domain.stock.contoroller;

import java.io.InputStream;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.mockstalk.common.response.ResponseMessage;
import com.example.mockstalk.domain.stock.service.StockService;

@RestController
@RequiredArgsConstructor
public class StockController {

	private final StockService stockService;

	@PostMapping("/savestock")
	public ResponseEntity<ResponseMessage<String>> saveStockCsv() {
		InputStream is = getClass().getClassLoader().getResourceAsStream("kospi_code_csv.csv");
		stockService.saveStockCsv(is);
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(ResponseMessage.success());
	}
}
