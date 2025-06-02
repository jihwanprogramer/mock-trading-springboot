package com.example.mockstalk.domain.order.controller;

import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.mockstalk.common.response.ResponseMessage;
import com.example.mockstalk.domain.order.dto.OrderListResponseDto;
import com.example.mockstalk.domain.order.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	@GetMapping("/accounts/{accountId}/orders")
	public ResponseEntity<ResponseMessage<Slice<OrderListResponseDto>>> findOrderByUserId(
		@PathVariable(name = "accountId") Long accountId,
		@RequestParam(required = false) Long lastId,
		@RequestParam(defaultValue = "10") int size
	) {
		return ResponseEntity.ok(ResponseMessage.success(orderService.findOrderByUserId(accountId, lastId, size)));

	}

	@DeleteMapping("/accounts/{accountId}/orders/cancel/{orderId}")
	public ResponseEntity<ResponseMessage<Void>> deleteOrder(
		@PathVariable(name = "accountId") Long accountId,
		@PathVariable(name = "orderId") Long orderId
	) {
		orderService.deleteOrder(accountId, orderId);

		return ResponseEntity.ok(ResponseMessage.success());
	}

}
