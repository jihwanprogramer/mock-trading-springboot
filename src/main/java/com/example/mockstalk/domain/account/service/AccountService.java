package com.example.mockstalk.domain.account.service;

import org.springframework.stereotype.Service;

import com.example.mockstalk.domain.account.repository.AccountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

	private final AccountRepository accountRepository;

	public void saveAccount() {

	}

}
