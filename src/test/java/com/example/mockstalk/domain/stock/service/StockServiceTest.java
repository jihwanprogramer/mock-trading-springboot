package com.example.mockstalk.domain.stock.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import com.example.mockstalk.domain.stock.entity.Stock;
import com.example.mockstalk.domain.stock.repository.StockRepository;

@SpringBootTest
@ActiveProfiles("test") // application-test.properties 사용
@Transactional
@Rollback(false)
class StockServiceTest {

	@Autowired
	private StockService stockService;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private EntityManager em;

	@Test
	void saveStockCsv_데이터_정상_입력() {
		// given
		String csv = "종목코드,표준코드,종목명,상장일자,상장폐지일자\n" +
			"000210,KR7000210005,DL,19760202,\n" +
			"000240,KR7000240002,한국앤컴퍼니,19760626,\n" +
			"000270,KR7000270009,기아,19730721,\n" +
			"001450,KR7001450007,현대해상,19860326,20211231";

		InputStream inputStream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

		// when
		stockService.saveStockCsv(inputStream);

		// then
		List<Stock> stocks = stockRepository.findAll();
		assertThat(stocks).hasSize(4);

		Stock last = stocks.get(3);
		assertThat(last.getStockName()).isEqualTo("현대해상");
		assertThat(last.getStockStatus().name()).isEqualTo("DELISTED");
		assertThat(last.getDelistedDate()).isNotNull();
	}
}

