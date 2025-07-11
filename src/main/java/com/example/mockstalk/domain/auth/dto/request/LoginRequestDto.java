package com.example.mockstalk.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginRequestDto {
//
//    @NotBlank(message = "Email은 필수 입력 값입니다.")
//    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

//    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
//    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하이어야 합니다.")
//    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*\\W)$")
    private String password;

//    @NotBlank(message = "권한은 필수 입력 값입니다.")
    private  String userRole;

}
