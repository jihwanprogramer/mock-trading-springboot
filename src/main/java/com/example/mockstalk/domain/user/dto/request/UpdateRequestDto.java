package com.example.mockstalk.domain.user.dto.request;

import lombok.Getter;

@Getter
public class UpdateRequestDto {

    private String nickname;

    private String oldPassword;

    private String newPassword;


}
