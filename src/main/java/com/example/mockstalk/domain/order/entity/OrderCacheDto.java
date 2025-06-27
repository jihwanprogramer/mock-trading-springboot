package com.example.mockstalk.domain.order.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class OrderCacheDto implements Serializable {
	private Long orderId;
	private Long accountId;
	private Long stockId;
	private Long quantity;
	private BigDecimal price;
	private BigDecimal totalPrice;
	private Type type;
	private OrderStatus status;

	public static OrderCacheDto from(Order o) {
		return OrderCacheDto.builder()
			.orderId(o.getId())
			.accountId(o.getAccount().getId())
			.stockId(o.getStock().getId())
			.quantity(o.getQuantity())
			.price(o.getPrice())
			.totalPrice(o.getTotalPrice())
			.type(o.getType())
			.status(o.getOrderStatus())
			.build();
	}
}