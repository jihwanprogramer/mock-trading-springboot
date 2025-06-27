package com.example.mockstalk;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.mockstalk.common.config.JwtUtil;
import com.example.mockstalk.domain.account.entity.Account;
import com.example.mockstalk.domain.account.repository.AccountRepository;
import com.example.mockstalk.domain.user.entity.User;
import com.example.mockstalk.domain.user.entity.UserRole;
import com.example.mockstalk.domain.user.repository.UserRepository;

@SpringBootTest
public class UserAccountTokenGenerationTest {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private JwtUtil jwtUtil;

	@Test
	void createOneUserWith1000AccountsAndToken() {
		// 1. 유저 1명 생성
		User user = User.builder()
			.email("user1@example.com")
			.password("1234") // 테스트용 평문
			.nickname("user1")
			.walletAddress("wallet-1")
			.userRole(UserRole.USER)
			.build();
		userRepository.save(user);

		// 2. JWT 토큰 발급 (유저 1명 기준)
		String token = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole());

		// 3. 계좌 1000개 생성 및 저장, JSON 결과 리스트 준비
		List<String> resultJsonList = new ArrayList<>();

		for (int i = 1; i <= 1000; i++) {
			Account account = Account.builder()
				.accountName("계좌-" + i)
				.password("1234")
				.initialBalance(new BigDecimal("1000000"))
				.currentBalance(new BigDecimal("1000000"))
				.isActive(true)
				.user(user)
				.build();
			accountRepository.save(account);

			// 동일 토큰 사용
			resultJsonList.add(String.format("{ \"accountId\": %d, \"token\": \"%s\" }", account.getId(), token));
		}

		// 4. JSON 배열 출력
		System.out.println("[");
		System.out.println(String.join(",\n", resultJsonList));
		System.out.println("]");
	}
}
