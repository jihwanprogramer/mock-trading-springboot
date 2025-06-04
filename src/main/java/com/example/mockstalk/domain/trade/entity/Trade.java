package com.example.mockstalk.domain.trade.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.mockstalk.common.baseEntity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "trade")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Trade extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private Long orderId;

	@Column
	private Long accountId;

	@Column
	private Long quantity;

	@Column
	private BigDecimal price;

	@Column
	private LocalDateTime traderDate;

	@Column
	private double charge;

	@Column(nullable = false)
	private boolean trade;

}
