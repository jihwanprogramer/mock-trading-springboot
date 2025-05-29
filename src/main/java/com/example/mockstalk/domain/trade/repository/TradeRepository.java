package com.example.mockstalk.domain.trade.repository;

import com.example.mockstalk.domain.trade.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeRepository extends JpaRepository<Trade,Long> {
}
