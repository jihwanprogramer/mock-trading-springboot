package com.example.mockstalk.domain.board.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.mockstalk.common.response.ResponseMessage;
import com.example.mockstalk.domain.board.dto.BoardRequestDto;
import com.example.mockstalk.domain.board.dto.BoardResponseDto;
import com.example.mockstalk.domain.board.dto.BoardUpdateRequestDto;
import com.example.mockstalk.domain.board.service.BoardService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class BoardController {

	private final BoardService boardService;

	@PostMapping("/stocks/{stockId}/board")
	public ResponseEntity<BoardResponseDto> savePost(@PathVariable Long stockId,
		@RequestBody BoardRequestDto boardRequestDto) {
		BoardResponseDto boardResponseDto = BoardService.saveBoard(stockId, boardRequestDto);
		return ResponseEntity.status(HttpStatus.CREATED).body(boardResponseDto);
	}

	@GetMapping("/stocks/{stockId}/board")
	public ResponseEntity<List<BoardResponseDto>> findPostByBoardId(@PathVariable Long stockId) {
		return ResponseEntity.ok(boardService.findAllByStock(stockId));
	}

	@PatchMapping("/stocks/{stockId}/board/{boardId}")
	public ResponseEntity<ResponseMessage<?>> updatePost(@PathVariable Long stockId, @PathVariable Long boardId,
		@RequestBody BoardUpdateRequestDto boardUpdateRequestDto) {
		boardService.updatePost(stockId, boardId, boardUpdateRequestDto);
		return ResponseEntity.ok(ResponseMessage.success("게시물 수정 완료"));

	}

	@DeleteMapping("/stocks/{stockId}/board/{boardId}")
	public ResponseEntity<ResponseMessage<?>> deletePost(@PathVariable Long stockId, @PathVariable Long boardId) {
		boardService.deletePost(stockId, boardId);
		return ResponseEntity.ok(ResponseMessage.success("게시물 삭제 완료"));
	}

}
