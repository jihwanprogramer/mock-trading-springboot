package com.example.mockstalk.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExceptionCode implements ErrorCode {

	// 400 Bad Request
	JWT_TOKEN_REQUIRED(HttpStatus.BAD_REQUEST, "JWT 토큰이 필요합니다."),
	VALID_ERROR(HttpStatus.BAD_REQUEST, "Validation 이 유효하지 않습니다"),
	COMMENT_MISMATCH_EXCEPTION(HttpStatus.BAD_REQUEST, "잘못된 댓글 접근입니다."),
	CSV_FILE_READ_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CSV 파일을 읽는 도중 오류가 발생했습니다."),

    // 401 Unauthorized = 인증이 안될 때
    INVALID_JWT_SIGNATURE(HttpStatus.UNAUTHORIZED, "유효하지 않는 JWT 서명입니다."),

    // 403 Forbidden = 권한이 없을 때
    USER_MISMATCH_EXCEPTION(HttpStatus.BAD_REQUEST, "권한이 없습니다."),

	// 404 Not Found
	NOT_FOUND_TOKEN(HttpStatus.NOT_FOUND, "해당 토큰을 찾을 수 없습니다."),
	NOT_FOUND_COMMENT(HttpStatus.NOT_FOUND, "해당 댓글을 찾을 수 없습니다."),
	CSV_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "CSV 파일을 찾을 수 없습니다."),

    // 409 Conflict = 서버와 충돌, 데이터가 이미 존재할때(400 보다 명확함)
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 등록된 회원입니다."),

    // 500 Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류 혹은 예기치 못한 예외가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public int getErrorCode() {
        return httpStatus.value();
    }
}