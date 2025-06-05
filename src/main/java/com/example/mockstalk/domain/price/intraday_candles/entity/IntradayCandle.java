package com.example.mockstalk.domain.price.intraday_candles.entity;

import java.time.LocalDateTime;

import com.example.mockstalk.common.baseEntity.BaseEntity;
import com.example.mockstalk.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "intraday_candles")
@NoArgsConstructor
@AllArgsConstructor
public class IntradayCandle extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column
	String stockCode;

	@Column
	private Long openingPrice;

	@Column
	private Long closingPrice;

	@Column
	private Long highPrice;

	@Column
	private Long lowPrice;

	@Column
	private Long tradingVolume;

	@Column
	private Long tradingValue;

	@Column
	private Long marketCap;

	@Column
	private LocalDateTime timeStamp;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CandleType candleType;
}
