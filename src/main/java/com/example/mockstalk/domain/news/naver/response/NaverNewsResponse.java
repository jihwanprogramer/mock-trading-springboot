package com.example.mockstalk.domain.news.naver.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NaverNewsResponse {

	public String lastBuildDate;
	public Integer total;
	public Integer start;
	public Integer display;
	public List<NewsResponse> items;
}
