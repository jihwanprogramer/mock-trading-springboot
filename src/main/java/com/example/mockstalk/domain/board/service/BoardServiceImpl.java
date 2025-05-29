package com.example.mockstalk.domain.board.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.mockstalk.domain.board.dto.BoardRequestDto;
import com.example.mockstalk.domain.board.dto.BoardResponseDto;
import com.example.mockstalk.domain.board.dto.BoardUpdateRequestDto;
import com.example.mockstalk.domain.board.entity.Board;
import com.example.mockstalk.domain.board.repository.BoardRepository;
import com.example.mockstalk.domain.stock.entity.Stock;
import com.example.mockstalk.domain.stock.repository.StockRepository;

import jakarta.transaction.Transactional;

@Service
public class BoardServiceImpl implements BoardService {

	private final BoardRepository boardRepository;
	private final StockRepository stockRepository;

	public BoardServiceImpl(BoardRepository boardRepository, StockRepository stockRepository) {
		this.boardRepository = boardRepository;
		this.stockRepository = stockRepository;
	}

	@Override
	public BoardResponseDto createPost(Long stockId, BoardRequestDto boardRequestDto) {

		if (boardRequestDto.getTitle() == null || boardRequestDto.getTitle().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "제목은 필수입니다!");
		}
		if (boardRequestDto.getContent() == null || boardRequestDto.getContent().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, " 내용은 필수입니다 !");
		}

		Stock stock = stockRepository.findById(stockId)
			.orElseThrow(() -> new IllegalArgumentException("종목을 찾을 수 없습니다"));

		Board board = new Board(boardRequestDto.getTitle(), boardRequestDto.getContent(), stock);
		Board saved = boardRepository.save(board);

		return new BoardResponseDto(
			saved.getId(),
			saved.getTitle(),
			saved.getContent()
		);
	}

	@Override
	public List<BoardResponseDto> findPostByStockId(Long stockId) {
		Stock stock = stockRepository.findById(stockId)
			.orElseThrow(() -> new IllegalArgumentException("종목을 찾을 수 없습니다"));

		List<Board> boardList = boardRepository.findPostByStockId(stock);
		List<BoardResponseDto> responseDtoList = new ArrayList<>();

		for (Board board : boardList) {
			BoardResponseDto boardResponseDto = new BoardResponseDto(
				board.getId(), board.getTitle(), board.getContent()
			);

			responseDtoList.add(boardResponseDto);
		}
		return responseDtoList;

	}

	@Transactional
	@Override
	public void updatePost(Long stockId, Long boardId, BoardUpdateRequestDto boardUpdateRequestDto) {
		Board board = boardRepository.findById(boardId)
			.orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

		board.updatedAt(boardUpdateRequestDto);
	}

	@Override
	public void deletePost(Long stockId, Long boardId) {
		Board board = boardRepository.findById(boardId)
			.orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

		boardRepository.delete(board);
	}
}
