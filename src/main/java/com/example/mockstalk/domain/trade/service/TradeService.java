package com.example.mockstalk.domain.trade.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.mockstalk.common.custom_annotation.DistributedLock;
import com.example.mockstalk.common.error.CustomRuntimeException;
import com.example.mockstalk.common.error.ExceptionCode;
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
import com.example.mockstalk.domain.trade.dto.TradeResponseDto;
import com.example.mockstalk.domain.trade.entity.Trade;
import com.example.mockstalk.domain.trade.repository.TradeRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeService {

	private final OrderRepository orderRepository;
	private final AccountRepository accountRepository;
	private final HoldingsRepository holdingsRepository;
	private final TradeRepository tradeRepository;
	private final CompleteOrderRedisCache completeOrderRedisCache;

	@PostConstruct
	public void loadCompletedOrdersToRedis() {
		List<Order> pendingOrders = orderRepository.findAllReadyOrdersWithFetchJoin(OrderStatus.COMPLETED);
		for (Order order : pendingOrders) {
			completeOrderRedisCache.add(order);
		}
	}

	@DistributedLock(key = "'trade:' + #tradeId")
	public void tradeOrder(Order order, Stock stock, BigDecimal currentPrice) {
		if (order.getOrderStatus() == OrderStatus.CANCELED) {
			throw new CustomRuntimeException(ExceptionCode.ORDER_ALREADY_CANCELED);
		}
		if (order.getOrderStatus() == OrderStatus.SETTLE) {
			throw new CustomRuntimeException(ExceptionCode.ORDER_ALREADY_COMPLETED);
		}

		Account account = accountRepository.findById(order.getAccount().getId())
			.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.ACCOUNT_NOT_FOUND));

		BigDecimal totalPrice = currentPrice.multiply(BigDecimal.valueOf(order.getQuantity()));
		BigDecimal fee = BigDecimal.ZERO;

		switch (order.getType()) {
			case MARKET_BUY:
			case LIMIT_BUY:
				Holdings holding = holdingsRepository.findByAccountAndStock(account, stock)
					.orElseGet(() -> Holdings.builder()
						.account(account)
						.stock(stock)
						.averagePrice(BigDecimal.ZERO)
						.quantity(0L)
						.build());

				holding.updateAveragePrice(order.getQuantity(), totalPrice);
				holdingsRepository.save(holding);
				break;
			case MARKET_SELL:
			case LIMIT_SELL:
				Holdings sellHolding = holdingsRepository.findByAccountAndStock(account, stock)
					.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.HOLDINGS_NOT_FOUND));

				sellHolding.decreaseQuantity(order.getQuantity());

				fee = totalPrice.multiply(BigDecimal.valueOf(0.0015));
				BigDecimal afterFee = totalPrice.subtract(fee);
				account.increaseCurrentBalance(afterFee);

				holdingsRepository.save(sellHolding);
				break;
		}

		order.updateOrderStatus(OrderStatus.SETTLE);
		orderRepository.save(order);
		accountRepository.save(account);

		Trade trade = Trade.builder()
			.orderId(order.getId())
			.accountId(account.getId())
			.quantity(order.getQuantity())
			.orderType(order.getType())
			.price(currentPrice)
			.traderDate(LocalDateTime.now())
			.charge(fee.doubleValue())
			.trade(true)
			.build();

		tradeRepository.save(trade);
	}

	public Slice<TradeResponseDto> findTradeByUserId(UserDetails userDetails, Long accountId, Type orderType,
		LocalDateTime startDate, LocalDateTime endDate, Long lastId, int size) {
		Account account = accountRepository.findById(accountId)
			.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.ACCOUNT_NOT_FOUND));

		if (!account.getUser().getEmail().equals(userDetails.getUsername())) {
			throw new CustomRuntimeException(ExceptionCode.UNAUTHORIZED_ACCOUNT_ACCESS);
		}

		Pageable pageable = PageRequest.of(0, size);

		return tradeRepository.findCursorTradeByAccount(accountId, orderType, startDate, endDate, lastId, pageable);
	}

	public void onPriceUpdated(Long stockId, BigDecimal currentPrice) {
		List<OrderCacheDto> cachedOrders = completeOrderRedisCache.getOrders(stockId);
		if (cachedOrders.isEmpty())
			return;

		List<Long> orderIds = cachedOrders.stream()
			.map(OrderCacheDto::getOrderId)
			.toList();

		Map<Long, Order> orderMap = orderRepository.findAllById(orderIds).stream()
			.collect(Collectors.toMap(Order::getId, Function.identity()));

		for (OrderCacheDto dto : cachedOrders) {
			Order order = orderMap.get(dto.getOrderId());
			if (order == null) {
				completeOrderRedisCache.remove(dto);
				continue;
			}

			if (order.getOrderStatus() == OrderStatus.SETTLE) {
				completeOrderRedisCache.remove(dto);
				continue;
			}

			if (dto.getType() == Type.LIMIT_BUY && currentPrice.compareTo(dto.getPrice()) > 0)
				continue;
			if (dto.getType() == Type.LIMIT_SELL && currentPrice.compareTo(dto.getPrice()) < 0)
				continue;

			tradeOrder(order, order.getStock(), currentPrice);
			completeOrderRedisCache.remove(dto);
		}
	}
}


