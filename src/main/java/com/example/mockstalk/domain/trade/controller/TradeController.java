package com.example.mockstalk.domain.trade.controller;

import java.time.LocalDateTime;

import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.mockstalk.common.response.ResponseMessage;
import com.example.mockstalk.domain.order.entity.Type;
import com.example.mockstalk.domain.trade.dto.TradeResponseDto;
import com.example.mockstalk.domain.trade.service.TradeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TradeController {

	private final TradeService tradeService;

	@PostMapping("/accounts/{accountId}/trades")
	public ResponseEntity<ResponseMessage<Slice<TradeResponseDto>>> findTradeByUserId(
		@AuthenticationPrincipal UserDetails userDetails,
		@PathVariable(name = "accountId") Long accountId,
		@RequestParam(required = false) Type orderType,
		@RequestParam(required = false) LocalDateTime startDate,
		@RequestParam(required = false) LocalDateTime endDate,
		@RequestParam(required = false) Long lastId,
		@RequestParam(defaultValue = "10") int size
	) {
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseMessage.success(
				tradeService.findTradeByUserId(userDetails, accountId, orderType, startDate, endDate, lastId, size)));
	}
}
