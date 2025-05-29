package com.example.mockstalk.domain.trade.entity;

import com.example.mockstalk.common.baseEntity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "trade")
@NoArgsConstructor
@AllArgsConstructor
public class Trade extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private Long orderId;

    @Column(nullable = true)
    private Long accountId;

    @Column(nullable = true)
    private Long quantity;

    @Column(nullable = true)
    private Long price;

    @Column(nullable = true)
    private LocalDateTime traderDate;

    @Column(nullable = true)
    private double charge;

    @Column(nullable = false)
    private boolean trade;


}
