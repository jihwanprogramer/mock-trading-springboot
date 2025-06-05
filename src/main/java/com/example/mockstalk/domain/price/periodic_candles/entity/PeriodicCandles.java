package com.example.mockstalk.domain.price.periodic_candles.entity;

import com.example.mockstalk.common.baseEntity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@Table(name = "periodic_candles")
@NoArgsConstructor
@AllArgsConstructor
public class PeriodicCandles extends BaseEntity {

    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PeriodicCandleType candleType;

    @Column
    private LocalDateTime date;

    @Column
    private Long openingPrice;

    @Column
    private Long closingPrice;

    @Column
    private Long highPrice;

    @Column
    private Long lowPrice;

    @Column
    private Long volume;

    @Column
    private String stockCode;

    @Column
    private String stockName;

}
