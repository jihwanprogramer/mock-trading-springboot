package com.example.mockstalk.domain.account.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mockstalk.common.response.ResponseMessage;
import com.example.mockstalk.domain.account.service.AccountService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
public class AccountController {

	private final AccountService accountService;

	/**
	 계좌 생성
	 **/
	@PostMapping
	public ResponseMessage<?> saveAccount() {

		return null;
	}

	/**
	 계좌 정보 가져오기. <- 해당 계좌로 로그인
	 **/
	@PostMapping("/signin")
	public ResponseMessage<?> signInAccount() {

		return null;
	}

	/**
	 계좌 단건 조회
	 **/
	@GetMapping("/{id}")
	public ResponseMessage<?> getAccount(@PathVariable Long id) {

		return null;
	}

	/**
	 계좌 다건 조회
	 **/
	@GetMapping
	public ResponseMessage<?> getAccounts() {

		return null;
	}

	/**
	 선택 계좌 보유 종목 조회
	 **/
	@GetMapping("/{id}/holdings")
	public ResponseMessage<?> getHoldings(@PathVariable Long id) {

		return null;
	}

	/**
	 선택 계좌 삭제
	 **/
	@DeleteMapping("/{id}")
	public ResponseMessage<?> deleteAccount(@PathVariable Long id) {

		return null;
	}

	/**
	 선택 계좌 비밀번호 변경
	 **/
	@PatchMapping("/{id}")
	public ResponseMessage<?> updateAccountPassword(@PathVariable Long id) {

		return null;
	}

}
