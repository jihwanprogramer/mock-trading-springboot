package com.example.mockstalk.domain.account.entity;

import java.math.BigDecimal;

import com.example.mockstalk.common.baseEntity.BaseEntity;
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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "accounts")
@NoArgsConstructor
@AllArgsConstructor
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

}