package com.example.mockstalk.domain.interest_stock.dto.response;

import lombok.Getter;

@Getter
public class InterestResponseDto {

    private String stockName;

    private String stockCode;

    public InterestResponseDto(String stockName, String stockCode) {
        this.stockName = stockName;
        this.stockCode = stockCode;
    }
}
