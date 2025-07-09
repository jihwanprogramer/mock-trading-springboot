package com.example.mockstalk.domain.order.service;

import java.math.BigDecimal;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.mockstalk.common.error.CustomRuntimeException;
import com.example.mockstalk.common.error.ExceptionCode;
import com.example.mockstalk.domain.account.entity.Account;
import com.example.mockstalk.domain.account.repository.AccountRepository;
import com.example.mockstalk.domain.holdings.entity.Holdings;
import com.example.mockstalk.domain.holdings.repository.HoldingsRepository;
import com.example.mockstalk.domain.order.cache.CompleteOrderRedisCache;
import com.example.mockstalk.domain.order.dto.LimitOrderRequestDto;
import com.example.mockstalk.domain.order.dto.LimitOrderResponseDto;
import com.example.mockstalk.domain.order.entity.Order;
import com.example.mockstalk.domain.order.entity.OrderStatus;
import com.example.mockstalk.domain.order.entity.Type;
import com.example.mockstalk.domain.order.repository.OrderRepository;
import com.example.mockstalk.domain.stock.entity.Stock;
import com.example.mockstalk.domain.stock.repository.StockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LimitOrderService {

	private final OrderRepository orderRepository;
	private final AccountRepository accountRepository;
	private final StockRepository stockRepository;
	private final HoldingsRepository holdingsRepository;
	private final CompleteOrderRedisCache completeOrderRedisCache;

	public LimitOrderResponseDto saveLimitBuy(UserDetails userDetails, Long accountId,
		LimitOrderRequestDto limitOrderRequestDto) {
		Account account = accountRepository.findById(accountId)
			.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.ACCOUNT_NOT_FOUND));

		if (!account.getUser().getEmail().equals(userDetails.getUsername())) {
			throw new CustomRuntimeException(ExceptionCode.UNAUTHORIZED_ACCOUNT_ACCESS);
		}

		Stock stock = stockRepository.findById(limitOrderRequestDto.getStockId())
			.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.STOCK_NOT_FOUND));

		BigDecimal totalPrice = limitOrderRequestDto.getLimitPrice()
			.multiply(BigDecimal.valueOf(limitOrderRequestDto.getQuantity()));

		if (account.getCurrentBalance().compareTo(totalPrice) < 0) {
			throw new CustomRuntimeException(ExceptionCode.INSUFFICIENT_BALANCE);
		}
		account.decreaseCurrentBalance(totalPrice);

		Order order = Order.builder()
			.account(account)
			.stock(stock)
			.type(Type.LIMIT_BUY)
			.quantity(limitOrderRequestDto.getQuantity())
			.price(limitOrderRequestDto.getLimitPrice())
			.totalPrice(totalPrice)
			.orderStatus(OrderStatus.COMPLETED)
			.build();

		orderRepository.save(order);
		completeOrderRedisCache.add(order);

		return LimitOrderResponseDto.from(order);
	}

	public LimitOrderResponseDto saveLimitSell(UserDetails userDetails, Long accountId,
		LimitOrderRequestDto limitOrderRequestDto) {
		Account account = accountRepository.findById(accountId)
			.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.ACCOUNT_NOT_FOUND));

		if (!account.getUser().getEmail().equals(userDetails.getUsername())) {
			throw new CustomRuntimeException(ExceptionCode.UNAUTHORIZED_ACCOUNT_ACCESS);
		}

		Stock stock = stockRepository.findById(limitOrderRequestDto.getStockId())
			.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.STOCK_NOT_FOUND));

		Holdings holding = holdingsRepository.findByAccountAndStock(account, stock)
			.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.HOLDINGS_NOT_FOUND));

		if (holding.getQuantity() < limitOrderRequestDto.getQuantity()) {
			throw new CustomRuntimeException(ExceptionCode.INSUFFICIENT_HOLDINGS);
		}

		BigDecimal totalPrice = limitOrderRequestDto.getLimitPrice()
			.multiply(BigDecimal.valueOf(limitOrderRequestDto.getQuantity()));

		Order order = Order.builder()
			.account(account)
			.stock(stock)
			.type(Type.LIMIT_SELL)
			.quantity(limitOrderRequestDto.getQuantity())
			.price(limitOrderRequestDto.getLimitPrice())
			.totalPrice(totalPrice)
			.orderStatus(OrderStatus.COMPLETED)
			.build();

		orderRepository.save(order);
		completeOrderRedisCache.add(order);

		return LimitOrderResponseDto.from(order);
	}
}
