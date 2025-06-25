package com.example.mockstalk.domain.price.intraday_candles.entity;

import java.time.LocalDateTime;

import com.example.mockstalk.common.baseEntity.BaseEntity;
import com.example.mockstalk.domain.stock.entity.Stock;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Builder
@Table(name = "intraday_candles")
@NoArgsConstructor
@AllArgsConstructor
public class IntradayCandle extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn
	private Stock stock;

	@Column(nullable = false)
	private String stockCode;

	@Column(nullable = false)
	private String stockName;

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
	private LocalDateTime timeStamp;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CandleType candleType;
}
