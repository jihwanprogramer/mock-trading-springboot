package com.example.mockstalk.domain.order.entity;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;

import com.example.mockstalk.domain.account.entity.Account;
import com.example.mockstalk.domain.stock.entity.Stock;

class OrderTest {

	private Order order;
	private Stock stock;

	@BeforeEach
	void setUp() {
		Account mockAccount = Account.builder().id(1L).build();
		Stock mockStock = Stock.builder().id(1L).build();
		Order.builder()
			.type(Type.MARKET_BUY)
			.quantity(100L)
			.price(BigDecimal.valueOf(50000))
			.orderStatus(OrderStatus.COMPLETED)
			.account(mockAccount)
			.stock(mockStock)
			.build();
	}

	// @Test
	// void updateOrderStatus() {
	// 	OrderStatus n
	// }
}