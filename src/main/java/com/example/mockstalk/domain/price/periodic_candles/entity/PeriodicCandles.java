package com.example.mockstalk.domain.price.periodic_candles.entity;

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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@Table(
    name = "periodic_candles",
    uniqueConstraints = @UniqueConstraint(columnNames = {"stock_id", "candle_type", "date"})
)
@NoArgsConstructor
@AllArgsConstructor
public class PeriodicCandles extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @ManyToOne
    @JoinColumn
    private Stock stock;

}
