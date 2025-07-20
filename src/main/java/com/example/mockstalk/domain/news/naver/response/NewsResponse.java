package com.example.mockstalk.domain.news.naver.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsResponse {

	private String title;
	private String originallink;
	private String link;
	private String description;
	private String pubDate;

}
