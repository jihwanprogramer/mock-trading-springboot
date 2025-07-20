package com.example.mockstalk.domain.board.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.mockstalk.domain.board.entity.Board;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

	List<Board> findPostByStockId(@Param("stockId") Long stockId);
}
