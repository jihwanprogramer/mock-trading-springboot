package com.example.mockstalk.domain.price.periodic_candles.dto;

import com.example.mockstalk.domain.price.periodic_candles.entity.PeriodicCandleType;
import com.example.mockstalk.domain.price.periodic_candles.entity.PeriodicCandles;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PeriodicCandleResponseDto {

    private Long id;
    private LocalDateTime date;
    private PeriodicCandleType candleType;
    private Long openingPrice;
    private Long closingPrice;
    private Long highPrice;
    private Long lowPrice;
    private Long volume;
    private String stockCode;
    private String stockName;

    public static PeriodicCandleResponseDto from(PeriodicCandles periodicCandles) {
        return new PeriodicCandleResponseDto(
            periodicCandles.getId(),
            periodicCandles.getDate(),
            periodicCandles.getCandleType(),
            periodicCandles.getOpeningPrice(),
            periodicCandles.getClosingPrice(),
            periodicCandles.getHighPrice(),
            periodicCandles.getLowPrice(),
            periodicCandles.getVolume(),
            periodicCandles.getStock().getStockCode(),
            periodicCandles.getStock().getStockName()
        );
    }


}
