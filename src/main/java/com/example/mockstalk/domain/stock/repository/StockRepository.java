package com.example.mockstalk.domain.stock.repository;

import com.example.mockstalk.domain.stock.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock,Long> {

    Optional<Stock>findByStockNameAndStockCode(String stockName, String stockCode);
    Stock findByStockCode(String stockCode);
}
