package com.example.mockstalk.domain.interest_stock.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import com.example.mockstalk.common.error.CustomRuntimeException;
import com.example.mockstalk.common.error.ExceptionCode;
import com.example.mockstalk.domain.auth.security.CustomUserDetails;
import com.example.mockstalk.domain.interest_stock.dto.request.InterestRequestDto;
import com.example.mockstalk.domain.interest_stock.dto.response.InterestResponseDto;
import com.example.mockstalk.domain.interest_stock.entity.InterestStock;
import com.example.mockstalk.domain.interest_stock.repository.InterestStockRepository;
import com.example.mockstalk.domain.stock.entity.Stock;
import com.example.mockstalk.domain.stock.repository.StockRepository;
import com.example.mockstalk.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InterestStockServiceTest {

    @Mock
    InterestStockRepository interestStockRepository;

    @Mock
    StockRepository stockRepository;

    @InjectMocks
    InterestStockService interestStockService;

    User user;
    Stock stock;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("user@example.com")
                .nickname("닉네임")
                .walletAddress("0x123")
                .build();

        stock = Stock.builder()
                .id(10L)
                .stockName("삼성전자")
                .stockCode("005930")
                .build();
    }

    @Test
    void addInterest_success() {
        InterestRequestDto dto = new InterestRequestDto("삼성전자", "005930");

        given(stockRepository.findByStockNameAndStockCode(dto.getStockName(), dto.getStockCode()))
                .willReturn(Optional.of(stock));
        given(interestStockRepository.existsByUserAndStock(user, stock))
                .willReturn(false);

        interestStockService.addInterest(user, dto);

        then(interestStockRepository).should().save(any(InterestStock.class));
    }

    @Test
    void addInterest_fail_stockNotFound() {
        InterestRequestDto dto = new InterestRequestDto("없는종목", "000000");

        given(stockRepository.findByStockNameAndStockCode(dto.getStockName(), dto.getStockCode()))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> interestStockService.addInterest(user, dto))
                .isInstanceOf(CustomRuntimeException.class)
                .hasMessageContaining(ExceptionCode.STOCK_NOT_FOUND.getMessage());
    }

    @Test
    void addInterest_fail_interestAlreadyExists() {
        InterestRequestDto dto = new InterestRequestDto("삼성전자", "005930");

        given(stockRepository.findByStockNameAndStockCode(dto.getStockName(), dto.getStockCode()))
                .willReturn(Optional.of(stock));
        given(interestStockRepository.existsByUserAndStock(user, stock))
                .willReturn(true);

        assertThatThrownBy(() -> interestStockService.addInterest(user, dto))
                .isInstanceOf(CustomRuntimeException.class)
                .hasMessageContaining(ExceptionCode.INTEREST_ALREADY_EXISTS.getMessage());
    }

    @Test
    void findInterest_success() {
        InterestStock interestStock = InterestStock.builder()
                .id(100L)
                .user(user)
                .stock(stock)
                .build();

        given(interestStockRepository.findAllByUser(user))
                .willReturn(List.of(interestStock));

        List<InterestResponseDto> result = interestStockService.findInterest(user);

        assertThat(result).hasSize(1);
        InterestResponseDto dto = result.get(0);
        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getStockName()).isEqualTo("삼성전자");
        assertThat(dto.getStockCode()).isEqualTo("005930");
    }

    @Test
    void deleteInterest_success() {
        InterestStock interestStock = InterestStock.builder()
                .id(50L)
                .user(user)
                .stock(stock)
                .build();

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        given(userDetails.getUser()).willReturn(user);

        given(interestStockRepository.findById(50L))
                .willReturn(Optional.of(interestStock));

        interestStockService.deleteInterest(userDetails, 50L);

        then(interestStockRepository).should().delete(interestStock);
    }

    @Test
    void deleteInterest_fail_stockNotFound() {
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        given(interestStockRepository.findById(999L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> interestStockService.deleteInterest(userDetails, 999L))
                .isInstanceOf(CustomRuntimeException.class)
                .hasMessageContaining(ExceptionCode.STOCK_NOT_FOUND.getMessage());
    }

    @Test
    void deleteInterest_fail_userMismatch() {
        User otherUser = User.builder()
                .id(2L)
                .email("other@example.com")
                .nickname("다른유저")
                .walletAddress("0x456")
                .build();

        InterestStock interestStock = InterestStock.builder()
                .id(60L)
                .user(otherUser)
                .stock(stock)
                .build();

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        given(userDetails.getUser()).willReturn(user);

        given(interestStockRepository.findById(60L))
                .willReturn(Optional.of(interestStock));

        assertThatThrownBy(() -> interestStockService.deleteInterest(userDetails, 60L))
                .isInstanceOf(CustomRuntimeException.class)
                .hasMessageContaining(ExceptionCode.USER_MISMATCH_EXCEPTION.getMessage());
    }
}
