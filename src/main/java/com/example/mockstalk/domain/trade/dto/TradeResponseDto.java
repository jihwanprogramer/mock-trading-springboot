package com.example.mockstalk.domain.trade.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.mockstalk.domain.order.entity.Type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TradeResponseDto {

	private Long orderId;
	private Type orderType;
	private Long quantity;
	private BigDecimal price;
	private LocalDateTime traderDate;
	private Double charge;
	private boolean trade;

}
