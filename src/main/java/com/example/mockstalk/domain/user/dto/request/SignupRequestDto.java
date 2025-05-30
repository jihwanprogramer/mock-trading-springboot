package com.example.mockstalk.domain.user.dto.request;

import com.example.mockstalk.domain.user.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class SignupRequestDto {



//    @NotBlank(message = "Email은 필수 입력 값입니다.")
//    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
//    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하이어야 합니다.")
    private String password;

    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    private String nickname;

    @NotBlank(message = "지갑주소는 필수 입력 값입니다.")
    private String walletAddress;

    @NotNull(message = "권한은 필수 입력 값입니다.")
    private UserRole userRole;

}
