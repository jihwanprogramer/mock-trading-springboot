package com.example.mockstalk.domain.user.dto.response;

import lombok.Getter;

@Getter
public class LoginResponseDto {

    private String token;

    public LoginResponseDto(String token) {
        this.token=token;
    }
}
