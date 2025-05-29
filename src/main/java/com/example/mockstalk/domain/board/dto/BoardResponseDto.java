package com.example.mockstalk.domain.board.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardResponseDto {

	private Long boardId;
	private Long userId;
	private Long stockId;
	private String title;
	private String content;

	public BoardResponseDto(Long id, String title, String content) {
	}
}
