package com.example.mockstalk.domain.order.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.example.mockstalk.domain.order.dto.OrderListResponseDto;

public interface OrderRepositoryCustom {

	Slice<OrderListResponseDto> findCursorOrderByAccount(Long accountId, long lastId, Pageable pageable);
}
