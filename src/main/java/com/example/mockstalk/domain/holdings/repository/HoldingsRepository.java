package com.example.mockstalk.domain.holdings.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.mockstalk.domain.account.entity.Account;
import com.example.mockstalk.domain.holdings.entity.Holdings;
import com.example.mockstalk.domain.stock.entity.Stock;

@Repository
public interface HoldingsRepository extends JpaRepository<Holdings, Long> {

	List<Holdings> findAllByAccount_Id(Long id);

	//매도를 위한 메소드(임시 설명)
	Optional<Holdings> findByAccountAndStock(Account accounts, Stock stock);

}
