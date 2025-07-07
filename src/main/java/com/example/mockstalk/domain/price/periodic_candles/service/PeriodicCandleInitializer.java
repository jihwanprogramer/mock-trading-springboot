package com.example.mockstalk.domain.price.periodic_candles.service;

import com.example.mockstalk.common.error.CustomRuntimeException;
import com.example.mockstalk.common.error.ExceptionCode;
import com.example.mockstalk.domain.stock.entity.Stock;
import com.example.mockstalk.domain.stock.repository.StockRepository;
import com.example.mockstalk.domain.stock.service.StockService;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PeriodicCandleInitializer {

    private final PeriodicCandleApiService periodicCandleApiService;
    private final StockRepository stockRepository;
    private final StockService stockService;

    private final int BATCH_SIZE = 100;
    private final List<String> candleTypes = List.of("D", "W", "M", "Y");

    private List<List<Stock>> stockBatches;
    private int currentBatchIndex = 0;
    private int currentCandleTypeIndex = 0;

    private final Map<String, List<Stock>> failedStocksMap = new HashMap<>();
    private final Map<String, Integer> successCountMap = new HashMap<>();

    @PostConstruct
    public void init() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("kospi_code.csv");
        stockService.saveStockCsv(is);
        List<Stock> allStocks = waitUntilStocksPersisted();
        stockBatches = splitIntoBatches(allStocks, BATCH_SIZE);
        candleTypes.forEach(type -> {
            failedStocksMap.put(type, new ArrayList<>());
            successCountMap.put(type, 0);
        });
        log.info("주식 총 종목 수: {}", allStocks.size());
    }

    @Scheduled(fixedDelay = 2 * 1000) // 2초 마다 실행
    public void prefetchCandlesBatch() {

        if (currentCandleTypeIndex >= candleTypes.size()) {
            log.info("작업 완료");
            return;
        }

        String candleType = candleTypes.get(currentCandleTypeIndex);
        log.info("현재 작업중인 봉 타입: {} / 배치 인덱스: {}", candleType, currentBatchIndex);

        // 먼저 실패한 종목 재시도
        if (!failedStocksMap.get(candleType).isEmpty()) {
            log.info("실패 종목 재시도 : {}", candleType);
            retryFailedStocks(candleType);
            return;
        }

        if (currentBatchIndex >= stockBatches.size()) {
            log.info("{} 완료 | 성공: {}, 실패: {}", candleType,
                successCountMap.get(candleType), failedStocksMap.get(candleType).size());
            currentBatchIndex = 0;
            currentCandleTypeIndex++;
            return;
        }

        List<Stock> currentBatch = stockBatches.get(currentBatchIndex);
        String start = getStart(candleType);
        String end = getEnd();

        int batchSuccess = 0;
        int batchFail = 0;

        for (Stock stock : currentBatch) {
            try {
                periodicCandleApiService.fetchAndSaveCandles(stock, candleType, start, end);
                successCountMap.put(candleType, successCountMap.get(candleType) + 1);
                batchSuccess++;
                Thread.sleep(700); // 0.7초 대기
            } catch (Exception e) {
                failedStocksMap.get(candleType).add(stock);
                batchFail++;
                log.error("저장 실패: {} ({}), {} - {}", stock.getStockCode(), candleType,
                    LocalDateTime.now(), e.getMessage());
            }
        }

        log.info("배치 완료: {} [{}] | 성공: {}, 실패: {}, 잔여 종목 수: {} | 완료 시간 : {}",
            candleType, currentBatchIndex, batchSuccess, batchFail,
            (stockBatches.size() - currentBatchIndex - 1) * BATCH_SIZE, LocalDateTime.now());

        currentBatchIndex++;
    }

    private void retryFailedStocks(String candleType) {
        List<Stock> failedList = new ArrayList<>(failedStocksMap.get(candleType));
        failedStocksMap.get(candleType).clear();

        String start = getStart(candleType);
        String end = getEnd();

        for (Stock stock : failedList) {
            try {
                periodicCandleApiService.fetchAndSaveCandles(stock, candleType, start, end);
                Thread.sleep(700);
            } catch (Exception e) {
                failedStocksMap.get(candleType).add(stock);
                log.error("저장 재실패: {} ({}), {} - {}", stock.getStockCode(), candleType,
                    LocalDateTime.now(), e.getMessage());
            }
        }
    }

    private List<List<Stock>> splitIntoBatches(List<Stock> list, int size) {
        List<List<Stock>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            batches.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return batches;
    }

    public String getStart(String type) {
        LocalDate today = LocalDate.now();
        switch (type) {
            case "D":
                return today.minusDays(30).format(DateTimeFormatter.BASIC_ISO_DATE);
            case "W":
                return today.minusMonths(3).format(DateTimeFormatter.BASIC_ISO_DATE);
            case "M":
                return today.minusYears(1).format(DateTimeFormatter.BASIC_ISO_DATE);
            case "Y":
                return "20000101";
            default:
                return today.format(DateTimeFormatter.BASIC_ISO_DATE);
        }
    }

    public String getEnd() {
        return LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    private List<Stock> waitUntilStocksPersisted() {
        List<Stock> stocks;
        int retry = 10;
        while (retry-- > 0) {
            stocks = stockRepository.findAll();
            if (!stocks.isEmpty()) {
                return stocks;
            }
            try {
                Thread.sleep(500); // 0.5초 대기
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        throw new CustomRuntimeException(ExceptionCode.NOT_READY_STOCK);
    }
}
