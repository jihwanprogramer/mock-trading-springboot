package com.example.mockstalk.domain.comment.service;

import com.example.mockstalk.common.error.CustomRuntimeException;
import com.example.mockstalk.common.error.ExceptionCode;
import com.example.mockstalk.domain.board.repository.BoardRepository;
import com.example.mockstalk.domain.comment.dto.CommentRequestDto;
import com.example.mockstalk.domain.comment.dto.CommentResponseDto;
import com.example.mockstalk.domain.comment.entity.Comment;
import com.example.mockstalk.domain.comment.repository.CommentRepository;
import com.example.mockstalk.domain.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    public void saveComment(UserDetails userDetails, Long boardId,
        CommentRequestDto commentRequestDto) {

        Comment comment = Comment.builder()
            .content(commentRequestDto.getContent())
            .board(boardRepository.findById(boardId).orElseThrow())
            .user(userRepository.findById(Long.parseLong(userDetails.getUsername())).orElseThrow())
            .build();

        commentRepository.save(comment);
    }

    public Slice<CommentResponseDto> findCommentByBoardId(Long boardId, Pageable pageable) {
        return commentRepository.findAllByBoardId(boardId, pageable).map(CommentResponseDto::from);
    }

    public void updateComment(UserDetails userDetails, Long boardId, Long commentId,
        CommentRequestDto commentRequestDto) {
        Comment comment = validComment(userDetails, boardId, commentId);
        comment.update(commentRequestDto.getContent());
    }

    public void deleteComment(UserDetails userDetails, Long boardId, Long commentId) {
        Comment comment = validComment(userDetails, boardId, commentId);
        commentRepository.delete(comment);
    }

    public Comment validComment(UserDetails userDetails, Long boardId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new CustomRuntimeException(ExceptionCode.NOT_FOUND_COMMENT));

        if (!comment.getBoard().getId().equals(boardId)) {
            throw new CustomRuntimeException(ExceptionCode.COMMENT_MISMATCH_EXCEPTION);
        }

        if (!userDetails.getUsername().equals(comment.getUser().getId())) {
            throw new CustomRuntimeException(ExceptionCode.USER_MISMATCH_EXCEPTION);
        }

        return comment;
    }


}
