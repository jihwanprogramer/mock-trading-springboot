package com.example.mockstalk.domain.price.periodic_candles.service;

import com.example.mockstalk.domain.price.periodic_candles.dto.PeriodicCandleResponseDto;
import com.example.mockstalk.domain.price.periodic_candles.entity.PeriodicCandleType;
import com.example.mockstalk.domain.price.periodic_candles.entity.PeriodicCandles;
import com.example.mockstalk.domain.price.periodic_candles.repository.PeriodicCandleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PeriodicCandleService {

    private final PeriodicCandleRepository candleRepository;

    @Async
    @Transactional
    public void saveCandlesAsync(List<PeriodicCandles> candles) {
        int batchSize = 100;
        for (int i = 0; i < candles.size(); i += batchSize) {
            int toIndex = Math.min(i + batchSize, candles.size());
            List<PeriodicCandles> batch = candles.subList(i, toIndex);
            candleRepository.saveAll(batch);
        }
    }

    public List<PeriodicCandleResponseDto> findPeriodicCandle(String stockCode, String candleType) {

        PeriodicCandleType periodicCandleType = PeriodicCandleType.valueOf(candleType);

        List<PeriodicCandles> periodicCandlesList = candleRepository.findByCandleTypeAndStock_StockCode(
            periodicCandleType, stockCode);

        return periodicCandlesList.stream().map(PeriodicCandleResponseDto::from).toList();

    }


}
