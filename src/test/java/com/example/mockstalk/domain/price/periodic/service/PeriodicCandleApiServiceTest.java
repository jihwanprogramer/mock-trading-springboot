package com.example.mockstalk.domain.price.periodic.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.mockstalk.common.hantutoken.TokenResponseDto;
import com.example.mockstalk.domain.price.periodic_candles.repository.PeriodicCandleRepository;
import com.example.mockstalk.domain.price.periodic_candles.service.PeriodicCandleApiService;
import com.example.mockstalk.domain.price.periodic_candles.service.PeriodicCandleService;
import com.example.mockstalk.domain.stock.entity.Stock;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class PeriodicCandleApiServiceTest {

    @InjectMocks
    private PeriodicCandleApiService periodicCandleApiService;

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private PeriodicCandleRepository candleRepository;
    @Mock
    private PeriodicCandleService periodicCandleService;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String, Object> mockValueOperations;

    private Stock stock;
    private TokenResponseDto tokenDto;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(periodicCandleApiService, "appKey", "test-app-key");
        ReflectionTestUtils.setField(periodicCandleApiService, "appSecret", "test-app-secret");
        ReflectionTestUtils.setField(periodicCandleApiService, "baseUrl",
            "https://test.koreainvestment.com:9443");

        stock = Stock.builder().id(1L).stockCode("005930").stockName("삼성전자").build();

        tokenDto = new TokenResponseDto();
        tokenDto.setAccess_token("test-access-token");
        tokenDto.setToken_type("Bearer");
        tokenDto.setExpires_in(86400);

        when(redisTemplate.opsForValue()).thenReturn(mockValueOperations);
    }

    @Test
    @DisplayName("기간별 캔들 데이터 API 호출 및 저장 성공 테스트")
    void fetchAndSaveCandles_success() {
        // given
        // 반드시 이 한 줄만 있으면 충분!
        when(mockValueOperations.get("accessToken::koreainvestment")).thenReturn(tokenDto);

        // API 응답 데이터 구성
        Map<String, Object> candleMap = Map.of(
            "stck_bsop_date", "20230101",
            "stck_oprc", "70000",
            "stck_clpr", "71000",
            "stck_hgpr", "72000",
            "stck_lwpr", "69000",
            "acml_vol", "1000000"
        );
        List<Map<String, Object>> output2 = List.of(candleMap);
        Map<String, Object> responseMap = Map.of("output2", output2);

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseMap, HttpStatus.OK);

        when(restTemplate.exchange(any(String.class), eq(HttpMethod.GET), any(HttpEntity.class),
            eq(Map.class)))
            .thenReturn(responseEntity);

        when(candleRepository.findByStockAndCandleTypeAndDateIn(any(), any(), any())).thenReturn(
            List.of());

        // when
        periodicCandleApiService.fetchAndSaveCandles(
            stock, "D", "20230101", "20230102"
        );

        // then
        verify(mockValueOperations, times(1)).get("accessToken::koreainvestment");
        verify(restTemplate, times(1)).exchange(any(String.class), eq(HttpMethod.GET),
            any(HttpEntity.class), eq(Map.class));
        verify(periodicCandleService, times(1)).saveCandlesAsync(anyList());
        verify(candleRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("토큰이 없으면 예외 발생")
    void fetchAndSaveCandles_noToken_throwsException() {
        // given
        given(redisTemplate.opsForValue().get("accessToken::koreainvestment")).willReturn(null);

        // when & then
        assertThatThrownBy(() -> periodicCandleApiService.fetchAndSaveCandles(
            stock, "D", "20230101", "20230102"
        )).isInstanceOf(RuntimeException.class)
            .hasMessageContaining("토큰이 Redis에 없습니다.");
    }
}