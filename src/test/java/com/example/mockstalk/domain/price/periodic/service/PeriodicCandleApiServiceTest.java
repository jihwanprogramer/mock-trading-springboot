package com.example.mockstalk.domain.price.periodic.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.mockstalk.common.hantutoken.TokenResponseDto;
import com.example.mockstalk.common.hantutoken.TokenService;
import com.example.mockstalk.domain.price.periodic_candles.dto.PeriodicCandleApiResponseDto;
import com.example.mockstalk.domain.price.periodic_candles.service.PeriodicCandleApiService;
import com.example.mockstalk.domain.stock.entity.Stock;
import com.example.mockstalk.domain.stock.repository.StockRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
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
    private TokenService tokenService;
    @Mock
    private StockRepository stockRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    private Stock stock;
    private TokenResponseDto tokenDto;

    @BeforeEach
    void setUp() {
        // 테스트용 private 필드 값 설정
        ReflectionTestUtils.setField(periodicCandleApiService, "hantuDomain",
            "https://test.koreainvestment.com:9443");
        ReflectionTestUtils.setField(periodicCandleApiService, "candlePath", "/test-path");
        ReflectionTestUtils.setField(periodicCandleApiService, "appKey", "test-app-key");
        ReflectionTestUtils.setField(periodicCandleApiService, "appSecret", "test-app-secret");
        ReflectionTestUtils.setField(periodicCandleApiService, "approvalKey", "test-approval-key");

        stock = Stock.builder().id(1L).stockCode("005930").stockName("삼성전자").build();

        tokenDto = new TokenResponseDto();
        tokenDto.setAccess_token("test-access-token");
        tokenDto.setToken_type("Bearer");
        tokenDto.setExpires_in(86400);
    }

    @Test
    @DisplayName("기간별 캔들 데이터 API 호출 및 DTO 변환 성공 테스트")
    void getPeriodicCandles_success() {
        // given
        given(tokenService.getAccessToken()).willReturn(tokenDto);

        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.put("rt_cd", "0");

        ArrayNode output2Array = objectMapper.createArrayNode();
        ObjectNode candleNode = objectMapper.createObjectNode();
        candleNode.put("stck_bsop_date", "20230101");
        candleNode.put("stck_oprc", "70000");
        candleNode.put("stck_clpr", "71000");
        candleNode.put("stck_hgpr", "72000");
        candleNode.put("stck_lwpr", "69000");
        candleNode.put("acml_vol", "1000000");
        output2Array.add(candleNode);
        rootNode.set("output2", output2Array);

        ResponseEntity<JsonNode> responseEntity = new ResponseEntity<>(rootNode, HttpStatus.OK);

        given(restTemplate.exchange(any(String.class), eq(HttpMethod.GET), any(HttpEntity.class),
            eq(JsonNode.class)))
            .willReturn(responseEntity);

        // when
        List<PeriodicCandleApiResponseDto> result = periodicCandleApiService.fetchAndSaveCandles(
            stock.getStockCode(), PeriodicCandleType.DAY);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStck_clpr()).isEqualTo("71000");

        verify(tokenService, times(1)).getAccessToken();
        verify(restTemplate, times(1)).exchange(any(String.class), eq(HttpMethod.GET),
            any(HttpEntity.class), eq(JsonNode.class));
    }

    @Test
    @DisplayName("API 응답 코드가 0이 아닐 경우 빈 리스트 반환 테스트")
    void getPeriodicCandles_fail_rt_cd_not_zero() {
        // given
        given(tokenService.getHantuToken()).willReturn(tokenDto);

        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.put("rt_cd", "1"); // 실패 코드
        rootNode.put("msg1", "에러 발생");

        ResponseEntity<JsonNode> responseEntity = new ResponseEntity<>(rootNode, HttpStatus.OK);

        given(restTemplate.exchange(any(String.class), eq(HttpMethod.GET), any(HttpEntity.class),
            eq(JsonNode.class)))
            .willReturn(responseEntity);

        // when
        List<PeriodicCandleApiResponseDto> result = periodicCandleApiService.getPeriodicCandles(
            stock.getStockCode(), PeriodicCandleType.DAY);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }
}