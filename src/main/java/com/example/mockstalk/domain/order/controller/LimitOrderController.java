package com.example.mockstalk.domain.order.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.mockstalk.common.response.ResponseMessage;
import com.example.mockstalk.domain.order.dto.LimitOrderRequestDto;
import com.example.mockstalk.domain.order.dto.LimitOrderResponseDto;
import com.example.mockstalk.domain.order.service.LimitOrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class LimitOrderController {

	private final LimitOrderService limitOrderService;

	@PostMapping("/accounts/{accountId}/orders/limit_buy")
	public ResponseEntity<ResponseMessage<LimitOrderResponseDto>> saveLimitBuy(
		@AuthenticationPrincipal UserDetails userDetails,
		@PathVariable Long accountId,
		@RequestBody LimitOrderRequestDto limitOrderRequestDto
	) {
		System.out.println(userDetails);
		return ResponseEntity.ok(
			ResponseMessage.success(limitOrderService.saveLimitBuy(userDetails, accountId, limitOrderRequestDto)));
	}

	@PostMapping("/accounts/{accountId}/orders/limit_sell")
	public ResponseEntity<ResponseMessage<LimitOrderResponseDto>> saveLimitSell(
		@AuthenticationPrincipal UserDetails userDetails,
		@PathVariable Long accountId,
		@RequestBody LimitOrderRequestDto limitOrderRequestDto
	) {
		return ResponseEntity.ok(
			ResponseMessage.success(limitOrderService.saveLimitSell(userDetails, accountId, limitOrderRequestDto)));
	}

}
