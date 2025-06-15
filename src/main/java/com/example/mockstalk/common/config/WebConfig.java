package com.example.mockstalk.common.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {

	@Bean
	public FilterRegistrationBean<AccountJwtFilter> accountJwtFilter(AccountJwtUtil accountJwtUtil) {
		FilterRegistrationBean<AccountJwtFilter> registration = new FilterRegistrationBean<>();
		registration.setFilter(new AccountJwtFilter(accountJwtUtil));
		registration.addUrlPatterns("/accounts/*"); // 원하는 URL에만 적용
		registration.setOrder(1); // 다른 필터와 충돌 나지 않게 순서 조정 가능
		return registration;
	}
}