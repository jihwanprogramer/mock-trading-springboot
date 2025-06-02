package com.example.mockstalk.domain.holdings.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.mockstalk.domain.account.entity.Accounts;
import com.example.mockstalk.domain.holdings.entity.Holdings;
import com.example.mockstalk.domain.stock.entity.Stock;

@Repository
public interface HoldingsRepository extends JpaRepository<Holdings, Long> {

	//매도를 위한 메소드(임시 설명)
	Optional<Holdings> findByAccountsAndStock(Accounts accounts, Stock stock);

}
