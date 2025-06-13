package com.example.mockstalk.domain.trade.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.mockstalk.domain.order.entity.Type;

import lombok.Getter;

@Getter
public class TradeResponseDto {

	private String stockId;
	private Type orderType;
	private Long quantity;
	private BigDecimal price;
	private LocalDateTime traderDate;
	private BigDecimal charge; //수수료
	private boolean trade;

}
