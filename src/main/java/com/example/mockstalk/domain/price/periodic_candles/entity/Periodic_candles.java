package com.example.mockstalk.domain.price.periodic_candles.entity;

import com.example.mockstalk.common.baseEntity.BaseEntity;
import com.example.mockstalk.domain.price.intraday_candles.entity.CandleType;
import com.example.mockstalk.domain.stock.entity.Stock;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "periodic_candles")
@NoArgsConstructor
@AllArgsConstructor
public class Periodic_candles extends BaseEntity {
    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CandleType candleType;

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

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "stock_id", referencedColumnName = "id")
    private Stock stock;



}
