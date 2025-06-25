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
	INSUFFICIENT_HOLDINGS(HttpStatus.BAD_REQUEST, "보유 주식 수량이 부족합니다."),
	INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "계정 잔액이 부족합니다."),
	ORDER_ALREADY_CANCELED(HttpStatus.BAD_REQUEST, "이미 취소된 주문입니다."),
	ORDER_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "이미 완료된 주문입니다."),
	ORDER_ALREADY_SETTLED(HttpStatus.BAD_REQUEST, "이미 체결된 주문입니다."),
	HANTU_TOKEN_REQUIRED(HttpStatus.BAD_REQUEST, "한국투자증권 접근 토큰생성 실패"),

	// 401 Unauthorized = 인증이 안될 때
	INVALID_JWT_SIGNATURE(HttpStatus.UNAUTHORIZED, "유효하지 않는 JWT 서명입니다."),
	INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 올바르지 않습니다."),
	INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다."),

	// 403 Forbidden = 권한이 없을 때
	USER_MISMATCH_EXCEPTION(HttpStatus.BAD_REQUEST, "권한이 없습니다."),
	UNAUTHORIZED_ACCOUNT_ACCESS(HttpStatus.FORBIDDEN, "계정에 접근할 권한이 없습니다."),
	UNAUTHORIZED_ORDER_ACCESS(HttpStatus.FORBIDDEN, "주문에 접근할 권한이 없습니다."),




	// 404 Not Found
	NOT_FOUND_TOKEN(HttpStatus.NOT_FOUND, "해당 토큰을 찾을 수 없습니다."),
	NOT_FOUND_COMMENT(HttpStatus.NOT_FOUND, "해당 댓글을 찾을 수 없습니다."),
	CSV_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "CSV 파일을 찾을 수 없습니다."),
	ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "계정을 찾을 수 없습니다."),
	STOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "주식을 찾을 수 없습니다."),
	HOLDINGS_NOT_FOUND(HttpStatus.NOT_FOUND, "보유 주식을 찾을 수 없습니다."),
	ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 종목입니다."),
	NOT_FOUND_CANDLE(HttpStatus.NOT_FOUND, "해당 봉을 찾을 수 없습니다."),
	NOT_FOUND_USER(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다."),
	NOT_FOUND_EMAIL(HttpStatus.NOT_FOUND, "해당 이메일을 찾을 수 없습니다."),
	NOT_FOUND_APPROVALKEY(HttpStatus.NOT_FOUND, "approvalKey를 찾을 수 없습니다."),


	// 409 Conflict = 서버와 충돌, 데이터가 이미 존재할때(400 보다 명확함)
	EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 등록된 회원입니다."),
	INTEREST_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 관심 등록된 항목입니다."),

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
