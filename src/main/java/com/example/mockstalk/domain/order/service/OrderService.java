package com.example.mockstalk.domain.order.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.example.mockstalk.common.error.CustomRuntimeException;
import com.example.mockstalk.common.error.ExceptionCode;
import com.example.mockstalk.domain.account.entity.Accounts;
import com.example.mockstalk.domain.account.repository.AccountRepository;
import com.example.mockstalk.domain.holdings.entity.Holdings;
import com.example.mockstalk.domain.holdings.repository.HoldingsRepository;
import com.example.mockstalk.domain.order.dto.OrderListResponseDto;
import com.example.mockstalk.domain.order.entity.Order;
import com.example.mockstalk.domain.order.entity.OrderStatus;
import com.example.mockstalk.domain.order.entity.Type;
import com.example.mockstalk.domain.order.repository.OrderRepository;
import com.example.mockstalk.domain.stock.entity.Stock;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final AccountRepository accountRepository;
	private final HoldingsRepository holdingsRepository;

	public Slice<OrderListResponseDto> findOrderByUserId(Long accountId, Long lastId, int size) {

		Pageable pageable = PageRequest.of(0, size);
		
		return orderRepository.findCursorOrderByAccount(accountId, lastId, pageable);
	}

	public void deleteOrder(Long accountId, Long orderId) {

		Order findOrder = orderRepository.findById(orderId)
			.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.USER_MISMATCH_EXCEPTION));

		Accounts account = accountRepository.findById(accountId)
			.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.USER_MISMATCH_EXCEPTION));

		//유저디테일 가져오면 계좌 유저아이디와 유저디테일의 아이디가 맞는지 IF 구분 적기

		Order order = orderRepository.findById(orderId)
			.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.USER_MISMATCH_EXCEPTION));

		if (!order.getAccount().getId().equals(accountId)) {
			throw new CustomRuntimeException(ExceptionCode.USER_MISMATCH_EXCEPTION);
		}

		if (!(order.getOrderStatus() == OrderStatus.PENDING)) {
			throw new CustomRuntimeException(ExceptionCode.USER_MISMATCH_EXCEPTION);
		}

		Stock stock = order.getStock();
		if (order.getType() == Type.LIMIT_BUY) {
			account.increaseCurrentBalance(order.getPrice());
			accountRepository.save(account);
		} else if (order.getType() == Type.LIMIT_SELL) {
			Holdings holding = holdingsRepository.findByAccountsAndStock(account, stock)
				.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.USER_MISMATCH_EXCEPTION));

			holding.increaseQuantity(order.getQuantity());
			holdingsRepository.save(holding);
		}

		order.updateOrderStatus(OrderStatus.CANCELED);
		orderRepository.save(order);
	}

}
