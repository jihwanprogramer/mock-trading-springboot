package com.example.mockstalk.domain.user.dto.response;


import lombok.Getter;

@Getter
public class LoginResponseDto {

    private Long id;

    private String token;

    public LoginResponseDto(Long id, String token) {
        this.id = id;
        this.token = token;
    }
}
