package com.example.mockstalk.common.config;

import java.util.List;

import com.example.mockstalk.domain.auth.jwt.JwtFilter;
import com.example.mockstalk.domain.auth.jwt.JwtUtil;
import com.example.mockstalk.domain.auth.service.JwtTokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


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
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(List.of("http://localhost:3000", "https://mockstalk.co.kr")); // 허용할 Origin
		config.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config); // 모든 경로에 대해 적용
		return source;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			// CSRF 보호 비활성화
			.csrf(csrf -> csrf.disable())
			// 요청별 인증/인가 설정
			.authorizeHttpRequests(user -> user
				.requestMatchers("/api/auth/login", "/api/users/signup", "/api/auth/reissue"
					, "/health")
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
