package com.example.mockstalk.domain.price.periodic_candles.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.mockstalk.domain.price.periodic_candles.entity.PeriodicCandleType;
import com.example.mockstalk.domain.price.periodic_candles.entity.PeriodicCandles;

@Repository
public interface PeriodicCandleRepository extends JpaRepository<PeriodicCandles, Long> {

	Optional<PeriodicCandles> findByCandleTypeAndStock_StockCode(PeriodicCandleType candleType,
		String stockCode);
}
