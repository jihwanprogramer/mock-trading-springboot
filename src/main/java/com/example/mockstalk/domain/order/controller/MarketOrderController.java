package com.example.mockstalk.domain.order.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.mockstalk.common.response.ResponseMessage;
import com.example.mockstalk.domain.order.dto.MarketOrderRequestDto;
import com.example.mockstalk.domain.order.dto.MarketOrderResponseDto;
import com.example.mockstalk.domain.order.service.MarketOrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MarketOrderController {

	private final MarketOrderService marketOrderService;

	//시장가 매수
	@PostMapping("/accounts/{accountId}/orders/market_buy")
	public ResponseEntity<ResponseMessage<MarketOrderResponseDto>> saveMarketBuy(
		@PathVariable Long accountId,
		@RequestBody MarketOrderRequestDto marketOrderRequestDto
	) {
		return ResponseEntity.ok(
			ResponseMessage.success(marketOrderService.saveMarketBuy(accountId, marketOrderRequestDto)));
	}

	//시장가 매도
	@PostMapping("/accounts/{accountId}/orders/market_sell")
	public ResponseEntity<ResponseMessage<MarketOrderResponseDto>> saveMarketSell(
		@PathVariable Long accountId,
		@RequestBody MarketOrderRequestDto marketOrderRequestDto
	) {
		return ResponseEntity.ok(
			ResponseMessage.success(marketOrderService.saveMarketSell(accountId, marketOrderRequestDto)));
	}

}
