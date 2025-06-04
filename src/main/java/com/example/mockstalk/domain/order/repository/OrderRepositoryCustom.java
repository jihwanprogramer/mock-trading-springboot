package com.example.mockstalk.domain.order.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.example.mockstalk.domain.order.dto.OrderListResponseDto;
import com.example.mockstalk.domain.order.entity.Order;
import com.example.mockstalk.domain.order.entity.OrderStatus;

public interface OrderRepositoryCustom {

	Slice<OrderListResponseDto> findCursorOrderByAccount(Long accountId, long lastId, Pageable pageable);

	List<Order> findByOrderStatus(OrderStatus orderStatus);
}
