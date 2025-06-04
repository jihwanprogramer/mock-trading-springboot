package com.example.mockstalk.domain.order.entity;

import com.example.mockstalk.common.baseEntity.BaseEntity;
import com.example.mockstalk.domain.account.entity.Accounts;
import com.example.mockstalk.domain.stock.entity.Stock;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "orders")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Type type;

	private Long quantity;

	private BigDecimal price;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderStatus orderStatus;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "account_id")
	private Account account;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stock_id")
	private Stock stock;

	public void updateOrderStatus(OrderStatus orderStatus) {
		this.orderStatus = orderStatus;
	}

}
