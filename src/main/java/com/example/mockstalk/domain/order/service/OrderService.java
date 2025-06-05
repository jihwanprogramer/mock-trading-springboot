package com.example.mockstalk.domain.order.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.mockstalk.common.error.CustomRuntimeException;
import com.example.mockstalk.common.error.ExceptionCode;
import com.example.mockstalk.domain.account.entity.Account;
import com.example.mockstalk.domain.account.repository.AccountRepository;
import com.example.mockstalk.domain.order.dto.OrderListResponseDto;
import com.example.mockstalk.domain.order.entity.Order;
import com.example.mockstalk.domain.order.entity.OrderStatus;
import com.example.mockstalk.domain.order.entity.Type;
import com.example.mockstalk.domain.order.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final AccountRepository accountRepository;

	public Slice<OrderListResponseDto> findOrderByUserId(UserDetails userDetails, Long accountId, Type orderType,
		OrderStatus orderStatus,
		LocalDateTime startDate, LocalDateTime endDate, Long lastId, int size) {
		Account account = accountRepository.findById(accountId)
			.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.ACCOUNT_NOT_FOUND));

		if (!account.getUser().getEmail().equals(userDetails.getUsername())) {
			throw new CustomRuntimeException(ExceptionCode.UNAUTHORIZED_ACCOUNT_ACCESS);
		}

		Pageable pageable = PageRequest.of(0, size);

		return orderRepository.findCursorOrderByAccount(accountId, orderType, orderStatus, startDate, endDate, lastId,
			pageable);
	}

	public void deleteOrder(UserDetails userDetails, Long accountId, Long orderId) {
		Account account = accountRepository.findById(accountId)
			.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.ACCOUNT_NOT_FOUND));

		if (!account.getUser().getEmail().equals(userDetails.getUsername())) {
			throw new CustomRuntimeException(ExceptionCode.UNAUTHORIZED_ACCOUNT_ACCESS);
		}

		Order order = orderRepository.findById(orderId)
			.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.ORDER_NOT_FOUND));

		if (!order.getAccount().getId().equals(accountId)) {
			throw new CustomRuntimeException(ExceptionCode.UNAUTHORIZED_ORDER_ACCESS);
		}

		if (order.getOrderStatus() == OrderStatus.CANCELED) {
			throw new CustomRuntimeException(
				ExceptionCode.ORDER_ALREADY_CANCELED);
		} else if (order.getOrderStatus() == OrderStatus.SETTLE) {
			throw new CustomRuntimeException(
				ExceptionCode.ORDER_ALREADY_SETTLED);
		}

		if (order.getType() == Type.LIMIT_BUY) {
			account.increaseCurrentBalance(order.getPrice());
			accountRepository.save(account);
		}

		order.updateOrderStatus(OrderStatus.CANCELED);
		orderRepository.save(order);
	}
}