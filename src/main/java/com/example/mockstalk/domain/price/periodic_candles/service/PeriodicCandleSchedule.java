package com.example.mockstalk.domain.price.periodic_candles.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import com.example.mockstalk.domain.stock.entity.Stock;
import com.example.mockstalk.domain.stock.repository.StockRepository;

@Service
@RequiredArgsConstructor
public class PeriodicCandleSchedule {

	private final PeriodicCandleApiService periodicCandleApiService;
	private final StockRepository stockRepository;

	// @Scheduled(cron = "0 0 * * * *") // 매 정시
	public void scheduleCandleUpdate() {
		LocalTime now = LocalTime.now();

		if (now.isAfter(LocalTime.of(16, 30)) || now.isBefore(LocalTime.of(8, 0))) {
			updateCandlesBatch();
		}
	}

	public void updateCandlesBatch() {
		List<String> allCodes = loadKospiStockCodes("src/main/resources/kospi_code.csv");
		List<List<String>> batches = splitIntoBatches(allCodes, 100);

		int batchIndex = getCurrentBatchIndex(); // 시간에 따라 인덱스
		if (batchIndex < 0 || batchIndex >= batches.size()) {
			return;
		}

		List<String> currentBatch = batches.get(batchIndex);

		for (String code : currentBatch) {
			Stock stock = stockRepository.findByStockCode(code);
			if (stock == null) {
				continue;
			}

			periodicCandleApiService.fetchAndSaveCandles(code, "D", getStart("D"), getEnd());
			periodicCandleApiService.fetchAndSaveCandles(code, "W", getStart("W"), getEnd());
			periodicCandleApiService.fetchAndSaveCandles(code, "M", getStart("M"), getEnd());
			periodicCandleApiService.fetchAndSaveCandles(code, "Y", getStart("Y"), getEnd());
		}
	}

	public List<List<String>> splitIntoBatches(List<String> list, int size) {
		List<List<String>> batches = new ArrayList<>();
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

	public List<String> loadKospiStockCodes(String filePath) {
		List<String> codes = new ArrayList<>();
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
				String code = String.format("%06d", Integer.parseInt(cols[0].trim()));
				codes.add(code);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return codes;
	}

}
