package com.example.mockstalk.domain.account.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.example.mockstalk.domain.holdings.entity.Holdings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class HoldingsResponseDto {
	private final String accountName; // 계좌명
	private final String stockCode; // 주식코드
	private final String stockName; // 주식이름
	private final Long quantity; // 보유량
	private final BigDecimal currentPrice; // 현재가(종가) [캐싱]
	private final BigDecimal averagePrice; // 구매 평균단가
	private final BigDecimal purchasedPrice; // 총 구매 금액 = 평균단가 * 수량 [ DB에 존재 X, 단순 DB들의 연산 값임 ]
	private final BigDecimal evaluatedPrice; // 총 평가 금액 = 현재가 * 수량  [ 캐싱 ]
	private final BigDecimal profitRate; // 종목 수익률 = (현재가 - 평균단가) / 평균단가 * 100    [ 캐싱 ]

	public static HoldingsResponseDto of(Holdings holdings, BigDecimal currentPrice) {
		Long quantity = holdings.getQuantity();
		BigDecimal averagePrice = holdings.getAveragePrice();

		BigDecimal purchasedPrice = averagePrice.multiply(BigDecimal.valueOf(quantity));
		BigDecimal evaluatedPrice = currentPrice.multiply(BigDecimal.valueOf(quantity));

		BigDecimal profitRate = BigDecimal.ZERO;
		if (averagePrice.compareTo(BigDecimal.ZERO) != 0) {
			profitRate = currentPrice.subtract(averagePrice)
				.divide(averagePrice, 4, RoundingMode.HALF_UP)
				.multiply(BigDecimal.valueOf(100));
		}

		return HoldingsResponseDto.builder()
			.stockCode(holdings.getStock().getStockCode())
			.stockName(holdings.getStock().getStockName())
			.quantity(quantity)
			.averagePrice(averagePrice)
			.currentPrice(currentPrice)
			.purchasedPrice(purchasedPrice)
			.evaluatedPrice(evaluatedPrice)
			.profitRate(profitRate)
			.build();
	}

}
