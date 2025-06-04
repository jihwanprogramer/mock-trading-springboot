package com.example.mockstalk.domain.account.dto;

import com.example.mockstalk.domain.holdings.entity.Holdings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class HoldingsResponseDto {
	private final String accountName;
	private final String stockCode;
	private final String stockName;
	private final Long quantity;
	// private final BigDecimal averagePrice;
	// private final BigDecimal currentPrice;
	// private final BigDecimal profitRate; 캐싱 고려중

	public static HoldingsResponseDto of(Holdings holdings) {
		return HoldingsResponseDto.builder()
			.accountName(holdings.getAccount().getAccountName())
			.stockCode(holdings.getStock().getStockCode())
			.stockName(holdings.getStock().getStockName())
			.quantity(holdings.getQuantity())
			// 가격 관련 요소들 구현 예정 -> 현재 가격, 매입 평균 단가
			// stock, trade 기능 1차 mvp 통합 이후 코드에 맞게 진행할 예정
			.build();
	}

}
