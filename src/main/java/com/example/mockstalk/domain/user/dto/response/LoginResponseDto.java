package com.example.mockstalk.domain.user.dto.response;


import lombok.Getter;

@Getter
public class LoginResponseDto {

    private Long id;

    private String accessToken;

    private String refreshToken;

    public LoginResponseDto(Long id, String accessToken, String refreshToken) {
        this.id = id;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
