package com.example.mockstalk.domain.comment.dto;

import com.example.mockstalk.domain.comment.entity.Comment;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentResponseDto {

    private final Long id;
    private final Long boardId;
    private final Long userId;
    private final String content;

    public static CommentResponseDto from(Comment comment) {
        return new CommentResponseDto(comment.getId(), comment.getBoard().getId(),
            comment.getUser().getId(), comment.getContent());
    }

}
