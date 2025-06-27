package com.example.mockstalk.domain.auth.service;

import com.example.mockstalk.domain.auth.dto.request.LoginRequestDto;
import com.example.mockstalk.domain.auth.dto.response.LoginResponseDto;
import com.example.mockstalk.domain.auth.jwt.JwtUtil;
import com.example.mockstalk.domain.user.entity.User;
import com.example.mockstalk.domain.user.entity.UserRole;
import com.example.mockstalk.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    JwtTokenService tokenService;
    @Mock
    JwtUtil jwtUtil;

    @InjectMocks
    AuthService authService;

    @Test
    void login_success() {
        // given
        User user = User.builder()
                .id(1L)
                .email("tester@example.com")
                .password("encoded")
                .nickname("nick")
                .walletAddress("wallet")
                .userRole(UserRole.USER)
                .build();

        given(userRepository.findByEmail("tester@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("test1234!", "encoded")).willReturn(true);
        given(jwtUtil.createToken(1L, "tester@example.com", "nick", UserRole.USER)).willReturn("access");
        given(jwtUtil.createRefreshToken(1L)).willReturn("refresh");
        given(jwtUtil.substringToken("refresh")).willReturn("refresh");


        // when
        LoginResponseDto response = authService.login(
                new LoginRequestDto("tester@example.com", "test1234!", "USER"));

        // then
        assertThat(response.getAccessToken()).isEqualTo("access");
        verify(tokenService).storeRefreshToken(1L, "refresh");
    }

    @Test // 이메일 없음 실패 케이스
    void login_fail_user_not_found() {
        // given
        given(userRepository.findByEmail("noone@example.com")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                authService.login(new LoginRequestDto("noone@example.com", "password", "USER"))
        ).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test  // 비밀번호 틀렷을 경우
    void login_fail_wrong_password() {
        // given
        User user = User.builder()
                .id(1L)
                .email("tester@example.com")
                .password("encoded")
                .nickname("nick")
                .walletAddress("wallet")
                .userRole(UserRole.USER)
                .build();

        given(userRepository.findByEmail("tester@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrongpass!", "encoded")).willReturn(false);

        // when & then
        assertThatThrownBy(() ->
                authService.login(new LoginRequestDto("tester@example.com", "wrongpass!", "USER"))
        ).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("비밀번호가 일치하지 않습니다"); // 실제 메시지로 수정
    }


}
