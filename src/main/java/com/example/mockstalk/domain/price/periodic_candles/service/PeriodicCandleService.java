package com.example.mockstalk.domain.price.periodic_candles.service;

import org.springframework.stereotype.Service;

import com.example.mockstalk.common.error.CustomRuntimeException;
import com.example.mockstalk.common.error.ExceptionCode;
import com.example.mockstalk.domain.price.periodic_candles.dto.PeriodicCandleResponseDto;
import com.example.mockstalk.domain.price.periodic_candles.entity.PeriodicCandleType;
import com.example.mockstalk.domain.price.periodic_candles.entity.PeriodicCandles;
import com.example.mockstalk.domain.price.periodic_candles.repository.PeriodicCandleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PeriodicCandleService {

	private final PeriodicCandleRepository candleRepository;

	public PeriodicCandleResponseDto findPeriodicCandle(String stockCode, String candleType) {

		PeriodicCandleType periodicCandleType = PeriodicCandleType.valueOf(candleType);

		PeriodicCandles periodicCandles = candleRepository.findByCandleTypeAndStock_StockCode(
			periodicCandleType, stockCode).orElseThrow(() -> new CustomRuntimeException(
			ExceptionCode.NOT_FOUND_CANDLE));

		return PeriodicCandleResponseDto.from(periodicCandles);

	}

}
