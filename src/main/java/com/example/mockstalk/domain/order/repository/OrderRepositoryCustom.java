package com.example.mockstalk.domain.order.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.example.mockstalk.domain.order.dto.OrderListResponseDto;
import com.example.mockstalk.domain.order.entity.Order;
import com.example.mockstalk.domain.order.entity.OrderStatus;
import com.example.mockstalk.domain.order.entity.Type;

public interface OrderRepositoryCustom {

	Slice<OrderListResponseDto> findCursorOrderByAccount(Long accountId, Type orderType, OrderStatus orderStatus,
		LocalDateTime startDate, LocalDateTime endDate, Long lastId, Pageable pageable);

	List<Order> findByOrderStatus(OrderStatus orderStatus);

	List<Order> findAllReadyOrdersWithFetchJoin(OrderStatus status);
}
