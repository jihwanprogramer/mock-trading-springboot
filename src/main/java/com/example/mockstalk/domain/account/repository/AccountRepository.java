package com.example.mockstalk.domain.account.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.mockstalk.domain.account.entity.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

	List<Account> findAllByUser_Id(Long userId);

	BigDecimal findInitialBalanceByUserId(Long userId);

	@Query("SELECT DISTINCT a.user.id FROM Account a")
	List<Long> findAllUserIds();
}
