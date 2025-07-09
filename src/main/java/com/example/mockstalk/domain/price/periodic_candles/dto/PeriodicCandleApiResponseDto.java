package com.example.mockstalk.domain.price.periodic_candles.dto;

import com.example.mockstalk.domain.price.periodic_candles.entity.PeriodicCandleType;
import com.example.mockstalk.domain.price.periodic_candles.entity.PeriodicCandles;
import com.example.mockstalk.domain.stock.entity.Stock;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PeriodicCandleApiResponseDto {

    private String stck_bsop_date;
    private String stck_oprc;
    private String stck_clpr;
    private String stck_hgpr;
    private String stck_lwpr;
    private String acml_vol;

    public PeriodicCandles toEntity(PeriodicCandleType candleType, Stock stock) {

        return PeriodicCandles.builder()
            .candleType(candleType)
            .date(parseDateTime(this.getStck_bsop_date()))
            .openingPrice(parseLong(this.getStck_oprc()))
            .closingPrice(parseLong(this.getStck_clpr()))
            .highPrice(parseLong(this.getStck_hgpr()))
            .lowPrice(parseLong(this.getStck_lwpr()))
            .volume(parseLong(this.getAcml_vol()))
            .stock(stock)
            .build();
    }

    private static Long parseLong(String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (Exception e) {
            return 0L;
        }
    }

    private static LocalDateTime parseDateTime(String date) {
        if (date == null || date.isBlank()) {
            log.warn("기준일자가 없어 기본값(오늘)으로 대체합니다.");
            return LocalDate.now().atStartOfDay();
        }
        return LocalDateTime.parse(date + "000000", DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

}
