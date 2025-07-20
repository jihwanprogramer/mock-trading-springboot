package com.example.mockstalk.domain.price.periodic_candles.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PeriodicCandleApiRequestDto {

    private String stockCode;

    private String period;

    private String startDate;

    private String endDate;

}
