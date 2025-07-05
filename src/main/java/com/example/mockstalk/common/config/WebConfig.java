package com.example.mockstalk.common.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Bean
	public FilterRegistrationBean<AccountJwtFilter> accountJwtFilter(AccountJwtUtil accountJwtUtil) {
		FilterRegistrationBean<AccountJwtFilter> registration = new FilterRegistrationBean<>();
		registration.setFilter(new AccountJwtFilter(accountJwtUtil));
		registration.addUrlPatterns("/api/accounts/info"); // 원하는 URL에만 적용
		registration.setOrder(1); // 다른 필터와 충돌 나지 않게 순서 조정 가능
		return registration;
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**") // CORS 허용할 URL 패턴
			.allowedOrigins("http://localhost:3000") // 프론트엔드 주소
			.allowedMethods("*") // 허용할 HTTP 메서드: GET, POST, etc
			.allowCredentials(true); // 쿠키 및 인증 정보 허용
	}

}