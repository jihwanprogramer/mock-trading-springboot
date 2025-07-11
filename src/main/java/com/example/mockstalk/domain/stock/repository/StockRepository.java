package com.example.mockstalk.domain.stock.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.mockstalk.domain.stock.entity.Stock;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

	Optional<Stock> findByStockNameAndStockCode(String stockName, String stockCode);

	Stock findByStockCode(String stockCode);

	Optional<Stock> findByStockName(String stockName);

	@Query("SELECT s.stockCode FROM Stock s")
		// code만 조회
	List<String> findAllStockCodes();

	@Query(value = "SELECT EXISTS (SELECT 1 FROM user LIMIT 1)", nativeQuery = true)
	boolean existsAnyNative();
}
