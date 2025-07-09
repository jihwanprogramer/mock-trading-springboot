package com.example.mockstalk.domain.order.dto;

import java.math.BigDecimal;
import java.time.LocalTime;

import com.example.mockstalk.domain.order.entity.Order;
import com.example.mockstalk.domain.order.entity.OrderStatus;
import com.example.mockstalk.domain.order.entity.Type;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MarketOrderResponseDto {

	private final Long stockId;
	private final Type type;
	private final Long quantity;
	private final BigDecimal price;
	private final LocalTime created_at;
	private final OrderStatus orderStatus;

	public static MarketOrderResponseDto from(Order order) {
		return MarketOrderResponseDto.builder()
			.stockId(order.getStock().getId())
			.type(order.getType())
			.quantity(order.getQuantity())
			.price(order.getPrice())
			.created_at(order.getCreatedAt().toLocalTime())
			.orderStatus(order.getOrderStatus())
			.build();
	}
}
