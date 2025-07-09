package com.example.mockstalk.domain.price.periodic.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.mockstalk.domain.price.periodic_candles.dto.PeriodicCandleResponseDto;
import com.example.mockstalk.domain.price.periodic_candles.entity.PeriodicCandleType;
import com.example.mockstalk.domain.price.periodic_candles.entity.PeriodicCandles;
import com.example.mockstalk.domain.price.periodic_candles.repository.PeriodicCandleRepository;
import com.example.mockstalk.domain.price.periodic_candles.service.PeriodicCandleService;
import com.example.mockstalk.domain.stock.entity.Stock;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PeriodicCandleServiceTest {

    @InjectMocks
    private PeriodicCandleService periodicCandleService;

    @Mock
    private PeriodicCandleRepository candleRepository;

    @Test
    @DisplayName("saveCandlesAsync가 100개씩 잘 저장하는지 테스트")
    void saveCandlesAsync_batchSave() {
        // given
        List<PeriodicCandles> candles =
            java.util.stream.IntStream.range(0, 250)
                .mapToObj(i -> PeriodicCandles.builder()
                    .candleType(PeriodicCandleType.D)
                    .date(LocalDateTime.now().minusDays(i))
                    .closingPrice(1000L + i)
                    .build())
                .toList();

        // when
        periodicCandleService.saveCandlesAsync(candles);

        // then
        ArgumentCaptor<List<PeriodicCandles>> captor = ArgumentCaptor.forClass(List.class);
        verify(candleRepository, times(3)).saveAll(captor.capture());
        List<List<PeriodicCandles>> allBatches = captor.getAllValues();
        assertThat(allBatches.get(0)).hasSize(100);
        assertThat(allBatches.get(1)).hasSize(100);
        assertThat(allBatches.get(2)).hasSize(50);
    }

    @Test
    @DisplayName("findPeriodicCandle 테스트")
    void findPeriodicCandle_success() {
        // given
        String stockName = "삼성전자";
        String candleType = "D";
        Stock stock = Stock.builder()
            .stockCode("005930")
            .stockName(stockName)
            .build();
        List<PeriodicCandles> entityList = List.of(
            PeriodicCandles.builder()
                .stock(stock)
                .candleType(PeriodicCandleType.D)
                .date(LocalDateTime.now())
                .closingPrice(1000L)
                .build()
        );
        when(candleRepository.findByCandleTypeAndStock_StockName(PeriodicCandleType.D, stockName))
            .thenReturn(entityList);

        // when
        List<PeriodicCandleResponseDto> result = periodicCandleService.findPeriodicCandle(stockName,
            candleType);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getClosingPrice()).isEqualTo(1000L);
        verify(candleRepository, times(1)).findByCandleTypeAndStock_StockName(PeriodicCandleType.D,
            stockName);
    }
}
