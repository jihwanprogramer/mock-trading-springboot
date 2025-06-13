package com.example.mockstalk.domain.price.periodic_candles.service;

import com.example.mockstalk.domain.price.periodic_candles.entity.PeriodicCandleType;
import com.example.mockstalk.domain.price.periodic_candles.repository.PeriodicCandleRepository;
import com.example.mockstalk.domain.stock.entity.Stock;
import com.example.mockstalk.domain.stock.repository.StockRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PeriodicCandleSchedule {

    private final PeriodicCandleApiService periodicCandleApiService;
    private final PeriodicCandleRepository candleRepository;
    private final StockRepository stockRepository;

    private final Map<String, List<String>> failedMap = new HashMap<>();

    @Scheduled(cron = "0 0 * * * *")
    public void scheduleCandleUpdate() {
        LocalTime now = LocalTime.now();

        if (now.isAfter(LocalTime.of(16, 30)) || now.isBefore(LocalTime.of(8, 0))) {
            updateCandlesBatch();
        }
    }

    // 매일 새벽 3시에 보관 기간이 지난 정보 삭제
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupOldCandles() {
        LocalDateTime now = LocalDateTime.now();

        // 일봉: 30일 초과
        LocalDateTime dailyCutoff = now.minusDays(30);
        candleRepository.deleteOlderThan(PeriodicCandleType.D, dailyCutoff);

        // 주봉: 3개월 초과
        LocalDateTime weeklyCutoff = now.minusMonths(3);
        candleRepository.deleteOlderThan(PeriodicCandleType.W, weeklyCutoff);

        // 월봉: 1년 초과
        LocalDateTime monthlyCutoff = now.minusYears(1);
        candleRepository.deleteOlderThan(PeriodicCandleType.M, monthlyCutoff);

        // 연봉: 보존 (삭제하지 않음)

        log.info("유효기간 이후 봉 데이터 정리 완료");
    }

    //필요한 시기에맨 정보 갱신
    public void updateCandlesBatch() {
        List<Stock> allStocks = stockRepository.findAll();
        List<List<Stock>> batches = splitIntoBatches(allStocks, 100);

        int successCount = 0;
        int failCount = 0;

        int batchIndex = getCurrentBatchIndex();
        if (batchIndex < 0 || batchIndex >= batches.size()) {
            log.info("현재 시간에는 처리할 배치가 없습니다. (index: {})", batchIndex);
            return;
        }

        List<Stock> currentBatch = batches.get(batchIndex);

        Set<String> candleTypes = new HashSet<>();
        candleTypes.add("D");

        LocalDate today = LocalDate.now();
        if (today.getDayOfWeek() == DayOfWeek.MONDAY) {
            candleTypes.add("W");
        }
        if (today.getDayOfMonth() == 1) {
            candleTypes.add("M");
        }
        if (today.getMonth() == Month.JANUARY && today.getDayOfMonth() == 1) {
            candleTypes.add("Y");
        }

        for (Stock stock : currentBatch) {
            for (String type : candleTypes) {
                try {
                    periodicCandleApiService.fetchAndSaveCandles(stock, type, getStart(type),
                        getEnd());
                    successCount++;
                    Thread.sleep(700);
                } catch (Exception e) {
                    failedMap.get(type).add(stock.getStockCode());
                    failCount++;
                    log.warn("저장 실패: {} ({}) 이유: {}", stock.getStockCode(), type, e.getMessage());
                }
            }
        }

        log.info("완료된 배치: {} / {} | 성공: {}건, 실패: {}건 | 완료 시간 : {}", batchIndex + 1, batches.size(),
            successCount, failCount, LocalDateTime.now());
        failedMap.forEach((type, list) -> {
            if (!list.isEmpty()) {
                log.warn("실패 종목: {} ({}) | 실패 시간 : {}", list, type, LocalDateTime.now());
            }
        });
    }

    public List<List<Stock>> splitIntoBatches(List<Stock> list, int size) {
        List<List<Stock>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            batches.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return batches;
    }

    private int getCurrentBatchIndex() {
        int hour = LocalTime.now().getHour();
        if (hour >= 17) {
            return hour - 17;
        } else if (hour < 8) {
            return hour + 7;
        }
        return -1;
    }

    public String getStart(String type) {
        LocalDate today = LocalDate.now();
        switch (type) {
            case "D":
                return today.minusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE); // 어제
            case "W":
                return today.minusWeeks(1).format(DateTimeFormatter.BASIC_ISO_DATE); // 지난주
            case "M":
                return today.minusMonths(1).format(DateTimeFormatter.BASIC_ISO_DATE); // 지난달
            case "Y":
                return today.minusYears(1).format(DateTimeFormatter.BASIC_ISO_DATE); // 작년
            default:
                return today.format(DateTimeFormatter.BASIC_ISO_DATE);
        }
    }


    public String getEnd() {
        return LocalDate.now().minusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE);
    }


}
