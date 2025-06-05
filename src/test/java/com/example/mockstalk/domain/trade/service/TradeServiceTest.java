package com.example.mockstalk.domain.trade.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.mockstalk.domain.account.entity.Account;
import com.example.mockstalk.domain.account.repository.AccountRepository;
import com.example.mockstalk.domain.holdings.entity.Holdings;
import com.example.mockstalk.domain.holdings.repository.HoldingsRepository;
import com.example.mockstalk.domain.order.entity.Order;
import com.example.mockstalk.domain.order.entity.OrderStatus;
import com.example.mockstalk.domain.order.entity.Type;
import com.example.mockstalk.domain.order.repository.OrderRepository;
import com.example.mockstalk.domain.stock.entity.Stock;
import com.example.mockstalk.domain.stock.entity.StockStatus;

@SpringBootTest
public class TradeServiceTest {

	@Autowired
	private TradeService tradeService;

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private HoldingsRepository holdingsRepository;

	private Account account;
	private Stock stock;
	private Order order1;
	private Order order2;
	private Holdings holdings;

	@BeforeEach
	void setUp() {

		account = Account.builder()
			.currentBalance(new BigDecimal("1000"))
			.build();
		account = accountRepository.save(account);

		stock = Stock.builder()
			.id(1L)
			.stockName("Test Stock")
			.stockCode("TEST")
			.stockStatus(StockStatus.ACTIVE)
			.build();

		holdings = Holdings.builder()
			.account(account)
			.stock(stock)
			.quantity(10L)
			.build();
		holdingsRepository.save(holdings);

		order1 = Order.builder()
			.account(account)
			.stock(stock)
			.type(Type.LIMIT_SELL)
			.quantity(1L)
			.price(new BigDecimal("600"))
			.orderStatus(OrderStatus.COMPLETED)
			.build();

		order2 = Order.builder()
			.account(account)
			.stock(stock)
			.type(Type.LIMIT_SELL)
			.quantity(1L)
			.price(new BigDecimal("600"))
			.orderStatus(OrderStatus.COMPLETED)
			.build();

		orderRepository.save(order1);
		orderRepository.save(order2);
	}

	@Test
	@DisplayName("계좌에 있는 주식을 두개의 주문으로 매도 했을때(락 걸기전) - 동시성 제어")
	void test_lock() {
		Arrays.asList(order1, order2)
			.parallelStream()
			.forEach(order -> tradeService.tradeOrder(order, stock, new BigDecimal("600")));

		Account updatedAccount = accountRepository.findById(account.getId()).get();
		Holdings updatedholdings = holdingsRepository.findById(holdings.getId()).get();

		System.out.println("최종 잔고: " + updatedAccount.getCurrentBalance());
		System.out.println("최종 수량:" + updatedholdings.getQuantity());

		assertNotEquals(new BigDecimal("2200"), updatedAccount.getCurrentBalance(),
			"동시서 오류로 계좌 잔고가 예상대로 업데이트되지 않음");
	}
}