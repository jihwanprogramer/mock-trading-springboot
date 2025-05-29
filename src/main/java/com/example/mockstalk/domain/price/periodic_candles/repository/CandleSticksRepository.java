package com.example.mockstalk.domain.price.periodic_candles.repository;

import com.example.mockstalk.domain.price.periodic_candles.entity.Periodic_candles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandleSticksRepository extends JpaRepository<Periodic_candles,Long> {
}
