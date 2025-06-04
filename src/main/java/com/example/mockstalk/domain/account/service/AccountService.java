package com.example.mockstalk.domain.account.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.mockstalk.common.error.CustomRuntimeException;
import com.example.mockstalk.common.error.ExceptionCode;
import com.example.mockstalk.domain.account.dto.AccountRequestDto;
import com.example.mockstalk.domain.account.dto.AccountResponseDto;
import com.example.mockstalk.domain.account.dto.HoldingsResponseDto;
import com.example.mockstalk.domain.account.dto.UpdateAccountRequestDto;
import com.example.mockstalk.domain.account.entity.Account;
import com.example.mockstalk.domain.account.repository.AccountRepository;
import com.example.mockstalk.domain.holdings.entity.Holdings;
import com.example.mockstalk.domain.holdings.repository.HoldingsRepository;
import com.example.mockstalk.domain.user.entity.User;
import com.example.mockstalk.domain.user.repository.UserRepository;
import com.example.mockstalk.domain.user.service.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

	private final AccountRepository accountRepository;

	private final UserRepository userRepository;

	private final HoldingsRepository holdingsRepository;

	// 계좌 생성
	@Transactional
	public void saveAccount(AccountRequestDto accountRequestDto,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		User user = userDetails.getUser();

		Account account = Account.builder()
			.accountName(accountRequestDto.getAccountName())
			.password(accountRequestDto.getPassword())
			.initialBalance(accountRequestDto.getInitialBalance())
			.user(user)
			.build();
		accountRepository.save(account);
	}

	// 계좌 조회(단건)
	@Transactional(readOnly = true)
	public AccountResponseDto findAccountById(Long id) {

		Account account = accountRepository.findById(id).
			orElseThrow(() -> new CustomRuntimeException(ExceptionCode.ACCOUNT_NOT_FOUND));

		// 접근 권한 <- 로그인 한 유저가 계좌 생성한 사람과 동일한 지 체크하는 로직

		return AccountResponseDto.of(account);
	}

	// 계좌 조회(다건)
	@Transactional(readOnly = true)
	public List<AccountResponseDto> findAccount(@AuthenticationPrincipal CustomUserDetails userDetails) {

		Long userId = userDetails.getId();

		List<Account> accounts = accountRepository.findAllByUser_Id(userId);

		return accounts.stream()
			.map(AccountResponseDto::of)
			.collect(Collectors.toList());
	}

	// 보유종목 리스트 조회
	@Transactional(readOnly = true)
	public List<HoldingsResponseDto> findHoldingsById(Long id) {

		List<Holdings> holdings = holdingsRepository.findAllByAccount_Id(id);

		return holdings.stream()
			.map(HoldingsResponseDto::of)
			.collect(Collectors.toList());
	}

	// 계좌 비밀번호 변경
	@Transactional
	public void updateAccountPassword(Long id, UpdateAccountRequestDto requestDto) {
		Account account = accountRepository.findById(id).
			orElseThrow(() -> new CustomRuntimeException(ExceptionCode.ACCOUNT_NOT_FOUND));

		account.updatePassword(requestDto.getPassword());

	}

	// 계좌 삭제
	@Transactional
	public void deleteAccount(Long id) {
		Account account = accountRepository.findById(id).
			orElseThrow(() -> new CustomRuntimeException(ExceptionCode.ACCOUNT_NOT_FOUND));

		accountRepository.delete(account);
	}

}
