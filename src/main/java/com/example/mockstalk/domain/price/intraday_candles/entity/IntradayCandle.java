package com.example.mockstalk.domain.price.intraday_candles.entity;

import com.example.mockstalk.common.baseEntity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "intraday_candles")
@NoArgsConstructor
@AllArgsConstructor
public class IntradayCandle extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
