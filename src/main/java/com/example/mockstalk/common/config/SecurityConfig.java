package com.example.mockstalk.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.mockstalk.common.jwttoken.JwtTokenService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtUtil jwtUtil;
	private final JwtTokenService tokenService;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public JwtFilter jwtFilter() {
		return new JwtFilter(jwtUtil, tokenService);  // JwtFilter 빈 등록
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			// CSRF 보호 비활성화
			.csrf(csrf -> csrf.disable())
			// 요청별 인증/인가 설정
			.authorizeHttpRequests(user -> user
				.requestMatchers("/auth/login", "/users/signup", "/auth/reissue", "/api/stocks/${stockCode}/candles")
				.permitAll()
				.requestMatchers("/admin/**")
				.hasRole("ADMIN")
				.anyRequest()
				.authenticated()
			)
			// JWT 필터를 앞에 삽입
			.addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

}
