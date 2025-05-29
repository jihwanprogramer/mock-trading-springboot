package com.example.mockstalk.domain.interest_stock.repository;

import com.example.mockstalk.domain.interest_stock.entity.InterestStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterestStockRepository extends JpaRepository<InterestStock,Long> {
}
