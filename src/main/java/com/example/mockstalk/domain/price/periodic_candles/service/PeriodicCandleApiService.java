package com.example.mockstalk.domain.price.periodic_candles.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.mockstalk.domain.price.periodic_candles.repository.PeriodicCandleRepository;
import com.example.mockstalk.domain.stock.repository.StockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PeriodicCandleApiService {

	private final RestTemplate restTemplate;
	private final PeriodicCandleRepository candleRepository;
	private final StockRepository stockRepository;

    @Value("${openapi.token}")
    private String token;

    @Value("${openapi.base-url}")
    private String baseUrl;

	public void fetchAndSaveCandles(String stockCode, String candleType, String startDate,
		String endDate) {
		// String url = baseUrl + "/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice";
		//
		// HttpHeaders headers = new HttpHeaders();
		// headers.set("authorization", "Bearer " + token);
		// headers.set("tr_id", "FHKST03010100");
		// headers.set("appKey", "YOUR_APP_KEY");
		// headers.set("appSecret", "YOUR_APP_SECRET");
		//
		// UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
		// 	.queryParam("FID_COND_MRKT_DIV_CODE", "J")  // 주식시장
		// 	.queryParam("FID_INPUT_ISCD", stockCode)
		// 	.queryParam("FID_PERIOD_DIV_CODE", candleType) // D/W/M/Y
		// 	.queryParam("FID_INPUT_DATE_1", startDate)
		// 	.queryParam("FID_INPUT_DATE_2", endDate)
		// 	.queryParam("FID_ORG_ADJ_PRC", "0");
		//
		// HttpEntity<?> entity = new HttpEntity<>(headers);
		//
		// ResponseEntity<Map> response = restTemplate.exchange(
		// 	builder.toUriString(),
		// 	HttpMethod.GET,
		// 	entity,
		// 	Map.class
		// );
		//
		// List<Map<String, Object>> outputs = (List<Map<String, Object>>)response.getBody()
		// 	.get("output");
		//
		// ObjectMapper mapper = new ObjectMapper();
		// List<PeriodicCandleApiResponseDto> dtoList = outputs.stream()
		// 	.map(data -> mapper.convertValue(data, PeriodicCandleApiResponseDto.class))
		// 	.toList();
		//
		// Stock stock = stockRepository.findByStockCode(stockCode);
		//
		// List<PeriodicCandles> entityList = dtoList.stream()
		// 	.map(dto -> dto.toEntity(PeriodicCandleType.valueOf(candleType), stock)).toList();
		//
		// candleRepository.saveAll(entityList);
	}

}
