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
	private BigDecimal initialBalance;  // 초기 자산

	@Column
	private BigDecimal currentBalance; // 현재 계좌의 잔고 (보유 종목 평가 금액 제외)

	// CurrentAsset <- 현재 총 자산 ( 현재 잔고 + 보유 종목 평가 금액의 합 )   해당 필드를 레디스 캐싱 처리 할 예정임.

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

	// Account 2차 통합 전 개발 예정 사항
	//
	// 1. 메서드별 유효성 및 권한 검증 로직 추가
	//
	// 2. 계좌 로그인 인증/인가 작업
	//
	//
	// 3. 레디스 캐싱 처리할 데이터 작업
	// 	1) 계좌 - 수익률(profitRate)
	// 	2) 보유종목 - 현재가(currentPrice), 수익률(profitRate), 실현 손익(realizedProfit)
	//
	// 4. 기능별 비즈니스 고도화 및 성능 개선
	// ex) 계좌 다건 조회 기능
	// 사용자의 계좌 전체 조회 시 보유 종목 표기 필요성 판단
	// -> 계좌별 보유 종목 전체를 조회하는 것은 비효율적
	// -> service 단에서 가장 수익률 높은 보유종목 3개 선정하는 메서드 구현 및 적용
}