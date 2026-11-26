package com.example.mockstalk.domain.trade.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.mockstalk.domain.account.entity.Account;
import com.example.mockstalk.domain.account.repository.AccountRepository;
import com.example.mockstalk.domain.holdings.entity.Holdings;
import com.example.mockstalk.domain.holdings.repository.HoldingsRepository;
import com.example.mockstalk.domain.order.cache.CompleteOrderRedisCache;
import com.example.mockstalk.domain.order.entity.Order;
import com.example.mockstalk.domain.order.entity.OrderCacheDto;
import com.example.mockstalk.domain.order.entity.OrderStatus;
import com.example.mockstalk.domain.order.entity.Type;
import com.example.mockstalk.domain.order.repository.OrderRepository;
import com.example.mockstalk.domain.stock.entity.Stock;
import com.example.mockstalk.domain.trade.repository.TradeRepository;

@ExtendWith(MockitoExtension.class)
class TradeServiceOnPriceUpdatedTest {

	@Mock
	private OrderRepository orderRepository;
	@Mock
	private AccountRepository accountRepository;
	@Mock
	private HoldingsRepository holdingsRepository;
	@Mock
	private TradeRepository tradeRepository;
	@Mock
	private CompleteOrderRedisCache completeOrderRedisCache;

	@InjectMocks
	private TradeService tradeService;

	@Test
	@DisplayName("대기 주문이 없으면 매칭을 시도하지 않는다")
	void onPriceUpdated_emptyCache_noop() {
		when(completeOrderRedisCache.getOrders(1L)).thenReturn(List.of());

		tradeService.onPriceUpdated(1L, new BigDecimal("10000"));

		verify(orderRepository, never()).findAllById(any());
		verify(completeOrderRedisCache, never()).remove(any());
	}

	@Test
	@DisplayName("지정가 매수: 현재가가 지정가 이하이면 체결 후 캐시 제거")
	void onPriceUpdated_limitBuy_matchesWhenPriceAtOrBelowLimit() {
		Long stockId = 1L;
		BigDecimal limitPrice = new BigDecimal("10000");
		BigDecimal currentPrice = new BigDecimal("9900");

		Account account = Account.builder()
			.id(10L)
			.currentBalance(new BigDecimal("1000000"))
			.initialBalance(new BigDecimal("1000000"))
			.build();
		Stock stock = Stock.builder().id(stockId).stockCode("005930").stockName("삼성전자").build();
		Order order = Order.builder()
			.id(100L)
			.account(account)
			.stock(stock)
			.quantity(1L)
			.price(limitPrice)
			.totalPrice(limitPrice)
			.type(Type.LIMIT_BUY)
			.orderStatus(OrderStatus.COMPLETED)
			.build();
		OrderCacheDto dto = OrderCacheDto.from(order);

		when(completeOrderRedisCache.getOrders(stockId)).thenReturn(List.of(dto));
		when(orderRepository.findAllById(List.of(100L))).thenReturn(List.of(order));
		when(accountRepository.findById(10L)).thenReturn(java.util.Optional.of(account));
		when(holdingsRepository.findByAccountAndStock(account, stock)).thenReturn(java.util.Optional.empty());
		when(holdingsRepository.save(any(Holdings.class))).thenAnswer(inv -> inv.getArgument(0));
		when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
		when(tradeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		tradeService.onPriceUpdated(stockId, currentPrice);

		verify(completeOrderRedisCache).remove(dto);
		verify(tradeRepository).save(any());
	}

	@Test
	@DisplayName("지정가 매수: 현재가가 지정가보다 높으면 체결하지 않는다")
	void onPriceUpdated_limitBuy_skipsWhenPriceAboveLimit() {
		Long stockId = 1L;
		Account account = Account.builder().id(10L).build();
		Stock stock = Stock.builder().id(stockId).stockCode("005930").stockName("삼성전자").build();
		Order order = Order.builder()
			.id(100L)
			.account(account)
			.stock(stock)
			.quantity(1L)
			.price(new BigDecimal("10000"))
			.totalPrice(new BigDecimal("10000"))
			.type(Type.LIMIT_BUY)
			.orderStatus(OrderStatus.COMPLETED)
			.build();
		OrderCacheDto dto = OrderCacheDto.from(order);

		when(completeOrderRedisCache.getOrders(stockId)).thenReturn(List.of(dto));
		when(orderRepository.findAllById(List.of(100L))).thenReturn(List.of(order));

		tradeService.onPriceUpdated(stockId, new BigDecimal("10100"));

		verify(completeOrderRedisCache, never()).remove(any());
		verify(tradeRepository, never()).save(any());
	}
}
