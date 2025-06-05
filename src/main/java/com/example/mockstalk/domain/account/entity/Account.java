package com.example.mockstalk.domain.account.entity;

import java.math.BigDecimal;

import com.example.mockstalk.common.baseEntity.BaseEntity;
import com.example.mockstalk.common.error.CustomRuntimeException;
import com.example.mockstalk.common.error.ExceptionCode;
import com.example.mockstalk.domain.user.entity.User;

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
@Table(name = "accounts")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 50)
	private String accountName;

	@Column(length = 50)
	private String password;

	@Column
	private BigDecimal initialBalance;

	@Column
	private BigDecimal currentBalance;

	@Column
	private Boolean isActive;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	public void updatePassword(String password) {
		this.password = password;
	}

	public void increaseCurrentBalance(BigDecimal balance) {
		if (balance.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("차감할 금액은 0보다 커야 합니다.");
		}
		if (this.currentBalance.compareTo(balance) < 0) {
			throw new CustomRuntimeException(ExceptionCode.USER_MISMATCH_EXCEPTION); //임시코드
		}
		this.currentBalance = this.currentBalance.add(balance);
	}

	//-1: this < other
	// 0: this == other
	// 1: this > other
	public void decreaseCurrentBalance(BigDecimal balance) {
		if (balance.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("차감할 금액은 0보다 커야 합니다.");
		}
		if (this.currentBalance.compareTo(balance) < 0) {
			throw new CustomRuntimeException(ExceptionCode.USER_MISMATCH_EXCEPTION); //임시코드
		}
		this.currentBalance = this.currentBalance.subtract(balance);
	}

}