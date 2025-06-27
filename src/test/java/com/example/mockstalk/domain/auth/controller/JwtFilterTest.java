package com.example.mockstalk.domain.auth.controller;

import com.example.mockstalk.domain.auth.jwt.JwtUtil;
import com.example.mockstalk.domain.auth.service.JwtTokenService;
import com.example.mockstalk.domain.user.entity.User;
import com.example.mockstalk.domain.user.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class JwtFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private JwtTokenService jwtTokenService;

    @Test
    void testSecureEndpoint() throws Exception {
        System.out.println("✅ testSecureEndpoint 실행됨");  // <-- 이 부분 추가!

        // Given
        User user = User.builder()
                .id(1L)
                .email("tester@example.com")
                .nickname("Tester")
                .password("")
                .userRole(UserRole.USER)
                .build();

        // access token 생성
        String token = jwtUtil.createToken(user.getId(), user.getEmail(), user.getNickname(), user.getUserRole());

        // 블랙리스트 아님
        given(jwtTokenService.isBlacklisted(anyString())).willReturn(false);

        // When & Then
        mockMvc.perform(get("/api/test/secure")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, tester@example.com"));

    }
}
