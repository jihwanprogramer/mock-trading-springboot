package com.example.mockstalk.domain.trade.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.mockstalk.common.error.CustomRuntimeException;
import com.example.mockstalk.common.error.ExceptionCode;
import com.example.mockstalk.domain.account.entity.Account;
import com.example.mockstalk.domain.account.repository.AccountRepository;
import com.example.mockstalk.domain.holdings.entity.Holdings;
import com.example.mockstalk.domain.holdings.repository.HoldingsRepository;
import com.example.mockstalk.domain.order.entity.Order;
import com.example.mockstalk.domain.order.entity.OrderStatus;
import com.example.mockstalk.domain.order.entity.Type;
import com.example.mockstalk.domain.order.repository.OrderRepository;
import com.example.mockstalk.domain.stock.entity.Stock;
import com.example.mockstalk.domain.stock.repository.StockRepository;
import com.example.mockstalk.domain.trade.dto.TradeResponseDto;
import com.example.mockstalk.domain.trade.entity.Trade;
import com.example.mockstalk.domain.trade.repository.TradeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TradeService {

	private final OrderRepository orderRepository;
	private final AccountRepository accountRepository;
	private final StockRepository stockRepository;
	private final HoldingsRepository holdingsRepository;
	private final TradeRepository tradeRepository;

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

		switch (order.getType()) {
			case LIMIT_BUY:
				Holdings holding = holdingsRepository.findByAccountAndStock(account, stock)
					.orElseGet(() -> Holdings.builder()
						.account(account)
						.stock(stock)
						.quantity(0L)
						.build());
				// holding.increaseQuantity(order.getQuantity());
				holding.updateAveragePrice(order.getQuantity(), totalPrice);
				holdingsRepository.save(holding);
				break;
			case LIMIT_SELL:
				Holdings sellHolding = holdingsRepository.findByAccountAndStock(account, stock)
					.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.HOLDINGS_NOT_FOUND));

				sellHolding.decreaseQuantity(order.getQuantity());
				account.increaseCurrentBalance(totalPrice);

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
			.price(order.getPrice())
			.traderDate(LocalDateTime.now())
			.charge(0.0) // 매도시 수수료 차감 (아직 적용전)
			.trade(true)
			.build();

		tradeRepository.save(trade);
	}

	@Scheduled(fixedRate = 1000) // 1초마다 실행
	public void settleOrders() {
		//주문완료 보유 주식 리스트
		List<Order> completeOrders = orderRepository.findByOrderStatus(OrderStatus.COMPLETED);

		for (Order order : completeOrders) {
			Stock stock = order.getStock();
			Stock currentStock = stockRepository.findById(stock.getId()).
				orElseThrow(() -> new CustomRuntimeException(ExceptionCode.STOCK_NOT_FOUND));

			// if (order.getType() == Type.LIMIT_BUY && currentStock.getPrice().compareTo(order.getPrice()) > 0) {
			// 	continue;
			// }
			// if (order.getType() == Type.LIMIT_SELL && currentStock.getPrice().compareTo(order.getPrice()) < 0) {
			// 	continue;
			// }
			
			// tradeOrder(order, stock, currentStock.getPrice());
		}
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
}

