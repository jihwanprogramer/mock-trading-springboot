package com.example.mockstalk.domain.holdings.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.mockstalk.domain.holdings.entity.Holdings;

@Repository
public interface HoldingsRepository extends JpaRepository<Holdings, Long> {
	List<Holdings> findAllByAccount_Id(Long id);
}
