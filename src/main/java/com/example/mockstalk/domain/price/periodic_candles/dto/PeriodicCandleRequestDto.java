package com.example.mockstalk.domain.price.periodic_candles.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PeriodicCandleRequestDto {

    private String stockCode;

    private String candleType;

    private LocalDate startDate;

    private LocalDate endDate;

}
