package com.example.mockstalk.domain.news.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.mockstalk.domain.news.naver.response.NewsSearchResponse;
import com.example.mockstalk.domain.news.service.NewsService;

@RestController
@RequiredArgsConstructor
public class NewsController {

	private final NewsService newsService;

	@GetMapping("/api/v1/news/search")
	public ResponseEntity<NewsSearchResponse> getNews(@RequestParam("keyword") String keyword) {
		NewsSearchResponse newsSearchResponse = newsService.getNews(keyword);

		return ResponseEntity.ok(newsSearchResponse);
	}

}
