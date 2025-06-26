package com.example.mockstalk.domain.price.livePrice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.startsWith;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import com.example.mockstalk.common.hantutoken.TokenResponseDto;
import com.example.mockstalk.domain.stock.repository.StockRepository;

class LivePriceServiceTest {

	@InjectMocks
	private LivePriceService livePriceService;

	@Mock
	private StockRepository stockRepository;

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	@Mock
	private ValueOperations<String, Object> valueOperations;

	@Mock
	private RestTemplate restTemplate;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);

		// LivePriceService 내부 @Value 필드 수동 설정
		livePriceService = new LivePriceService(
			stockRepository, redisTemplate, restTemplate
		);
		ReflectionTestUtils.setField(livePriceService, "apiDomain", "https://fake.api");
		ReflectionTestUtils.setField(livePriceService, "appKey", "test-app-key");
		ReflectionTestUtils.setField(livePriceService, "appSecret", "test-app-secret");
	}

	@Test
	void testGetStockPriceData_success() throws Exception {
		// given
		// 종목/가짜 엑세스 토큰
		String stockCode = "005930";
		TokenResponseDto token = new TokenResponseDto();
		token.setAccess_token("test-token");

		// 토큰을 가져오는 세팅
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get("accessToken::koreainvestment")).thenReturn(token);

		// api 응답 예상
		String mockResponse = """
			{
			  "output": {
			    "stck_prpr": "123456"
			  }
			}
			""";

		ResponseEntity<String> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

		RestTemplate mockRestTemplate = mock(RestTemplate.class);
		ReflectionTestUtils.setField(livePriceService, "restTemplate", mockRestTemplate);

		when(mockRestTemplate.exchange(
			anyString(),
			eq(HttpMethod.GET),
			any(HttpEntity.class),
			eq(String.class))
		).thenReturn(responseEntity);

		// when
		String result = livePriceService.getStockPriceData(stockCode);

		// then
		assertThat(result).isEqualTo("123456");
	}

	@Test
	void testCacheAllStockPrices_success() {
		// given
		List<String> codes = Arrays.asList("005930", "000660");

		when(stockRepository.findAllStockCodes()).thenReturn(codes);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get("accessToken::koreainvestment"))
			.thenReturn(new TokenResponseDto("Bearer test-token"));

		// getStockPriceData 내부 RestTemplate 응답 mocking은 생략 가능하거나 따로 처리 가능
		LivePriceService spyService = Mockito.spy(livePriceService);
		doReturn("123456").when(spyService).getStockPriceData(anyString());

		// when
		spyService.cacheAllStockPrices();

		// then
		verify(redisTemplate.opsForValue(), times(codes.size())).set(
			startsWith("stockPrice::"), eq("123456"), eq(Duration.ofMinutes(5))
		);
	}

	@Test
	void testGetCachedStockPrice_success() {
		// given
		String stockCode = "005930";
		String expectedPrice = "123456";

		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get("stockPrice::" + stockCode)).thenReturn(expectedPrice);

		// when
		String result = livePriceService.getCachedStockPrice(stockCode);

		// then
		assertThat(result).isEqualTo(expectedPrice);
	}
}

