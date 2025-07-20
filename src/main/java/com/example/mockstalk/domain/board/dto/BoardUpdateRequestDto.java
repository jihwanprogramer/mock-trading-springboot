package com.example.mockstalk.domain.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class BoardUpdateRequestDto {
	private String title;
	private String content;
}
