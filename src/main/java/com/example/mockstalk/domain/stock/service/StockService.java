package com.example.mockstalk.domain.stock.service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import com.example.mockstalk.common.error.CustomRuntimeException;
import com.example.mockstalk.common.error.ExceptionCode;
import com.example.mockstalk.domain.stock.entity.Stock;
import com.example.mockstalk.domain.stock.entity.StockStatus;
import com.example.mockstalk.domain.stock.repository.StockRepository;

@Service
@RequiredArgsConstructor
public class StockService {
	@PersistenceContext
	private EntityManager em;

	private final StockRepository stockRepository;

	private static final int BATCH_SIZE = 1000;

	@Transactional
	public void saveStockCsv(String filePath) {
		em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
		em.createNativeQuery("TRUNCATE TABLE stock").executeUpdate();
		em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate(); //db 초기화 id값도 1로처음부터
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			String line;
			boolean isFirst = true;
			int count = 0;

			while ((line = br.readLine()) != null) {
				if (isFirst) {
					isFirst = false;
					continue;
				}
				String[] tokens = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
				for (int i = 0; i < tokens.length; i++) {
					tokens[i] = tokens[i].trim().replaceAll("^\"|\"$", "");
				}

				Stock stock = Stock.builder()
					.stockName(tokens[0])
					.stockCode(tokens[1])
					.listedDate(LocalDate.parse(tokens[2]))
					.delistedDate(tokens.length > 3 && !tokens[3].isEmpty() ? LocalDate.parse(tokens[3]) : null)
					.stockStatus(tokens.length > 3 && !tokens[3].isEmpty() ? StockStatus.DELISTED : StockStatus.ACTIVE)
					.build();

				em.persist(stock);  //영속성 컨텍스트에 1차저장
				count++;
				if (count % BATCH_SIZE == 0) {
					em.flush();   // 쿼리 날림
					em.clear();   // 영속성 컨텍스트 초기화 (메모리 누수 방지)
				}
			}
			em.flush(); //1000개 미만으로 남은 데이터들 처리
			em.clear();
		} catch (FileNotFoundException e) {
			throw new CustomRuntimeException(ExceptionCode.CSV_FILE_NOT_FOUND);
		} catch (IOException e) {
			throw new CustomRuntimeException(ExceptionCode.CSV_FILE_READ_FAILED);
		}
	}
}
