package com.example.mockstalk.common.config;

import java.io.IOException;

import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "AccountJwtFilter")
@RequiredArgsConstructor
public class AccountJwtFilter extends OncePerRequestFilter {

	private final AccountJwtUtil accountJwtUtil;

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {

		String tokenValue = request.getHeader("X-ACCOUNT-Authorization");

		if (StringUtils.hasText(tokenValue) && tokenValue.startsWith("Bearer ")) {
			try {
				String token = accountJwtUtil.substringToken(tokenValue);
				Claims claims = accountJwtUtil.extractClaims(token);

				// 이 필터가 처리할 토큰인지 판단하는 장치
				if (!"account".equals(claims.get("tokenType"))) {
					filterChain.doFilter(request, response);
					return;
				}

				Long accountId = claims.get("accountId", Long.class);

				// 계좌 정보 컨텍스트에 저장
				AccountContextHolder.set(accountId);
				log.debug("AccountJwt 인증 성공 - accountId: {}", accountId);

			} catch (Exception e) {
				log.warn("유효하지 않은 계좌 토큰입니다. {}", e.getMessage());
				// 인증 실패해도 흐름은 계속 진행
			}
		}

		filterChain.doFilter(request, response);
		AccountContextHolder.clear(); // ThreadLocal 클리어
	}
}