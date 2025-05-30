package com.example.mockstalk.domain.comment.repository;

import com.example.mockstalk.domain.comment.entity.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Slice<Comment> findAllByBoardId(Long boardId, Pageable pageable);
}
