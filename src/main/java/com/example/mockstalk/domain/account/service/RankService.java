package com.example.mockstalk.domain.account.service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.mockstalk.domain.account.dto.UserProfitRankDto;
import com.example.mockstalk.domain.account.entity.Account;
import com.example.mockstalk.domain.account.repository.AccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RankService {

	private final RedisTemplate<String, Object> redisTemplate;
	private final AccountRepository accountRepository;
	private final AccountService accountService;

	//

	/**
	 * 유저가 소유한 계좌 중 최고 수익률 계좌의 수익률 조회
	 * @param userId
	 * @return
	 */
	public BigDecimal getMaxProfitRateByUser(Long userId) {
		List<Account> accounts = accountRepository.findAllByUser_Id(userId);
		BigDecimal maxRate = BigDecimal.valueOf(-999);

		for (Account account : accounts) {
			try {
				BigDecimal rate = accountService.getProfitRateWithCache(account.getId());
				if (rate.compareTo(maxRate) > 0) {
					maxRate = rate;
				}
			} catch (Exception e) {
				log.warn("계좌 수익률 조회 실패 - accountId: {}, 이유: {}", account.getId(), e.getMessage());
			}
		}

		return maxRate;
	}

	/**
	 * Redis에 저장
	 * 12시간 마다 갱신
	 * @param userId
	 */
	public void updateUserProfitRanking(Long userId) {
		BigDecimal maxRate = getMaxProfitRateByUser(userId);
		redisTemplate.opsForZSet().add("userProfitRanking", userId.toString(), maxRate.doubleValue());
	}

	/**
	 * 유저 최고 수익률 캐싱 메서드
	 * 추후 스케줄러 클래스로 이동시킬 예정
	 * transcational 처리 해주려면 같은 클래스 내부에 스케줄러 코드와 캐싱 코드가 존재하면 안됨)
	 */
	@Scheduled(cron = "0 0 */12 * * *") // 12시간마다 실행
	public void updateAllUserProfitRankings() {
		List<Long> userIds = accountRepository.findAllUserIds(); // 별도 메서드 필요
		for (Long userId : userIds) {
			try {
				updateUserProfitRanking(userId);
			} catch (Exception e) {
				log.warn("수익률 랭킹 갱신 실패 - userId: {}, 이유: {}", userId, e.getMessage());
			}
		}
	}

	/**
	 * 수익률 랭킹 조회 메서드 (topN에 넣는 값 만큼의 랭킹을 구함)
	 * @param topN
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<UserProfitRankDto> getTopUserProfitRankings(int topN) {
		Set<ZSetOperations.TypedTuple<Object>> topUsers =
			redisTemplate.opsForZSet().reverseRangeWithScores("userProfitRanking", 0, topN - 1);

		if (topUsers == null || topUsers.isEmpty()) {
			log.info("Redis 랭킹 캐시 데이터가 없어 갱신을 수행합니다.");
			updateAllUserProfitRankings();
			topUsers = redisTemplate.opsForZSet().reverseRangeWithScores("userProfitRanking", 0, topN - 1);

			if (topUsers == null || topUsers.isEmpty()) {
				return Collections.emptyList(); // 그래도 없으면 빈값
			}
		}

		return topUsers.stream()
			.map(tuple -> {
				Long userId = Long.valueOf(tuple.getValue().toString());
				double score = tuple.getScore();
				return new UserProfitRankDto(userId, BigDecimal.valueOf(score));
			})
			.collect(Collectors.toList());
	}

	/**
	 * 초기자산 별 유저 수익률 랭킹 조회 (계좌 기반) 코드 예시 - 추후 보유종목 기반 수익률 랭킹 추가 여부 고민중
	 * @param topN
	 * @param targetInitialBalance
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<UserProfitRankDto> getTopUserProfitRankingsByInitialBalance(int topN, BigDecimal targetInitialBalance) {
		Set<ZSetOperations.TypedTuple<Object>> topUsers =
			redisTemplate.opsForZSet().reverseRangeWithScores("userProfitRanking", 0, topN * 2L); // 넉넉히 조회

		if (topUsers == null || topUsers.isEmpty()) {
			return Collections.emptyList();
		}

		return topUsers.stream()
			.map(tuple -> {
				Long userId = Long.valueOf(tuple.getValue().toString());
				BigDecimal score = BigDecimal.valueOf(tuple.getScore());

				BigDecimal userInitialBalance = accountRepository.findInitialBalanceByUserId(userId);

				if (userInitialBalance.compareTo(targetInitialBalance) == 0) {
					return new UserProfitRankDto(userId, score);
				}
				return null;
			})
			.filter(Objects::nonNull)
			.limit(topN)
			.collect(Collectors.toList());
	}
}
