package com.example.mockstalk.domain.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateRequestDto {

    private String nickname;

    private String oldPassword;

    private String newPassword;


}
