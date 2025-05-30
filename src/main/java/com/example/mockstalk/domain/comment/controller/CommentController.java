/*
package com.example.mockstalk.domain.comment.controller;


import static org.springframework.data.domain.Sort.Direction.DESC;

import com.example.mockstalk.common.response.ResponseMessage;
import com.example.mockstalk.domain.comment.dto.CommentRequestDto;
import com.example.mockstalk.domain.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{boardId}/comments")
    public ResponseEntity<ResponseMessage<?>> saveComment(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long boardId,
        @RequestBody CommentRequestDto commentRequestDto
    ) {
        commentService.saveComment(userDetails, boardId, commentRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseMessage.success());
    }

    @GetMapping("/{boardId}/comments")
    public ResponseEntity<ResponseMessage<?>> findBoardByBoardId(
        @PathVariable Long boardId,
        @PageableDefault(size = 10, sort = "creatTime", direction = DESC) Pageable pageable) {
        return ResponseEntity.ok(
            ResponseMessage.success(commentService.findCommentByBoardId(boardId, pageable)));
    }

    @PatchMapping("/{boardId}/comments/{commentId}")
    public ResponseEntity<ResponseMessage<?>> updateComment(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long boardId,
        @PathVariable Long commentId,
        @RequestBody CommentRequestDto commentRequestDto) {
        commentService.updateComment(userDetails, boardId, commentId, commentRequestDto);
        return ResponseEntity.ok(ResponseMessage.success("댓글이 수정되었습니다."));
    }

    @DeleteMapping("/{boardId}/comments/{commentId}")
    public ResponseEntity<ResponseMessage<?>> deleteComment(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long boardId,
        @PathVariable Long coomentId) {
        commentService.deleteComment(userDetails, boardId, coomentId);
        return ResponseEntity.ok(ResponseMessage.success("댓글이 삭제되었습니다."));
    }

}
*/
