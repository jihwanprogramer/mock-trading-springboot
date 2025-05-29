package com.example.mockstalk.domain.board.service;

import java.util.List;

import com.example.mockstalk.domain.board.dto.BoardRequestDto;
import com.example.mockstalk.domain.board.dto.BoardResponseDto;
import com.example.mockstalk.domain.board.dto.BoardUpdateRequestDto;

import jakarta.transaction.Transactional;

public interface BoardService {
	BoardResponseDto createPost(Long stockId, BoardRequestDto boardRequestDto);

	List<BoardResponseDto> findPostByStockId(Long stockId);

	@Transactional
	void updatePost(Long stockId, Long boardId, BoardUpdateRequestDto boardUpdateRequestDto);

	void deletePost(Long stockId, Long boardId);
}
