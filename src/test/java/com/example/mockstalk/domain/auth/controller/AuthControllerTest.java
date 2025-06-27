package com.example.mockstalk.domain.auth.controller;

import com.example.mockstalk.domain.auth.dto.request.LoginRequestDto;
import com.example.mockstalk.domain.auth.dto.response.LoginResponseDto;
import com.example.mockstalk.domain.auth.jwt.JwtUtil;
import com.example.mockstalk.domain.auth.service.AuthService;
import com.example.mockstalk.domain.auth.service.JwtTokenService;
import com.example.mockstalk.domain.user.entity.User;
import com.example.mockstalk.domain.user.entity.UserRole;
import com.example.mockstalk.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtUtil jwtUtil;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenService tokenService;

    private User user;

    @BeforeEach
    void setup() {
        user = User.builder()
                .email("tester@example.com")
                .password(passwordEncoder.encode("test1234!"))
                .nickname("테스트유저")
                .walletAddress("0x123")
                .userRole(UserRole.ADMIN)
                .build();
        userRepository.save(user);
    }

    @Test
    void login_success() throws Exception {
        LoginRequestDto dto = new LoginRequestDto("tester@example.com", "test1234!", "ADMIN");

        // LoginResponseDto 예시 (AuthService.login() 반환값)
        LoginResponseDto mockLoginResponse = new LoginResponseDto(
                1L,
                "accessToken",
                "refreshToken"
        );

        when(authService.login(any(LoginRequestDto.class))).thenReturn(mockLoginResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(jsonPath("$.data").isString());

        verify(authService, times(1)).login(any(LoginRequestDto.class));
    }


    @Test
    void logout_success() throws Exception {
        // 1. "Bearer " 포함된 토큰 생성
        String bearerToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getNickname(), user.getUserRole());

        // 2. "Bearer " 제거해서 실제 토큰만 추출 (→ 서비스 로직에서 이 값으로 사용)
        String pureToken = jwtUtil.substringToken(bearerToken);

        // 3. logout 호출 mock 설정
        doNothing().when(authService).logout(eq(pureToken), eq(user.getId()));

        // 4. 요청 수행
        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그아웃 완료"));

        // 5. 검증
        verify(authService, times(1)).logout(eq(pureToken), eq(user.getId()));
    }




    @Test
    void reissue_success() throws Exception {
        String refreshToken = jwtUtil.createRefreshToken(user.getId());
        String accessToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getNickname(), user.getUserRole());

        String newAccessToken = "new.access.token.jwt";
        when(authService.reissueAccessToken(refreshToken, accessToken)).thenReturn(newAccessToken);

        mockMvc.perform(post("/auth/reissue")
                        .cookie(new jakarta.servlet.http.Cookie("refreshToken", refreshToken))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("토큰 재발급 성공"))
                .andExpect(jsonPath("$.data").value(newAccessToken));

        verify(authService, times(1)).reissueAccessToken(refreshToken, accessToken);
        // tokenService 검증 제거
    }


}
