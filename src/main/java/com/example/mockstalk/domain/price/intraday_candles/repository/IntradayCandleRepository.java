package com.example.mockstalk.domain.price.intraday_candles.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.mockstalk.domain.price.intraday_candles.entity.CandleType;
import com.example.mockstalk.domain.price.intraday_candles.entity.IntradayCandle;

@Repository
public interface IntradayCandleRepository extends JpaRepository<IntradayCandle, Long> {

	boolean existsByStock_StockCodeAndTimeStamp(String stockCode, LocalDateTime timestamp);

	List<IntradayCandle> findByStock_StockCodeAndCandleTypeAndTimeStampBetween(String stockCode, CandleType type,
		LocalDateTime start, LocalDateTime end);

	List<IntradayCandle> findByStock_StockNameAndCandleTypeAndTimeStampBetween(
		String stockName, CandleType type, LocalDateTime start, LocalDateTime end
	);

	Optional<IntradayCandle> findByStock_StockCodeAndTimeStamp(String stockCode, LocalDateTime timestamp);
}

