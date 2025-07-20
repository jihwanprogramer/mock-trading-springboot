package com.example.mockstalk.domain.news.service;

import java.net.URI;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.example.mockstalk.domain.news.naver.NaverApiConfig;
import com.example.mockstalk.domain.news.naver.response.NaverNewsResponse;
import com.example.mockstalk.domain.news.naver.response.NewsSearchResponse;

@Service
@RequiredArgsConstructor
public class NewsService {

	private final NaverApiConfig naverApiConfig;
	private final RestTemplate restTemplate;

	@Cacheable(value = "newsCache", key = "#keyword", unless = "#result == null") // 키워드 기준으로 캐싱 실패응답은 캐싱하지않음
	public NewsSearchResponse getNews(String keyword) {
		URI uri = UriComponentsBuilder
			.fromUriString(naverApiConfig.newsUrl)
			.queryParam("query", keyword)
			.queryParam("display", 20)
			.queryParam("start", 1)
			.queryParam("sort", "date")
			.build()
			.encode()
			.toUri();
		HttpHeaders headers = new HttpHeaders();
		headers.set("X-Naver-Client-Id", naverApiConfig.clientId);
		headers.set("X-Naver-Client-Secret", naverApiConfig.clientSecret);

		NaverNewsResponse naverNewsResponse = restTemplate.exchange
				(uri, HttpMethod.GET, new HttpEntity<>(null, headers), NaverNewsResponse.class)
			.getBody();

		NewsSearchResponse newsSearchResponse = new NewsSearchResponse();
		newsSearchResponse.totalCount = naverNewsResponse.total;
		newsSearchResponse.articles = naverNewsResponse.items;
		return newsSearchResponse;
	}
}
