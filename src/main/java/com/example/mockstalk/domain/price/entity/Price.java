package com.example.mockstalk.domain.price.entity;

import com.example.mockstalk.common.baseEntity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "price")
@NoArgsConstructor
@AllArgsConstructor
public class Price extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long openingPrice;

    private Long closingPrice;

    private Long highPrice;

    private Long lowPrice;

    private Long tradingVolume;

    private Long tradingValue;

    private Long marketCap;

    private LocalDateTime timeStamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CandleType candleType;
}
