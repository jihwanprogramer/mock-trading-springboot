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
}
