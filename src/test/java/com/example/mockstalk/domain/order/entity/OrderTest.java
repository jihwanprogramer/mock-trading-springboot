package com.example.mockstalk.domain.order.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.mockstalk.domain.account.entity.Account;
import com.example.mockstalk.domain.stock.entity.Stock;

class OrderTest {

	private Account account;
	private Order order;
	private Stock stock;

	@BeforeEach
	void setUp() {
		account = Account.builder().id(1L).build();
		stock = Stock.builder().id(1L).build();
		order = Order.builder()
			.type(Type.LIMIT_BUY)
			.quantity(100L)
			.price(BigDecimal.valueOf(50000))
			.orderStatus(OrderStatus.COMPLETED)
			.account(account)
			.stock(stock)
			.build();
	}

	@Test
	@DisplayName("주문 상태 변경 테스트 - CANCELED로 변경")
	void updateOrderStatus() {
		// when
		order.updateOrderStatus(OrderStatus.CANCELED);

		// then
		assertEquals(OrderStatus.CANCELED, order.getOrderStatus());
		assertEquals(Type.LIMIT_BUY, order.getType());
		assertEquals(100L, order.getQuantity());
		assertEquals(BigDecimal.valueOf(50000), order.getPrice());
		assertEquals(stock, order.getStock());
		assertNotNull(order.getAccount());
	}

}
