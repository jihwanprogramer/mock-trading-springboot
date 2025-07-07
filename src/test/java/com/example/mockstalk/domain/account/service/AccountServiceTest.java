package com.example.mockstalk.domain.account.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.mockstalk.common.config.AccountJwtUtil;
import com.example.mockstalk.common.error.CustomRuntimeException;
import com.example.mockstalk.domain.account.dto.AccountResponseDto;
import com.example.mockstalk.domain.account.dto.AccountSignInRequestDto;
import com.example.mockstalk.domain.account.dto.HoldingsResponseDto;
import com.example.mockstalk.domain.account.entity.Account;
import com.example.mockstalk.domain.account.repository.AccountRepository;
import com.example.mockstalk.domain.holdings.entity.Holdings;
import com.example.mockstalk.domain.holdings.repository.HoldingsRepository;
import com.example.mockstalk.domain.stock.entity.Stock;
import com.example.mockstalk.domain.stock.entity.StockStatus;
import com.example.mockstalk.domain.user.entity.User;
import com.example.mockstalk.domain.user.service.CustomUserDetails;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

	@Mock(lenient = true) // UnnecessaryStubbingException 회피
	private AccountRepository accountRepository;

	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private AccountJwtUtil accountJwtUtil;
	@Mock
	private RedisTemplate<String, Object> redisTemplate;
	@Mock
	private HoldingsRepository holdingsRepository;

	@InjectMocks
	private AccountService accountService;

	private User user;
	private Account account;
	private Stock stock;
	private Holdings holdings;
	private CustomUserDetails userDetails;
	private AccountSignInRequestDto signInDto;
	private ValueOperations<String, Object> valueOperations;

	@BeforeEach
	void setUp() {
		user = User.builder()
			.id(1L)
			.email("test@example.com")
			.nickname("tester")
			.build();

		account = Account.builder()
			.id(10L)
			.accountName("계좌1")
			.password("encodedPassword")
			.initialBalance(new BigDecimal("1000000"))
			.currentBalance(new BigDecimal("1000000"))
			.user(user)
			.build();

		stock = Stock.builder()
			.id(1L)
			.stockName("삼성전자")
			.stockCode("005930")
			.listedDate(LocalDate.of(2000, 1, 1))
			.stockStatus(StockStatus.ACTIVE)
			.build();

		holdings = Holdings.builder()
			.id(100L)
			.account(account)
			.stock(stock)
			.quantity(10L)
			.averagePrice(new BigDecimal("9500")) // ✅ 필수: NPE 방지
			.build();

		userDetails = new CustomUserDetails(user);
		signInDto = new AccountSignInRequestDto(account.getId(), "rawPassword");

		valueOperations = mock(ValueOperations.class);
		lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		lenient().when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
	}

	@Test
	void loginAccount_OK() {
		when(passwordEncoder.matches("rawPassword", account.getPassword())).thenReturn(true);
		when(accountJwtUtil.createAccountToken(account.getId())).thenReturn("mock.token");

		String result = accountService.loginAccount(signInDto, userDetails);

		assertEquals("mock.token", result);
	}

	@Test
	void loginAccount_False() {
		when(passwordEncoder.matches("rawPassword", account.getPassword())).thenReturn(false);

		assertThrows(CustomRuntimeException.class, () ->
			accountService.loginAccount(signInDto, userDetails)
		);
	}

	@Test
	void findAccountById_OK() {
		when(valueOperations.get("accountAsset::" + account.getId()))
			.thenReturn("2000000");
		when(valueOperations.get("accountProfitRate::" + account.getId()))
			.thenReturn("20.0");

		AccountResponseDto result = accountService.findAccountById(account.getId());

		assertEquals(account.getId(), result.getId());
		assertEquals(new BigDecimal("2000000"), result.getTotalAsset());
		assertEquals(new BigDecimal("20.0"), result.getProfitRate());
	}

	@Test
	void getProfitRateWithCache_NOCACHE() {
		when(valueOperations.get("accountProfitRate::" + account.getId())).thenReturn(null);
		when(holdingsRepository.findByAccountId(account.getId())).thenReturn(Collections.emptyList());

		BigDecimal result = accountService.getProfitRateWithCache(account.getId());

		assertEquals(0, result.compareTo(BigDecimal.ZERO)); //
	}

	@Test
	void calculateTotalAsset_OK() {
		when(holdingsRepository.findByAccountId(account.getId())).thenReturn(List.of(holdings));
		when(valueOperations.get("stockPrice::005930")).thenReturn("10000");

		BigDecimal result = accountService.calculateTotalAsset(account.getId());

		assertEquals(new BigDecimal("1100000"), result); // 1000000 + (10 * 10000)
	}

	@Test
	void findHoldingsById_OK() {
		when(holdingsRepository.findAllByAccount_Id(account.getId())).thenReturn(List.of(holdings));
		when(valueOperations.get("stockPrice::005930")).thenReturn("10000");

		List<HoldingsResponseDto> result = accountService.findHoldingsById(account.getId());

		assertEquals(1, result.size());
		assertEquals(new BigDecimal("10000"), result.get(0).getCurrentPrice());
	}
}