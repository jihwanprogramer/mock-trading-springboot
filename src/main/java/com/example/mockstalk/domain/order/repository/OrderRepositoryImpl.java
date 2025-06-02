package com.example.mockstalk.domain.order.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.example.mockstalk.domain.order.dto.LimitOrderResponseDto;
import com.example.mockstalk.domain.order.dto.MarketOrderResponseDto;
import com.example.mockstalk.domain.order.dto.OrderListResponseDto;
import com.example.mockstalk.domain.order.entity.QOrder;
import com.example.mockstalk.domain.order.entity.Type;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom {

	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public Slice<OrderListResponseDto> findCursorOrderByAccount(Long accountId, long lastId, Pageable pageable) {

		QOrder order = QOrder.order;

		List<LimitOrderResponseDto> limitOrders = jpaQueryFactory
			.select(Projections.constructor(
				LimitOrderResponseDto.class,
				order.id,
				order.price,
				order.quantity
			))
			.from(order)
			.where(
				order.account.id.eq(accountId),
				order.type.in(Type.LIMIT_BUY, Type.LIMIT_SELL),
				order.id.lt(lastId)
			)
			.orderBy(order.id.desc())
			.limit(pageable.getPageSize() + 1)
			.fetch();

		if (limitOrders.size() > pageable.getPageSize()) {
			limitOrders.remove(pageable.getPageSize());
		}

		List<MarketOrderResponseDto> marketOrders = jpaQueryFactory
			.select(Projections.constructor(
				MarketOrderResponseDto.class,
				order.id,
				order.price,
				order.quantity
			))
			.from(order)
			.where(
				order.account.id.eq(accountId),
				order.type.in(Type.MARKET_BUY, Type.MARKET_SELL),
				order.id.lt(lastId)
			)
			.orderBy(order.id.desc())
			.limit(pageable.getPageSize() + 1)
			.fetch();

		if (marketOrders.size() > pageable.getPageSize()) {
			marketOrders.remove(pageable.getPageSize());
		}

		OrderListResponseDto dto = new OrderListResponseDto(limitOrders, marketOrders);

		boolean isCheck = limitOrders.size() > pageable.getPageSize() || marketOrders.size() > pageable.getPageSize();

		return new SliceImpl<>(List.of(dto), pageable, isCheck);
	}
}
