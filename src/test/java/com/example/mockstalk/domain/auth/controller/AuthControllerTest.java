package com.example.mockstalk.domain.auth.controller;

import com.example.mockstalk.domain.auth.dto.request.LoginRequestDto;
import com.example.mockstalk.domain.auth.jwt.JwtUtil;
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
    com.example.mockstalk.domain.auth.service.JwtTokenService tokenService;

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

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(jsonPath("$.data").isString());
    }

    @Test
    void logout_success() throws Exception {
        // given
        String token = jwtUtil.createToken(user.getId(), user.getEmail(), user.getNickname(), user.getUserRole());

        // when
        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", token)
                        .principal(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                new com.example.mockstalk.domain.auth.security.CustomUserDetails(user),
                                null,
                                null
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그아웃 완료"));

        // then (성능상 실제 Redis 호출은 안함)
        verify(tokenService, times(1)).blacklistAccessToken(anyString(), anyLong());
        verify(tokenService, times(1)).deleteRefreshToken(eq(user.getId()));
    }

    @Test
    void reissue_success() throws Exception {
        // given
        String refreshToken = jwtUtil.createRefreshToken(user.getId());
        String stripped = jwtUtil.substringToken(refreshToken);

        when(tokenService.getStoredRefreshToken(user.getId())).thenReturn(stripped);

        // when & then
        mockMvc.perform(post("/auth/reissue")
                        .cookie(new jakarta.servlet.http.Cookie("refreshToken", stripped)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isString());

        verify(tokenService, times(1)).getStoredRefreshToken(user.getId());
    }
}
