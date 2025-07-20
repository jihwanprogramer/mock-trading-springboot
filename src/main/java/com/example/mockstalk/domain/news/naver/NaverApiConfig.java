package com.example.mockstalk.domain.news.naver;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NaverApiConfig {

	@Value("${naver.api.client-id}")
	public String clientId;

	@Value("${naver.api.client-secret}")
	public String clientSecret;

	@Value("${naver.api.url}")
	public String newsUrl;
}
