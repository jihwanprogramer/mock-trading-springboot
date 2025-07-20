package com.example.mockstalk.domain.news.naver.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewsSearchResponse {

	public Integer totalCount;
	public List<NewsResponse> articles;
}
