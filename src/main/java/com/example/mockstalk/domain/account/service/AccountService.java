package com.example.mockstalk.domain.account.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.mockstalk.common.config.AccountContextHolder;
import com.example.mockstalk.common.config.AccountJwtUtil;
import com.example.mockstalk.common.error.CustomRuntimeException;
import com.example.mockstalk.common.error.ExceptionCode;
import com.example.mockstalk.domain.account.dto.AccountRequestDto;
import com.example.mockstalk.domain.account.dto.AccountResponseDto;
import com.example.mockstalk.domain.account.dto.AccountSignInRequestDto;
import com.example.mockstalk.domain.account.dto.HoldingsResponseDto;
import com.example.mockstalk.domain.account.dto.UpdateAccountRequestDto;
import com.example.mockstalk.domain.account.entity.Account;
import com.example.mockstalk.domain.account.repository.AccountRepository;
import com.example.mockstalk.domain.holdings.entity.Holdings;
import com.example.mockstalk.domain.holdings.repository.HoldingsRepository;
import com.example.mockstalk.domain.user.entity.User;
import com.example.mockstalk.domain.user.service.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

	private final AccountRepository accountRepository;

	private final PasswordEncoder passwordEncoder;
	private final HoldingsRepository holdingsRepository;
	private final AccountJwtUtil accountJwtUtil;
	private final RedisTemplate<String, Object> redisTemplate;

	// 계좌 생성
	@Transactional
	public void saveAccount(AccountRequestDto accountRequestDto,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		User user = userDetails.getUser();
		String encode = passwordEncoder.encode(accountRequestDto.getPassword());
		Account account = Account.builder()
			.accountName(accountRequestDto.getAccountName())
			.password(encode)
			.initialBalance(accountRequestDto.getInitialBalance())
			.currentBalance(accountRequestDto.getInitialBalance())
			.user(user)
			.build();
		accountRepository.save(account);
	}

	// 계좌 로그인
	public String loginAccount(AccountSignInRequestDto requestDto,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		Long accountId = requestDto.getAccountId();
		String password = requestDto.getPassword();
		Long userId = userDetails.getId();

		// 계좌 객체 생성
		Account account = accountRepository.findById(accountId).
			orElseThrow(() -> new CustomRuntimeException(ExceptionCode.ACCOUNT_NOT_FOUND));

		// 사용자 계좌 중에 해당 계좌가 존재하는 지 검사 로직
		if (!userId.equals(account.getUser().getId())) {
			throw new CustomRuntimeException(ExceptionCode.ACCOUNT_NOT_FOUND); // 예외 처리 변경 예정
		}

		// 계좌 비밀번호 일치 여부 로직 ( 입력 비밀번호 vs 입력한 계좌 ID의 실제 비밀번호 )
		if (!passwordEncoder.matches(password, account.getPassword())) {
			throw new CustomRuntimeException(ExceptionCode.ACCOUNT_NOT_FOUND); // 예외 처리 변경 예정
		}

		return accountJwtUtil.createAccountToken(accountId);
	}

	// 계좌 조회(단건)
	@Transactional(readOnly = true)
	public AccountResponseDto findAccountById(Long id) {

		Account account = accountRepository.findById(id).
			orElseThrow(() -> new CustomRuntimeException(ExceptionCode.ACCOUNT_NOT_FOUND));

		// 접근 권한 <- 로그인 한 유저가 계좌 생성한 사람과 동일한 지 체크하는 로직

		// Redis에서 캐싱된 총 자산, 수익률 가져오기
		BigDecimal totalAsset = getTotalAssetWithCache(id);
		BigDecimal profitRate = getProfitRateWithCache(id);

		return AccountResponseDto.of(account, totalAsset, profitRate);
	}

	// 로그인한 계좌 정보 조회
	public AccountResponseDto findAccountByToken() {
		Long accountId = AccountContextHolder.getAccountId();

		Account account = accountRepository.findById(accountId).
			orElseThrow(() -> new CustomRuntimeException(ExceptionCode.ACCOUNT_NOT_FOUND));

		// Redis에서 캐싱된 총 자산, 수익률 가져오기
		BigDecimal totalAsset = getTotalAssetWithCache(accountId);
		BigDecimal profitRate = getProfitRateWithCache(accountId);

		return AccountResponseDto.of(account, totalAsset, profitRate);
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
			.map(holding -> {
				String stockCode = holding.getStock().getStockCode();
				BigDecimal currentPrice = getCurrentPriceFromCache(stockCode);
				return HoldingsResponseDto.of(holding, currentPrice);
			})
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

	/**
	 * 계좌 총 자산(잔고 + 총 보유종목 평가 금액) 계산
	 * @param accountId
	 * @return
	 */
	public BigDecimal calculateTotalAsset(Long accountId) {
		Account account = accountRepository.findById(accountId)
			.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.ACCOUNT_NOT_FOUND));

		BigDecimal currentBalance = account.getCurrentBalance(); // 현금 잔고
		List<Holdings> holdings = holdingsRepository.findByAccountId(accountId);

		BigDecimal holdingValue = BigDecimal.ZERO;

		for (Holdings holding : holdings) {
			String stockCode = holding.getStock().getStockCode();
			Long quantity = holding.getQuantity();

			BigDecimal currentPrice = getCurrentPriceFromCache(stockCode); // Redis에서 현재가 가져오기

			BigDecimal evaluated = currentPrice.multiply(BigDecimal.valueOf(quantity));
			holdingValue = holdingValue.add(evaluated);
		}

		return currentBalance.add(holdingValue);
	}

	/**
	 * 계좌 수익률 계산
	 * @param accountId
	 */
	public BigDecimal calculateProfitRate(Long accountId) {
		Account account = accountRepository.findById(accountId)
			.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.ACCOUNT_NOT_FOUND));

		BigDecimal initialBalance = account.getInitialBalance(); // 초기자산

		// 보유 종목 정보
		List<Holdings> holdings = holdingsRepository.findByAccountId(accountId);

		// 종목별 평가금액 계산
		BigDecimal holdingValue = BigDecimal.ZERO;
		for (Holdings holding : holdings) {
			String stockCode = holding.getStock().getStockCode();
			Long quantity = holding.getQuantity();
			BigDecimal currentPrice = getCurrentPriceFromCache(stockCode); // 외부 또는 Redis에서 가져올 예정

			holdingValue = holdingValue.add(currentPrice.multiply(BigDecimal.valueOf(quantity)));
		}

		// 현재 총 자산 (현금 잔고 + 종목별 평가금액의 합)
		BigDecimal totalAsset = calculateTotalAsset(accountId);

		// 수익률 계산
		if (initialBalance.compareTo(BigDecimal.ZERO) == 0)
			return BigDecimal.ZERO;

		return totalAsset.subtract(initialBalance)
			.divide(initialBalance, 4, RoundingMode.HALF_UP)
			.multiply(BigDecimal.valueOf(100));
	}

	/**
	 * 계좌 수익률 캐싱
	 * @param accountId
	 * @return
	 */
	public BigDecimal getProfitRateWithCache(Long accountId) {
		String key = "accountProfitRate::" + accountId;
		Object cached = redisTemplate.opsForValue().get(key);

		if (cached != null) {
			return new BigDecimal(cached.toString());
		}

		// Redis에 없으면 계산해서 저장
		BigDecimal calculated = calculateProfitRate(accountId);

		// 1분 정도 TTL 설정 (원하는 주기로 조정 가능)
		redisTemplate.opsForValue().set(key, calculated.toPlainString(), Duration.ofMinutes(1));

		return calculated;
	}

	/**
	 * 계좌 총 자산(잔고 + 총 보유종목 평가 금액) 캐싱
	 * @param accountId
	 * @return
	 */
	public BigDecimal getTotalAssetWithCache(Long accountId) {
		String key = "accountAsset::" + accountId;
		Object cached = redisTemplate.opsForValue().get(key);

		if (cached != null) {
			return new BigDecimal(cached.toString());
		}

		// 캐시에 없으면 계산하고 저장
		BigDecimal calculated = calculateTotalAsset(accountId);
		redisTemplate.opsForValue().set(key, calculated.toPlainString(), Duration.ofMinutes(1));

		return calculated;
	}

	/**
	 * 현재가 가져오기
	 * @param stockCode
	 * @return
	 */
	public BigDecimal getCurrentPriceFromCache(String stockCode) {
		String key = "stockPrice::" + stockCode;
		Object cached = redisTemplate.opsForValue().get(key);

		if (cached == null) {
			throw new RuntimeException("현재가 캐시 없음: " + stockCode);
		}

		return new BigDecimal(cached.toString());
	}

}
