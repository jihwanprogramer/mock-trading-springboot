package com.example.mockstalk.domain.holdings.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.example.mockstalk.common.baseEntity.BaseEntity;
import com.example.mockstalk.common.error.CustomRuntimeException;
import com.example.mockstalk.common.error.ExceptionCode;
import com.example.mockstalk.domain.account.entity.Account;
import com.example.mockstalk.domain.stock.entity.Stock;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "holdings")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Holdings extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private Long quantity;

	@Column
	private BigDecimal averagePrice;

	// 보유 종목 가격 관련 데이터
	// averagePrice(매수 평균 단가) <- DB에 존재
	// currentPrice(현재 주식 평균가) <- 해당 필드는 캐싱 고려중
	// profitRate(수익률)는 캐싱으로 관리
	// 고민중 - 실현 손익 필드 추가 여부 <- 저장 방식 또한

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "account_id")
	private Account account;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stock_id")
	private Stock stock;

	public void decreaseQuantity(Long orderQuantity) {
		if (orderQuantity <= 0) {
			throw new IllegalArgumentException("차감할 수량은 0보다 커야 합니다.");
		}
		if (this.quantity < orderQuantity) {
			throw new CustomRuntimeException(ExceptionCode.INSUFFICIENT_HOLDINGS); //임시코드
		}
		this.quantity -= orderQuantity;
	}

	// increaseQuantity 메서드 통합 고려 << 이 메서드에서 평균단가와 수량 증가 한 번에 처리 가능하도록
	// 사는 경우와 달리, 파는 경우는 평균단가에 변동이 생기지 않음.
	public void updateAveragePrice(Long orderQuantity, BigDecimal totalOrderPrice) {
		//     유효성 검증 로직
		// 			if (orderQuantity <= 0) {
		// 		throw new IllegalArgumentException("차감할 수량은 0보다 커야 합니다.");
		// 	}
		// 		if (this.quantity < orderQuantity) {
		// 		throw new CustomRuntimeException(ExceptionCode.INSUFFICIENT_HOLDINGS); //임시코드
		// 	}
		// 		this.quantity += orderQuantity;
		// }

		// 1. TotalQuantity -> 기존 수량 + 주문 수량
		Long totalQuantity = this.quantity + orderQuantity;

		// 2. TotalPrice -> 해당 종목 전체 매입가(quantity x averagePrice) + 총 주문 금액
		BigDecimal totalOldPrice = this.averagePrice.multiply(BigDecimal.valueOf(this.quantity));
		BigDecimal totalPrice = totalOldPrice.add(totalOrderPrice);

		// 3. 갱신할 평균단가 계산
		this.averagePrice = totalPrice.divide(BigDecimal.valueOf(totalQuantity), 2, RoundingMode.HALF_UP);

		// 4. 수량 갱신
		this.quantity = totalQuantity;
	}

}
