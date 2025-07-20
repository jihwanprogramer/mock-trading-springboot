package com.example.mockstalk.domain.comment.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.example.mockstalk.common.error.CustomRuntimeException;
import com.example.mockstalk.common.error.ExceptionCode;
import com.example.mockstalk.domain.auth.security.CustomUserDetails;
import com.example.mockstalk.domain.board.entity.Board;
import com.example.mockstalk.domain.board.repository.BoardRepository;
import com.example.mockstalk.domain.comment.dto.CommentRequestDto;
import com.example.mockstalk.domain.comment.dto.CommentResponseDto;
import com.example.mockstalk.domain.comment.entity.Comment;
import com.example.mockstalk.domain.comment.repository.CommentRepository;
import com.example.mockstalk.domain.user.entity.User;
import com.example.mockstalk.domain.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private BoardRepository boardRepository;
    @Mock
    private UserRepository userRepository;

    private User user;
    private Board board;
    private Comment comment;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        user = new User(1L, "test@test.com", "password", "nickname", "wallet", null, null, null,
            null, null);
        board = Board.builder().id(1L).title("title").content("content").user(user).build();
        comment = Comment.builder().id(1L).content("comment content").user(user).board(board)
            .build();
        userDetails = new CustomUserDetails(user);
    }

    @Nested
    @DisplayName("댓글 생성 테스트")
    class CreateComment {

        @Test
        @DisplayName("성공")
        void saveComment_success() {
            // given
            CommentRequestDto requestDto = new CommentRequestDto("new comment");
            given(boardRepository.findById(board.getId())).willReturn(Optional.of(board));
            given(userRepository.findById(userDetails.getId())).willReturn(Optional.of(user));

            // when
            commentService.saveComment(userDetails, board.getId(), requestDto);

            // then
            then(commentRepository).should(times(1)).save(any(Comment.class));
        }
    }

    @Nested
    @DisplayName("게시글 ID로 댓글 조회 테스트")
    class FindComment {

        @Test
        @DisplayName("성공")
        void findCommentByBoardId_success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Slice<Comment> comments = new SliceImpl<>(List.of(comment), pageable, false);
            given(commentRepository.findAllByBoardId(board.getId(), pageable)).willReturn(comments);

            // when
            Slice<CommentResponseDto> result = commentService.findCommentByBoardId(board.getId(),
                pageable);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo(comment.getId());
            then(commentRepository).should(times(1)).findAllByBoardId(board.getId(), pageable);
        }
    }

    @Nested
    @DisplayName("댓글 수정 테스트")
    class UpdateComment {

        @Test
        @DisplayName("성공")
        void updateComment_success() {
            // given
            CommentRequestDto requestDto = new CommentRequestDto("updated content");
            given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));

            // when
            commentService.updateComment(userDetails, board.getId(), comment.getId(), requestDto);

            // then
            assertThat(comment.getContent()).isEqualTo("updated content");
        }

        @Test
        @DisplayName("실패 - 댓글이 존재하지 않음")
        void updateComment_fail_notFoundComment() {
            // given
            CommentRequestDto requestDto = new CommentRequestDto("updated content");
            given(commentRepository.findById(comment.getId())).willReturn(Optional.empty());

            // when & then
            CustomRuntimeException exception = assertThrows(CustomRuntimeException.class, () ->
                commentService.updateComment(userDetails, board.getId(), comment.getId(),
                    requestDto));
            assertThat(exception.getExceptionCode()).isEqualTo(ExceptionCode.NOT_FOUND_COMMENT);
        }

        @Test
        @DisplayName("실패 - 게시글 ID 불일치")
        void updateComment_fail_boardMismatch() {
            // given
            Long wrongBoardId = 2L;
            CommentRequestDto requestDto = new CommentRequestDto("updated content");
            given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));

            // when & then
            CustomRuntimeException exception = assertThrows(CustomRuntimeException.class, () ->
                commentService.updateComment(userDetails, wrongBoardId, comment.getId(),
                    requestDto));
            assertThat(exception.getExceptionCode()).isEqualTo(
                ExceptionCode.COMMENT_MISMATCH_EXCEPTION);
        }

        @Test
        @DisplayName("실패 - 사용자 불일치")
        void updateComment_fail_userMismatch() {
            // given
            User anotherUser = new User(2L, "another@test.com", "password", "another", "wallet2",
                null, null, null, null, null);
            CustomUserDetails anotherUserDetails = new CustomUserDetails(anotherUser);
            CommentRequestDto requestDto = new CommentRequestDto("updated content");
            given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));

            // when & then
            CustomRuntimeException exception = assertThrows(CustomRuntimeException.class, () ->
                commentService.updateComment(anotherUserDetails, board.getId(), comment.getId(),
                    requestDto));
            assertThat(exception.getExceptionCode()).isEqualTo(
                ExceptionCode.USER_MISMATCH_EXCEPTION);
        }
    }

    @Nested
    @DisplayName("댓글 삭제 테스트")
    class DeleteComment {

        @Test
        @DisplayName("성공")
        void deleteComment_success() {
            // given
            given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));

            // when
            commentService.deleteComment(userDetails, board.getId(), comment.getId());

            // then
            then(commentRepository).should(times(1)).delete(comment);
        }

        @Test
        @DisplayName("실패 - 댓글을 찾을 수 없음")
        void deleteComment_fail_notFoundComment() {
            // given
            given(commentRepository.findById(comment.getId())).willReturn(Optional.empty());

            // when & then
            CustomRuntimeException exception = assertThrows(CustomRuntimeException.class, () ->
                commentService.deleteComment(userDetails, board.getId(), comment.getId()));

            assertThat(exception.getExceptionCode()).isEqualTo(ExceptionCode.NOT_FOUND_COMMENT);
            then(commentRepository).should(never()).delete(any());
        }
    }
}
