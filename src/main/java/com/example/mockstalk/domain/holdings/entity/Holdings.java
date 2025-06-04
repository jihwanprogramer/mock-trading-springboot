package com.example.mockstalk.domain.holdings.entity;

import com.example.mockstalk.common.baseEntity.BaseEntity;
import com.example.mockstalk.domain.account.entity.Accounts;
import com.example.mockstalk.domain.stock.entity.Stock;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "account_id")
	private Account account;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stock_id")
	private Stock stock;

	public void increaseQuantity(Long orderQuantity) {
		if (orderQuantity <= 0) {
			throw new IllegalArgumentException("차감할 수량은 0보다 커야 합니다.");
		}
		if (this.quantity < orderQuantity) {
			throw new CustomRuntimeException(ExceptionCode.INSUFFICIENT_HOLDINGS); //임시코드
		}
		this.quantity += orderQuantity;
	}

	public void decreaseQuantity(Long orderQuantity) {
		if (orderQuantity <= 0) {
			throw new IllegalArgumentException("차감할 수량은 0보다 커야 합니다.");
		}
		if (this.quantity < orderQuantity) {
			throw new CustomRuntimeException(ExceptionCode.INSUFFICIENT_HOLDINGS); //임시코드
		}
		this.quantity -= orderQuantity;
	}
}
