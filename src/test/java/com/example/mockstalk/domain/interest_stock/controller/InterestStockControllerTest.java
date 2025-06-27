package com.example.mockstalk.domain.interest_stock.controller;

import com.example.mockstalk.domain.auth.jwt.JwtUtil;
import com.example.mockstalk.domain.interest_stock.dto.request.InterestRequestDto;
import com.example.mockstalk.domain.interest_stock.entity.InterestStock;
import com.example.mockstalk.domain.interest_stock.repository.InterestStockRepository;
import com.example.mockstalk.domain.stock.entity.Stock;
import com.example.mockstalk.domain.stock.repository.StockRepository;
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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class InterestStockControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired StockRepository stockRepository;
    @Autowired InterestStockRepository interestStockRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtUtil jwtUtil;

    private String accessToken;
    private User user;
    private Stock stock;

    @BeforeEach
    void setup() {
        user = User.builder()
                .email("tester@example.com")
                .password(passwordEncoder.encode("test1234!"))
                .nickname("테스트유저")
                .walletAddress("0xabc")
                .userRole(UserRole.USER)
                .build();
        userRepository.saveAndFlush(user);

        stock = new Stock("삼성전자", "005930");
        stockRepository.saveAndFlush(stock);

        accessToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getNickname(), user.getUserRole());
    }

    @Test
    void addInterest_success() throws Exception {
        InterestRequestDto dto = new InterestRequestDto("삼성전자", "005930");

        mockMvc.perform(post("/interests")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("관심종목 등록이 되었습니다."));

        // DB에 저장되었는지 확인
        assertThat(interestStockRepository.existsByUserAndStock(user, stock)).isTrue();
    }

    @Test
    void findInterest_success() throws Exception {
        InterestStock interest = new InterestStock(user, stock);
        interestStockRepository.saveAndFlush(interest);

        mockMvc.perform(get("/interests")
                        .header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void deleteInterest_success() throws Exception {
        InterestStock interest = new InterestStock(user, stock);
        interestStockRepository.saveAndFlush(interest);

        mockMvc.perform(delete("/interests/" + interest.getId())
                        .header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("관심 종목이 삭제 되었습니다."));

        // 삭제 확인
        assertThat(interestStockRepository.findById(interest.getId())).isEmpty();
    }
}
