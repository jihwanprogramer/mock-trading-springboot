package com.example.mockstalk.domain.price.livePrice.service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.example.mockstalk.common.hantutoken.TokenResponseDto;
import com.example.mockstalk.domain.stock.repository.StockRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class LivePriceService {
	@Value("${hantu-openapi.domain}")
	private String apiDomain;

	@Value("${hantu-openapi.appkey}")
	private String appKey;

	@Value("${hantu-openapi.appsecret}")
	private String appSecret;

	private final StockRepository stockRepository;
	private final RedisTemplate<String, Object> redisTemplate;
	private final RestTemplate restTemplate;

	public String getStockPriceData(String stockCode) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		TokenResponseDto tokenResponse = (TokenResponseDto)redisTemplate.opsForValue()
			.get("accessToken::koreainvestment");

		if (tokenResponse == null) {
			throw new RuntimeException("토큰이 Redis에 없습니다.");
		}

		String accessToken = tokenResponse.getAccess_token();
		if (!accessToken.startsWith("Bearer ")) {
			accessToken = "Bearer " + accessToken;
		}
		headers.set("authorization", accessToken);
		headers.set("appkey", appKey);
		headers.set("appsecret", appSecret);
		headers.set("tr_id", "FHKST01010100");

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
				apiDomain + "/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice")
			.queryParam("FID_COND_MRKT_DIV_CODE", "J")
			.queryParam("FID_INPUT_ISCD", stockCode);

		HttpEntity<?> entity = new HttpEntity<>(headers);

		ResponseEntity<String> response = restTemplate.exchange(
			builder.toUriString(),
			HttpMethod.GET,
			entity,
			String.class);

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode root = objectMapper.readTree(response.getBody());
			String price = root.path("output").path("stck_prpr").asText();
			return price;
		} catch (Exception e) {
			System.err.println("JSON 파싱 실패: " + e.getMessage());
		}

		return null; // 실패 시 null 반환
	}

	// 모든 종목 현재가 조회 및 캐싱
	public void cacheAllStockPrices() {
		List<String> codes = stockRepository.findAllStockCodes();
		int delayPerRequestMillis = 50;
		for (int i = 0; i < codes.size(); i++) {
			String code = codes.get(i);
			try {
				String priceData = getStockPriceData(code);
				redisTemplate.opsForValue().set("stockPrice::" + code, priceData, Duration.ofMinutes(5));
				System.out.printf("[%03d/%03d] 캐싱 완료: %s%n", i + 1, codes.size(), code);
			} catch (Exception e) {
				System.err.printf("[%03d/%03d] 실패: %s -> %s%n", i + 1, codes.size(), code, e.getMessage());
			}

			// 요청 간 간격 주기 (0.5초 = 초당 2개)
			try {
				Thread.sleep(delayPerRequestMillis);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt(); // 인터럽트 복구
				break;
			}
		}
	}

	// 캐시된 데이터 조회
	public String getCachedStockPrice(String stockCode) {
		return (String)redisTemplate.opsForValue().get("stockPrice::" + stockCode);
	}
}
