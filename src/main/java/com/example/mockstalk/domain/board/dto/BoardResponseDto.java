package com.example.mockstalk.domain.board.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardResponseDto {

	private Long id;
	private Long userId;
	private String title;
	private String content;

	public BoardResponseDto(Long id, String title, String content) {
		this.id = id;
		this.title = title;
		this.content = content;
	}
}
