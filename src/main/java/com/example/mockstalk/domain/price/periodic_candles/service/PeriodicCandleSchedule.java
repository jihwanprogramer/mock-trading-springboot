package com.example.mockstalk.domain.price.periodic_candles.service;

import com.example.mockstalk.domain.stock.entity.Stock;
import com.example.mockstalk.domain.stock.repository.StockRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PeriodicCandleSchedule {

    private final PeriodicCandleApiService periodicCandleApiService;
    private final StockRepository stockRepository;


    @Scheduled(cron = "0 0 * * * *") // 매 정시
    public void scheduleCandleUpdate() {
        LocalTime now = LocalTime.now();

        if (now.isAfter(LocalTime.of(16, 30)) || now.isBefore(LocalTime.of(8, 0))) {
            updateCandlesBatch();
        }
    }

    //필요한 시기에맨 정보 갱신
    public void updateCandlesBatch() {
        List<Stock> allStocks = stockRepository.findAll();
        List<List<Stock>> batches = splitIntoBatches(allStocks, 100);

        int batchIndex = getCurrentBatchIndex();
        if (batchIndex < 0 || batchIndex >= batches.size()) {
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

            if (candleTypes.contains("D")) {
                periodicCandleApiService.fetchAndSaveCandles(stock, "D", getStart("D"), getEnd());
            }
            if (candleTypes.contains("W")) {
                periodicCandleApiService.fetchAndSaveCandles(stock, "W", getStart("W"), getEnd());
            }
            if (candleTypes.contains("M")) {
                periodicCandleApiService.fetchAndSaveCandles(stock, "M", getStart("M"), getEnd());
            }
            if (candleTypes.contains("Y")) {
                periodicCandleApiService.fetchAndSaveCandles(stock, "Y", getStart("Y"), getEnd());
            }
        }
    }

    /*
    //DB에서 정보를 가져오는 방식 + 매일 갱신
    public void updateCandlesBatch() {
        List<Stock> allStocks = stockRepository.findAll();
        List<List<Stock>> batches = splitIntoBatches(allStocks, 100);

        int batchIndex = getCurrentBatchIndex();
        if (batchIndex < 0 || batchIndex >= batches.size()) {
            return;
        }

        List<Stock> currentBatch = batches.get(batchIndex);
        for (Stock stock : currentBatch) {

            periodicCandleApiService.fetchAndSaveCandles(stock, "D", getStart("D"), getEnd());
            periodicCandleApiService.fetchAndSaveCandles(stock, "W", getStart("W"), getEnd());
            periodicCandleApiService.fetchAndSaveCandles(stock, "M", getStart("M"), getEnd());
            periodicCandleApiService.fetchAndSaveCandles(stock, "Y", getStart("Y"), getEnd());
        }
    }

     */

    /*
    // csv 에서 가져온 정보로 하는 방식
    public void updateCandlesBatch() {
        List<StockCsvDto> allStocks = loadKospiStockDtos();
        List<List<StockCsvDto>> batches = splitIntoBatches(allStocks, 100);

        int batchIndex = getCurrentBatchIndex(); // 시간에 따라 인덱스
        if (batchIndex < 0 || batchIndex >= batches.size()) {
            return;
        }

        List<StockCsvDto> currentBatch = batches.get(batchIndex);

        for (StockCsvDto dto : currentBatch) {
            Stock stock = stockRepository.findByStockCode(dto.getStockCode());
			if (stock == null) {
				continue;
			}

            periodicCandleApiService.fetchAndSaveCandles(dto.getStockCode(), "D", getStart("D"),
                getEnd());
            periodicCandleApiService.fetchAndSaveCandles(dto.getStockCode(), "W", getStart("W"),
                getEnd());
            periodicCandleApiService.fetchAndSaveCandles(dto.getStockCode(), "M", getStart("M"),
                getEnd());
            periodicCandleApiService.fetchAndSaveCandles(dto.getStockCode(), "Y", getStart("Y"),
                getEnd());
        }
    }


     */

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
        return LocalDate.now().minusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE);
    }

/*
    //csv 에서 정보 가져오는 방식
    public List<StockCsvDto> loadKospiStockDtos() {
        List<StockCsvDto> stockList = new ArrayList<>();
        try (
            InputStream inputStream = new ClassPathResource(
                "kospi_stock_list.csv").getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))
        ) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                String[] cols = line.split(",");
                String code = String.format("%06d", Integer.parseInt(cols[0].trim())); // 주식 코드
                String name = cols[2].trim(); // 주식 이름
                stockList.add(new StockCsvDto(code, name));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stockList;
    }


 */

}
