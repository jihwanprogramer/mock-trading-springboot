package com.example.mockstalk.domain.account.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.mockstalk.common.error.CustomRuntimeException;
import com.example.mockstalk.common.error.ExceptionCode;
import com.example.mockstalk.domain.account.dto.AccountRequestDto;
import com.example.mockstalk.domain.account.dto.UpdateAccountRequestDto;
import com.example.mockstalk.domain.account.entity.Account;
import com.example.mockstalk.domain.account.repository.AccountRepository;
import com.example.mockstalk.domain.holdings.repository.HoldingsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

	private final AccountRepository accountRepository;

	private final HoldingsRepository holdingsRepository;

	@Transactional
	public void saveAccount(AccountRequestDto accountRequestDto) {
		Account account = Account.builder()
			.accountName(accountRequestDto.getAccountName())
			.password(accountRequestDto.getPassword())
			.initialBalance(accountRequestDto.getInitialBalance())
			.build();
		accountRepository.save(account);
	}

	@Transactional(readOnly = true)
	public void findAccountById(Long id) {

		Account account = accountRepository.findById(id).
			orElseThrow(() -> new CustomRuntimeException(ExceptionCode.NOT_FOUND_ACCOUNT));

	}

	@Transactional(readOnly = true)
	public void findAccount() {

	}

	public void findHoldingsById(Long id) {
		// 보유종목 리스트 조회
	}

	@Transactional
	public void updateAccountPassword(Long id, UpdateAccountRequestDto requestDto) {
		Account account = accountRepository.findById(id).
			orElseThrow(() -> new CustomRuntimeException(ExceptionCode.NOT_FOUND_ACCOUNT));

		account.updatePassword(requestDto.getPassword());

	}

	@Transactional
	public void deleteAccount(Long id) {
		Account account = accountRepository.findById(id).
			orElseThrow(() -> new CustomRuntimeException(ExceptionCode.NOT_FOUND_ACCOUNT));

		accountRepository.delete(account);
	}

}
