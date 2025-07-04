package com.example.mockstalk.domain.news.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.mockstalk.domain.news.naver.response.NewsSearchResponse;
import com.example.mockstalk.domain.news.service.NewsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class NewsController {

	private final NewsService newsService;

	@GetMapping("/v1/news/search")
	public ResponseEntity<NewsSearchResponse> getNews(@RequestParam("keyword") String keyword) {
		NewsSearchResponse newsSearchResponse = newsService.getNews(keyword);

		return ResponseEntity.ok(newsSearchResponse);
	}

}
