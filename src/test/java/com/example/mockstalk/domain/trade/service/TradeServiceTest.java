// package com.example.mockstalk.domain.trade.service;
//
// import java.math.BigDecimal;
// import java.util.stream.IntStream;
//
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
//
// import com.example.mockstalk.domain.account.entity.Account;
// import com.example.mockstalk.domain.account.repository.AccountRepository;
// import com.example.mockstalk.domain.holdings.entity.Holdings;
// import com.example.mockstalk.domain.holdings.repository.HoldingsRepository;
// import com.example.mockstalk.domain.order.entity.Order;
// import com.example.mockstalk.domain.order.entity.OrderStatus;
// import com.example.mockstalk.domain.order.entity.Type;
// import com.example.mockstalk.domain.order.repository.OrderRepository;
// import com.example.mockstalk.domain.stock.entity.Stock;
// import com.example.mockstalk.domain.stock.entity.StockStatus;
// import com.example.mockstalk.domain.stock.repository.StockRepository;
//
// @SpringBootTest
// public class TradeServiceTest {
//
// 	@Autowired
// 	private TradeService tradeService;
//
// 	@Autowired
// 	private AccountRepository accountRepository;
//
// 	@Autowired
// 	private OrderRepository orderRepository;
//
// 	@Autowired
// 	private StockRepository stockRepository;
//
// 	@Autowired
// 	private HoldingsRepository holdingsRepository;
//
// 	private Account account;
// 	private Stock stock;
// 	private Holdings holdings;
//
// 	@BeforeEach
// 	void setUp() {
//
// 		account = Account.builder()
// 			.currentBalance(new BigDecimal("1000000"))
// 			.initialBalance(new BigDecimal("1000000"))
// 			.password("test1234")
// 			.build();
// 		account = accountRepository.save(account);
//
// 		stock = Stock.builder()
// 			.id(1L)
// 			.stockName("Test Stock")
// 			.stockCode("TEST")
// 			.stockStatus(StockStatus.ACTIVE)
// 			.build();
// 		stock = stockRepository.save(stock);
//
// 		holdings = Holdings.builder()
// 			.account(account)
// 			.stock(stock)
// 			.averagePrice(BigDecimal.ZERO)
// 			.quantity(10L)
// 			.build();
// 		holdingsRepository.save(holdings);
// 	}
//
// 	@Test
// 	@DisplayName("100개의 동시 매수 주문 처리 테스트")
// 	void test_concurrent_buy_orders() {
// 		int numberOfOrders = 100;
//
// 		IntStream.range(0, numberOfOrders).parallel().forEach(i -> {
// 			BigDecimal currentPrice = new BigDecimal("500");
//
// 			account.decreaseCurrentBalance(currentPrice);
// 			accountRepository.save(account);
//
// 			Order order = Order.builder()
// 				.account(account)
// 				.stock(stock)
// 				.quantity(1L)
// 				.type(Type.MARKET_BUY)
// 				.orderStatus(OrderStatus.COMPLETED)
// 				.price(currentPrice)
// 				.build();
// 			orderRepository.save(order);
//
// 			tradeService.tradeOrder(order, stock, currentPrice);
// 		});
//
// 		Account updatedAccount = accountRepository.findById(account.getId()).orElseThrow();
// 		Holdings updatedHoldings = holdingsRepository.findById(holdings.getId()).orElseThrow();
//
// 		System.out.printf("기존 잔고: %s → 최종 잔고: %s\n", account.getInitialBalance(), updatedAccount.getCurrentBalance());
// 		System.out.printf("기존 수량: %d → 최종 수량: %d\n", holdings.getQuantity(), updatedHoldings.getQuantity());
// 		System.out.printf("동시성 오류가 발생한 주문 횟수: %d\n",
// 			numberOfOrders - (updatedHoldings.getQuantity() - holdings.getQuantity()));
// 	}
// }