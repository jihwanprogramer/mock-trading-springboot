package com.example.mockstalk.domain.order.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.example.mockstalk.common.error.CustomRuntimeException;
import com.example.mockstalk.common.error.ExceptionCode;
import com.example.mockstalk.domain.account.entity.Accounts;
import com.example.mockstalk.domain.account.repository.AccountRepository;
import com.example.mockstalk.domain.holdings.entity.Holdings;
import com.example.mockstalk.domain.holdings.repository.HoldingsRepository;
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

	public LimitOrderResponseDto saveLimitBuy(Long accountId, LimitOrderRequestDto limitOrderRequestDto) {
		Accounts account = accountRepository.findById(accountId).orElseThrow(() -> new CustomRuntimeException(
			ExceptionCode.USER_MISMATCH_EXCEPTION //임시 예외처리코드
		));

		Stock stock = stockRepository.findById(limitOrderRequestDto.getStockId())
			.orElseThrow(() -> new CustomRuntimeException(
				ExceptionCode.USER_MISMATCH_EXCEPTION)); //임시 예외처리코드

		BigDecimal totalPrice = limitOrderRequestDto.getLimitPrice()
			.multiply(BigDecimal.valueOf(limitOrderRequestDto.getQuantity()));

		account.decreaseCurrentBalance(totalPrice);

		Order order = Order.builder()
			.account(account)
			.stock(stock)
			.type(Type.MARKET_SELL)
			//주식 가격 불러오는거 추후 추가
			.quantity(limitOrderRequestDto.getQuantity())
			.price(totalPrice)
			.orderStatus(OrderStatus.PENDING)
			.build();

		orderRepository.save(order);
		return LimitOrderResponseDto.from(order);
	}

	public LimitOrderResponseDto saveLimitSell(Long accountId, LimitOrderRequestDto limitOrderRequestDto) {

		Accounts account = accountRepository.findById(accountId).orElseThrow(() -> new CustomRuntimeException(
			ExceptionCode.USER_MISMATCH_EXCEPTION //임시 예외처리코드
		));

		Stock stock = stockRepository.findById(limitOrderRequestDto.getStockId())
			.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.USER_MISMATCH_EXCEPTION)); //임시 예외처리코드

		Holdings holding = holdingsRepository.findByAccountsAndStock(account, stock)
			.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.USER_MISMATCH_EXCEPTION)); //임시 예외처리코드

		holding.decreaseQuantity(limitOrderRequestDto.getQuantity());

		BigDecimal totalPrice = limitOrderRequestDto.getLimitPrice()
			.multiply(BigDecimal.valueOf(limitOrderRequestDto.getQuantity()));

		Order order = Order.builder()
			.account(account)
			.stock(stock)
			.type(Type.LIMIT_SELL)
			.quantity(limitOrderRequestDto.getQuantity())
			.price(totalPrice)
			.orderStatus(OrderStatus.PENDING)
			.build();

		orderRepository.save(order);
		return LimitOrderResponseDto.from(order);
	}
}
