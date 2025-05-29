package com.example.mockstalk.domain.holdings.repository;

import com.example.mockstalk.domain.holdings.entity.Holdings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HoldingsRepository extends JpaRepository<Holdings,Long> {
}
