package com.example.mockstalk.domain.trade.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.example.mockstalk.domain.order.entity.Type;
import com.example.mockstalk.domain.trade.dto.TradeResponseDto;
import com.example.mockstalk.domain.trade.entity.QTrade;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TradeRepositoryImpl implements TradeRepositoryCustom {

	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public Slice<TradeResponseDto> findCursorTradeByAccount(Long accountId, Type orderType, LocalDateTime startDate,
		LocalDateTime endDate, Long lastId, Pageable pageable) {

		QTrade trade = QTrade.trade1;
		BooleanBuilder builder = new BooleanBuilder();

		builder.and(accountIdEq(accountId));
		builder.and(orderTypeEq(orderType));
		builder.and(createdAtGoe(startDate));
		builder.and(createdAtLoe(endDate));
		builder.and(idLt(lastId));

		List<TradeResponseDto> result = jpaQueryFactory
			.select(Projections.constructor(
				TradeResponseDto.class,
				trade.orderId,
				trade.orderType,
				trade.quantity,
				trade.price,
				trade.traderDate,
				trade.charge,
				trade.trade
			))
			.from(trade)
			.where(builder)
			.orderBy(trade.traderDate.desc())
			.limit(pageable.getPageSize() + 1)
			.fetch();

		boolean hasNext = result.size() > pageable.getPageSize();
		if (hasNext) {
			result.remove(pageable.getPageSize());
		}

		return new SliceImpl<>(result, pageable, hasNext);
	}

	private BooleanExpression accountIdEq(Long accountId) {
		return accountId != null ? QTrade.trade1.accountId.eq(accountId) : null;
	}

	private BooleanExpression orderTypeEq(Type type) {
		return type != null ? QTrade.trade1.orderType.eq(type) : null;
	}

	private BooleanExpression createdAtGoe(LocalDateTime startDate) {
		return startDate != null ? QTrade.trade1.createdAt.goe(startDate) : null;
	}

	private BooleanExpression createdAtLoe(LocalDateTime endDate) {
		return endDate != null ? QTrade.trade1.createdAt.loe(endDate) : null;
	}

	private BooleanExpression idLt(Long lastId) {
		return lastId != null ? QTrade.trade1.id.lt(lastId) : null;
	}
}