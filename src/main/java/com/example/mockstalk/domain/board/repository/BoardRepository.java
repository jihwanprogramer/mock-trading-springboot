package com.example.mockstalk.domain.board.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.mockstalk.domain.board.entity.Board;
import com.example.mockstalk.domain.stock.entity.Stock;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

	List<Board> findPostByStockId(Stock stock);
}
