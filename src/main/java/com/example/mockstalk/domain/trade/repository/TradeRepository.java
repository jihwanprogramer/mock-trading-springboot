package com.example.mockstalk.domain.trade.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.mockstalk.domain.trade.entity.Trade;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long>, TradeRepositoryCustom {

}
