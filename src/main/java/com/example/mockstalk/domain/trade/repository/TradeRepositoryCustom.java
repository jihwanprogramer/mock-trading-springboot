package com.example.mockstalk.domain.trade.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.example.mockstalk.domain.order.entity.Type;
import com.example.mockstalk.domain.trade.dto.TradeResponseDto;

public interface TradeRepositoryCustom {

	Slice<TradeResponseDto> findCursorTradeByAccount(Long accountId, Type orderType, LocalDateTime startDate,
		LocalDateTime endDate, Long lastId, Pageable pageable);
}
