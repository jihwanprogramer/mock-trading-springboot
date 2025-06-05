package com.example.mockstalk.domain.stock.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.example.mockstalk.common.baseEntity.BaseEntity;

@Getter
@Entity
@Table(name = "stock")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String stockName;

	@Column
	private String stockCode;

	@Column
	private LocalDate listedDate;

	@Column
	private LocalDate delistedDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private StockStatus stockStatus = StockStatus.ACTIVE;

}
