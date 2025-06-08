package com.example.mockstalk.domain.price.periodic_candles.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StockCsvDto {

    private String stockCode;
    private String stockName;
}
