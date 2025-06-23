package com.example.mockstalk.domain.price.periodic_candles.service;

import com.example.mockstalk.common.hantutoken.TokenResponseDto;
import com.example.mockstalk.domain.price.periodic_candles.dto.PeriodicCandleApiResponseDto;
import com.example.mockstalk.domain.price.periodic_candles.entity.PeriodicCandleType;
import com.example.mockstalk.domain.price.periodic_candles.entity.PeriodicCandles;
import com.example.mockstalk.domain.price.periodic_candles.repository.PeriodicCandleRepository;
import com.example.mockstalk.domain.stock.entity.Stock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class PeriodicCandleApiService {

    private final RestTemplate restTemplate;
    private final PeriodicCandleRepository candleRepository;
    private final PeriodicCandleService periodicCandleService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${hantu-openapi.appkey}")
    private String appKey;

    @Value("${hantu-openapi.appsecret}")
    private String appSecret;

    @Value("${hantu-openapi.domain}")
    private String baseUrl;


    public void fetchAndSaveCandles(Stock stock, String candleType,
        String startDate,
        String endDate) {

        String stockCode = stock.getStockCode();

        String url = baseUrl + "/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice";

        TokenResponseDto tokenResponse = (TokenResponseDto) redisTemplate.opsForValue()
            .get("accessToken::koreainvestment");

        if (tokenResponse == null) {
            throw new RuntimeException("토큰이 Redis에 없습니다.");
        }

        String accessToken = tokenResponse.getAccess_token();
        if (!accessToken.startsWith("Bearer ")) {
            accessToken = "Bearer " + accessToken;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", accessToken);
        headers.set("tr_id", "FHKST03010100");
        headers.set("appKey", appKey);
        headers.set("appSecret", appSecret);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
            .queryParam("FID_COND_MRKT_DIV_CODE", "J")  // 주식시장
            .queryParam("FID_INPUT_ISCD", stockCode)
            .queryParam("FID_PERIOD_DIV_CODE", candleType) // D/W/M/Y
            .queryParam("FID_INPUT_DATE_1", startDate)
            .queryParam("FID_INPUT_DATE_2", endDate)
            .queryParam("FID_ORG_ADJ_PRC", "0");

        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
            builder.toUriString(),
            HttpMethod.GET,
            entity,
            Map.class
        );

        List<Map<String, Object>> output2 = (List<Map<String, Object>>) response.getBody()
            .get("output2");

        List<PeriodicCandleApiResponseDto> dtoList = output2.stream()
            .map(data -> new PeriodicCandleApiResponseDto(
                (String) data.get("stck_bsop_date"),
                String.valueOf(data.get("stck_oprc")),
                String.valueOf(data.get("stck_clpr")),
                String.valueOf(data.get("stck_hgpr")),
                String.valueOf(data.get("stck_lwpr")),
                String.valueOf(data.get("acml_vol"))
            ))
            .toList();

        Set<LocalDateTime> apiDates = dtoList.stream()
            .map(dto -> LocalDate.parse(dto.getStck_bsop_date(), DateTimeFormatter.BASIC_ISO_DATE)
                .atStartOfDay())
            .collect(Collectors.toSet());

        List<PeriodicCandles> existingCandles = candleRepository
            .findByStockAndCandleTypeAndDateIn(stock, PeriodicCandleType.valueOf(candleType),
                apiDates);

        Set<LocalDateTime> existingDates = existingCandles.stream()
            .map(PeriodicCandles::getDate)
            .collect(Collectors.toSet());

        List<PeriodicCandles> entityList = dtoList.stream()
            .filter(dto -> {
                LocalDateTime dateTime = LocalDate.parse(dto.getStck_bsop_date(),
                        DateTimeFormatter.BASIC_ISO_DATE)
                    .atStartOfDay();
                return !existingDates.contains(dateTime);
            })
            .map(dto -> dto.toEntity(PeriodicCandleType.valueOf(candleType), stock))
            .collect(Collectors.toList());

        periodicCandleService.saveCandlesAsync(entityList);

        candleRepository.saveAll(entityList);
    }


}
