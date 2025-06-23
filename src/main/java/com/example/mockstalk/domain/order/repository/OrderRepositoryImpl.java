package com.example.mockstalk.domain.order.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.example.mockstalk.domain.account.entity.QAccount;
import com.example.mockstalk.domain.order.dto.OrderListResponseDto;
import com.example.mockstalk.domain.order.entity.Order;
import com.example.mockstalk.domain.order.entity.OrderStatus;
import com.example.mockstalk.domain.order.entity.QOrder;
import com.example.mockstalk.domain.order.entity.Type;
import com.example.mockstalk.domain.stock.entity.QStock;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom {

	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public Slice<OrderListResponseDto> findCursorOrderByAccount(Long accountId, Type orderType, OrderStatus orderStatus,
		LocalDateTime startDate, LocalDateTime endDate,
		Long lastId, Pageable pageable
	) {
		QOrder order = QOrder.order;
		BooleanBuilder builder = new BooleanBuilder();

		builder.and(accountIdEq(accountId));
		builder.and(orderTypeEq(orderType));
		builder.and(orderStatusEq(orderStatus));
		builder.and(createdAtGoe(startDate));
		builder.and(createdAtLoe(endDate));
		builder.and(idLt(lastId));

		List<OrderListResponseDto> result = jpaQueryFactory
			.select(Projections.constructor(
				OrderListResponseDto.class,
				order.stock.id,
				order.type,
				order.quantity,
				order.price,
				order.price.multiply(Expressions.numberTemplate(BigDecimal.class, "{0}", order.quantity)),
				order.createdAt,
				order.orderStatus
			))
			.from(order)
			.where(builder)
			.orderBy(order.id.desc())
			.limit(pageable.getPageSize() + 1)
			.fetch();

		boolean hasNext = result.size() > pageable.getPageSize();
		if (hasNext) {
			result.remove(pageable.getPageSize());
		}

		return new SliceImpl<>(result, pageable, hasNext);
	}

	public List<Order> findByOrderStatus(OrderStatus orderStatus) {
		QOrder order = QOrder.order;

		return jpaQueryFactory
			.selectFrom(order)
			.where(order.orderStatus.eq(orderStatus))
			.fetch();
	}

	@Override
	public List<Order> findAllReadyOrdersByStock(Long stockId) {

		QOrder order = QOrder.order;
		QStock stock = QStock.stock;
		QAccount account = QAccount.account;

		return jpaQueryFactory
			.selectFrom(order)
			.leftJoin(order.stock, stock).fetchJoin()
			.leftJoin(order.account, account).fetchJoin()
			.where(
				order.orderStatus.eq(OrderStatus.COMPLETED),
				order.stock.id.eq(stockId)
			)
			.fetch();
	}

	@Override
	public List<Order> findAllReadyOrdersWithFetchJoin(OrderStatus status) {
		QOrder order = QOrder.order;

		return jpaQueryFactory
			.selectFrom(order)
			.leftJoin(order.account).fetchJoin()
			.leftJoin(order.stock).fetchJoin()
			.where(order.orderStatus.eq(status))
			.fetch();
	}

	private BooleanExpression accountIdEq(Long accountId) {
		return accountId != null ? QOrder.order.account.id.eq(accountId) : null;
	}

	private BooleanExpression orderTypeEq(Type type) {
		return type != null ? QOrder.order.type.eq(type) : null;
	}

	private BooleanExpression orderStatusEq(OrderStatus status) {
		return status != null ? QOrder.order.orderStatus.eq(status) : null;
	}

	private BooleanExpression createdAtGoe(LocalDateTime startDate) {
		return startDate != null ? QOrder.order.createdAt.goe(startDate) : null;
	}

	private BooleanExpression createdAtLoe(LocalDateTime endDate) {
		return endDate != null ? QOrder.order.createdAt.loe(endDate) : null;
	}

	private BooleanExpression idLt(Long lastId) {
		return lastId != null ? QOrder.order.id.lt(lastId) : null;
	}

}
