package com.example.mockstalk.domain.price.periodic.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.mockstalk.domain.price.periodic_candles.service.PeriodicCandleApiService;
import com.example.mockstalk.domain.price.periodic_candles.service.PeriodicCandleInitializer;
import com.example.mockstalk.domain.stock.entity.Stock;
import com.example.mockstalk.domain.stock.repository.StockRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PeriodicCandleInitializerTest {

    @InjectMocks
    private PeriodicCandleInitializer periodicCandleInitializer;

    @Mock
    private PeriodicCandleApiService periodicCandleApiService;
    @Mock
    private StockRepository stockRepository;

    private Stock stock1;
    private Stock stock2;

    @BeforeEach
    void setUp() {
        stock1 = Stock.builder().id(1L).stockCode("005930").stockName("삼성전자").build();
        stock2 = Stock.builder().id(2L).stockCode("000660").stockName("SK하이닉스").build();
    }

    @Test
    @DisplayName("init()의 종목 배치화 및 맵 초기화 테스트")
    void init_success() {
        // given
        when(stockRepository.findAll()).thenReturn(List.of(stock1, stock2));

        // when
        periodicCandleInitializer.init();

        // then
        List<List<Stock>> stockBatches = (List<List<Stock>>) ReflectionTestUtils.getField(
            periodicCandleInitializer, "stockBatches");
        assertThat(stockBatches).isNotNull();
        assertThat(stockBatches.stream().flatMap(List::stream).toList()).containsExactly(stock1,
            stock2);

        List<String> candleTypes = (List<String>) ReflectionTestUtils.getField(
            periodicCandleInitializer, "candleTypes");
        assertThat(candleTypes).containsExactly("D", "W", "M", "Y");
    }

    @Test
    @DisplayName("etchAndSaveCandles 호출 테스트")
    void prefetchCandlesBatch_success() {
        // given
        when(stockRepository.findAll()).thenReturn(List.of(stock1, stock2));
        periodicCandleInitializer.init();

        // 배치 사이즈를 1로 세팅
        ReflectionTestUtils.setField(periodicCandleInitializer, "stockBatches",
            List.of(List.of(stock1), List.of(stock2)));
        ReflectionTestUtils.setField(periodicCandleInitializer, "currentBatchIndex", 0);
        ReflectionTestUtils.setField(periodicCandleInitializer, "currentCandleTypeIndex", 0);

        // when
        periodicCandleInitializer.prefetchCandlesBatch();

        // then
        verify(periodicCandleApiService, atLeastOnce()).fetchAndSaveCandles(any(Stock.class),
            eq("D"), anyString(), anyString());
    }

    @Test
    @DisplayName("getStart, getEnd 테스트")
    void getStartAndEnd_success() {
        // when
        String startD = periodicCandleInitializer.getStart("D");
        String startW = periodicCandleInitializer.getStart("W");
        String startM = periodicCandleInitializer.getStart("M");
        String startY = periodicCandleInitializer.getStart("Y");
        String end = periodicCandleInitializer.getEnd();

        // then
        assertThat(startD).isEqualTo(
            LocalDate.now().minusDays(30).format(DateTimeFormatter.BASIC_ISO_DATE));
        assertThat(startW).isEqualTo(
            LocalDate.now().minusMonths(3).format(DateTimeFormatter.BASIC_ISO_DATE));
        assertThat(startM).isEqualTo(
            LocalDate.now().minusYears(1).format(DateTimeFormatter.BASIC_ISO_DATE));
        assertThat(startY).isEqualTo("20000101");
        assertThat(end).isEqualTo(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
    }

    @Test
    @DisplayName("prefetchCandlesBatch에서 예외 발생 시 실패 시 테스트")
    void prefetchCandlesBatch_fail() {
        // given
        when(stockRepository.findAll()).thenReturn(List.of(stock1));
        periodicCandleInitializer.init();

        ReflectionTestUtils.setField(periodicCandleInitializer, "stockBatches",
            List.of(List.of(stock1)));
        ReflectionTestUtils.setField(periodicCandleInitializer, "currentBatchIndex", 0);
        ReflectionTestUtils.setField(periodicCandleInitializer, "currentCandleTypeIndex", 0);

        doThrow(new RuntimeException("저장 실패")).when(periodicCandleApiService)
            .fetchAndSaveCandles(any(Stock.class), anyString(), anyString(), anyString());

        // when
        periodicCandleInitializer.prefetchCandlesBatch();

        // then
        var failedStocksMap = (java.util.Map<String, List<Stock>>) ReflectionTestUtils.getField(
            periodicCandleInitializer, "failedStocksMap");
        assertThat(failedStocksMap.get("D")).contains(stock1);
    }
}