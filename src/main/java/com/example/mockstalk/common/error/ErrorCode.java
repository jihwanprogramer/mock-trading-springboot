package com.example.mockstalk.common.error;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

	HttpStatus getHttpStatus();

	String getMessage();

	int getErrorCode();
}

