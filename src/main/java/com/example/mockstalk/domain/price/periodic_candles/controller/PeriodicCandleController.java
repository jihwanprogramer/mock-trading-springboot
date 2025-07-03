package com.example.mockstalk.domain.price.periodic_candles.controller;

import com.example.mockstalk.common.response.ResponseMessage;
import com.example.mockstalk.domain.price.periodic_candles.service.PeriodicCandleSchedule;
import com.example.mockstalk.domain.price.periodic_candles.service.PeriodicCandleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/period")
@RequiredArgsConstructor
public class PeriodicCandleController {

    private final PeriodicCandleService periodicCandleService;
    private final PeriodicCandleSchedule periodicCandleSchedule;

    @GetMapping("/{stockCode}/candle/{candle}")
    public ResponseEntity<ResponseMessage<?>> findPeriod(
        @PathVariable String stockCode,
        @PathVariable String candle //D,W,M,Y
    ) {

        return ResponseEntity.ok(
            ResponseMessage.success(periodicCandleService.findPeriodicCandle(stockCode, candle)));
    }


}
