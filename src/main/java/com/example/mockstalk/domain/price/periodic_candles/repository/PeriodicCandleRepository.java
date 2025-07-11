package com.example.mockstalk.domain.price.periodic_candles.repository;

import com.example.mockstalk.domain.price.periodic_candles.entity.PeriodicCandleType;
import com.example.mockstalk.domain.price.periodic_candles.entity.PeriodicCandles;
import com.example.mockstalk.domain.stock.entity.Stock;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PeriodicCandleRepository extends JpaRepository<PeriodicCandles, Long> {

    List<PeriodicCandles> findByCandleTypeAndStock_StockName(PeriodicCandleType candleType,
        String stockName);

    @Modifying
    @Transactional
    @Query("DELETE FROM PeriodicCandles c WHERE c.candleType = :candleType AND c.date < :cutoffDate")
    void deleteOlderThan(@Param("candleType") PeriodicCandleType candleType,
        @Param("cutoffDate") LocalDateTime cutoffDate);

    List<PeriodicCandles> findByStockAndCandleTypeAndDateIn(Stock stock,
        PeriodicCandleType candleType, Collection<LocalDateTime> dates);


}
