package com.example.mockstalk.domain.price.periodic.service;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.mockstalk.domain.price.periodic_candles.entity.PeriodicCandleType;
import com.example.mockstalk.domain.price.periodic_candles.repository.PeriodicCandleRepository;
import com.example.mockstalk.domain.price.periodic_candles.service.PeriodicCandleApiService;
import com.example.mockstalk.domain.price.periodic_candles.service.PeriodicCandleSchedule;
import com.example.mockstalk.domain.stock.entity.Stock;
import com.example.mockstalk.domain.stock.repository.StockRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PeriodicCandleScheduleTest {


    @InjectMocks
    private PeriodicCandleSchedule schedule;

    @Mock
    private PeriodicCandleApiService candleApiService;
    @Mock
    private PeriodicCandleRepository candleRepository;
    @Mock
    private StockRepository stockRepository;

    @Test
    @DisplayName("batch 업데이트 성공")
    void updateCandlesBatch_success() throws Exception {
        // given
        Stock stock = Stock.builder().stockCode("005930").build();
        when(stockRepository.findAll()).thenReturn(List.of(stock));

        // 날짜 조건 조작
        LocalDate now = LocalDate.of(2025, 6, 23); // 월요일
        LocalTime fakeTime = LocalTime.of(17, 0); // 배치 index 0
        try (MockedStatic<LocalDate> localDateMock = mockStatic(LocalDate.class);
            MockedStatic<LocalTime> localTimeMock = mockStatic(LocalTime.class)) {

            localDateMock.when(LocalDate::now).thenReturn(now);
            localTimeMock.when(LocalTime::now).thenReturn(fakeTime);

            // when
            schedule.updateCandlesBatch();

            // then
            verify(candleApiService, times(1)).fetchAndSaveCandles(eq(stock), eq("D"), anyString(),
                anyString());
        }
    }

    @Test
    @DisplayName("캔들 삭제 정상 처리")
    void cleanupOldCandles_deletesOldData() {
        // given
        LocalDateTime now = LocalDateTime.of(2025, 6, 23, 3, 0);
        try (MockedStatic<LocalDateTime> localDateTimeMock = mockStatic(LocalDateTime.class)) {
            localDateTimeMock.when(LocalDateTime::now).thenReturn(now);

            // when
            schedule.cleanupOldCandles();

            // then
            verify(candleRepository).deleteOlderThan(eq(PeriodicCandleType.D), any());
            verify(candleRepository).deleteOlderThan(eq(PeriodicCandleType.W), any());
            verify(candleRepository).deleteOlderThan(eq(PeriodicCandleType.M), any());
            verify(candleRepository, never()).deleteOlderThan(eq(PeriodicCandleType.Y), any());
        }
    }
}
