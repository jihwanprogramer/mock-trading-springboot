// package com.example.mockstalk.common.websocket;
//
// import org.springframework.data.redis.core.RedisTemplate;
// import org.springframework.stereotype.Service;
//
// import com.example.mockstalk.common.error.CustomRuntimeException;
// import com.example.mockstalk.common.error.ExceptionCode;
//
// import lombok.RequiredArgsConstructor;
//
// @Service
// @RequiredArgsConstructor
// public class ApprovalKeyService {
// 	private final RedisTemplate<String, Object> redisTemplate;
//
// 	public String get() {
// 		String key = (String)redisTemplate.opsForValue().get("approvalKey::koreainvestment");
// 		if (key == null || key.isBlank()) {
// 			throw new CustomRuntimeException(ExceptionCode.NOT_FOUND_APPROVALKEY);
// 		}
// 		return key;
// 	}

