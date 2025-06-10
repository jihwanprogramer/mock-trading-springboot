package com.example.mockstalk.domain.price.intraday_candles.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.mockstalk.domain.price.intraday_candles.entity.IntradayCandle;

@Repository
public interface IntradayCandleRepository extends JpaRepository<IntradayCandle, Long> {

	List<IntradayCandle> findByStockCodeAndTimeStampBetween(String stockCode, LocalDateTime start, LocalDateTime end);

	boolean existsByStockCodeAndTimeStamp(String stockCode, LocalDateTime timestamp);

	List<IntradayCandle> stockCode(String stockCode);
}

