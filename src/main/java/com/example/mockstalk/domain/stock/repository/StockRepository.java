package com.example.mockstalk.domain.stock.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.mockstalk.domain.stock.entity.Stock;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

	Optional<Stock> findByStockNameAndStockCode(String stockName, String stockCode);

	Stock findByStockCode(String stockCode);
}
