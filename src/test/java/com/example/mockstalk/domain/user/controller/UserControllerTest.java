package com.example.mockstalk.domain.user.controller;

import com.example.mockstalk.domain.user.dto.request.DeleteRequestDto;
import com.example.mockstalk.domain.user.dto.request.SignupRequestDto;
import com.example.mockstalk.domain.user.dto.request.UpdateRequestDto;
import com.example.mockstalk.domain.user.entity.User;
import com.example.mockstalk.domain.user.repository.UserRepository;
import com.example.mockstalk.domain.auth.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static com.example.mockstalk.domain.user.entity.UserRole.USER;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtUtil jwtUtil;

    private String accessToken;

    @BeforeEach
    void setup() {
        User user = User.builder()
                .email("tester@example.com")
                .password(passwordEncoder.encode("test1234!"))
                .nickname("테스트유저")
                .walletAddress("0x123")
                .userRole(USER)
                .build();
        userRepository.saveAndFlush(user); // flush 중요!
        accessToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getNickname(), user.getUserRole());
    }

    @Test
    void signup_success() throws Exception {
        SignupRequestDto dto = new SignupRequestDto("new@example.com", "new1234!", "테스트유저", "0x999", USER);

        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("회원가입 완료."));
    }

    @Test
    void findMe_success() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("정보 조회"));
    }

    @Test
    void updateMe_success() throws Exception {
        UpdateRequestDto dto = new UpdateRequestDto("테스트유저", "test1234!","새로운비번");

        mockMvc.perform(patch("/users/me")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("유저 정보 수정 완료"));
    }

    @Test
    void findByWallet_success() throws Exception {
        mockMvc.perform(get("/users/wallet/0x123")
                        .header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("타 유저 조회"));

    }

    @Test
    void deleteMe_success() throws Exception {
        DeleteRequestDto dto = new DeleteRequestDto("test1234!");

        mockMvc.perform(delete("/users/me")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("탈퇴가 완료 되었습니다."));
    }


}
