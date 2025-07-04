package com.example.mockstalk.domain.price;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/realtime-price")
@RequiredArgsConstructor
public class RealtimePriceController {

	private final RedisTemplate<String, String> redisTemplate;

	// @GetMapping("/{stockCode}")
	// public ResponseEntity<ResponseMessage<String>> getRealtimePrice(@PathVariable String stockCode) {
	// 	String price = redisTemplate.opsForValue().get("stockPrice:" + stockCode);
	// 	return ResponseEntity.ok(ResponseMessage.success(price));
	// }

	@GetMapping("/{stockCode}")
	public ResponseEntity<String> getRealtimePrice(@PathVariable String stockCode) {
		String price = redisTemplate.opsForValue().get("stockPrice:" + stockCode);
		return price != null ? ResponseEntity.ok(price) : ResponseEntity.noContent().build();
	}

}
