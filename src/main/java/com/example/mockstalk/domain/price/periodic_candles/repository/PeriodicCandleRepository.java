package com.example.mockstalk.domain.price.periodic_candles.repository;

import com.example.mockstalk.domain.price.periodic_candles.entity.PeriodicCandleType;
import com.example.mockstalk.domain.price.periodic_candles.entity.PeriodicCandles;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PeriodicCandleRepository extends JpaRepository<PeriodicCandles, Long> {

    Optional<PeriodicCandles> findByCandleTypeAndStockCode(PeriodicCandleType candleType,
        String stockCode);
}
