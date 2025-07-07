package com.example.mockstalk.domain.account.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import com.example.mockstalk.domain.account.dto.UserProfitRankDto;
import com.example.mockstalk.domain.account.repository.AccountRepository;

@ExtendWith(MockitoExtension.class)
class RankServiceTest {

	@Mock
	private RedisTemplate<String, Object> redisTemplate;
	@Mock
	private AccountRepository accountRepository;
	@Mock
	private AccountService accountService;
	@Mock
	private ZSetOperations<String, Object> zSetOperations;

	@InjectMocks
	private RankService rankService;

	private ZSetOperations.TypedTuple<Object> tuple1;
	private ZSetOperations.TypedTuple<Object> tuple2;
	private BigDecimal targetInitialBalance;

	@BeforeEach
	void setUp() {
		// Redis ZSet dummy tuple들
		tuple1 = new ZSetOperations.TypedTuple<>() {
			@Override
			public Object getValue() {
				return "1";
			}

			@Override
			public Double getScore() {
				return 10.0;
			}

			@Override
			public int compareTo(ZSetOperations.TypedTuple<Object> o) {
				return 0;
			}
		};
		tuple2 = new ZSetOperations.TypedTuple<>() {
			@Override
			public Object getValue() {
				return "2";
			}

			@Override
			public Double getScore() {
				return 5.0;
			}

			@Override
			public int compareTo(ZSetOperations.TypedTuple<Object> o) {
				return 0;
			}
		};

		when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

		targetInitialBalance = new BigDecimal("1000000");
	}

	@Test
	void getTopUserProfitRankings_정상() {
		when(zSetOperations.reverseRangeWithScores("userProfitRanking", 0, 2))
			.thenReturn(Set.of(tuple1));

		List<UserProfitRankDto> result = rankService.getTopUserProfitRankings(3);

		assertEquals(1, result.size());
		assertEquals(BigDecimal.valueOf(10.0), result.get(0).getProfitRate());
	}

	@Test
	void getTopUserProfitRankingsByInitialBalance_초기자산_필터링_정상() {
		when(zSetOperations.reverseRangeWithScores("userProfitRanking", 0, 4))
			.thenReturn(Set.of(tuple1, tuple2));

		when(accountRepository.findInitialBalanceByUserId(1L)).thenReturn(targetInitialBalance);
		when(accountRepository.findInitialBalanceByUserId(2L)).thenReturn(new BigDecimal("2000000"));

		List<UserProfitRankDto> result = rankService.getTopUserProfitRankingsByInitialBalance(2, targetInitialBalance);

		assertEquals(1, result.size());
		assertEquals(1L, result.get(0).getUserId());
	}

	@Test
	void getTopUserProfitRankingsByInitialBalance_빈결과() {
		when(zSetOperations.reverseRangeWithScores("userProfitRanking", 0, 4))
			.thenReturn(Set.of(tuple2));

		when(accountRepository.findInitialBalanceByUserId(2L)).thenReturn(new BigDecimal("3000000"));

		List<UserProfitRankDto> result = rankService.getTopUserProfitRankingsByInitialBalance(2, targetInitialBalance);

		assertTrue(result.isEmpty());
	}

	// @Test
	// void updateUserProfitRanking() {
	// }
	//
	// @Test
	// void updateAllUserProfitRankings() {
	// }
	//
	// @Test
	// void getTopUserProfitRankings() {
	// }
	//
	// @Test
	// void getTopUserProfitRankingsByInitialBalance() {
	// }
}