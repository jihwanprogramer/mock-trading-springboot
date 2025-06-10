package com.example.mockstalk.domain.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.mockstalk.domain.order.entity.OrderStatus;
import com.example.mockstalk.domain.order.entity.Type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderListResponseDto {

	private final Long stockId;
	private final Type type;
	private final Long quantity;
	private final BigDecimal price;
	private final BigDecimal totalPrice;
	private final LocalDateTime created_at;
	private final OrderStatus orderStatus;

}
