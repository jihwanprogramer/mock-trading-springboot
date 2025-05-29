package com.example.mockstalk.domain.stock.contoroller;

import lombok.RequiredArgsConstructor;

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
		String filePath = "src/main/resources/stock_list_with_date.csv";
		stockService.saveStockCsv(filePath);
		return ResponseEntity.ok(ResponseMessage.success());
	}
}
