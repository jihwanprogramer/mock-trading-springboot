package com.example.mockstalk.common.error;

import lombok.Getter;

@Getter
public class CustomRuntimeException extends RuntimeException {

  private final ExceptionCode exceptionCode;

  public CustomRuntimeException(ExceptionCode exceptionCode) {
    super(exceptionCode.getMessage());
    this.exceptionCode = exceptionCode;
  }
}

