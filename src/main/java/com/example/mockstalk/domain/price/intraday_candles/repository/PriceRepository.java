package com.example.mockstalk.domain.price.intraday_candles.repository;

import com.example.mockstalk.domain.price.intraday_candles.entity.Intraday_candle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceRepository extends JpaRepository<Intraday_candle,Long> {
}
