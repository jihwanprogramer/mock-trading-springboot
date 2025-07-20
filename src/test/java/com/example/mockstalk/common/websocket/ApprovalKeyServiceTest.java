package com.example.mockstalk.common.websocket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.example.mockstalk.common.error.CustomRuntimeException;

@ExtendWith(MockitoExtension.class)
class ApprovalKeyServiceTest {

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	@Mock
	private ValueOperations<String, Object> valueOps;

	@InjectMocks
	private ApprovalKeyService approvalKeyService;

	@Test
	void getApprovalKey_정상작동() {
		// given
		given(redisTemplate.opsForValue()).willReturn(valueOps);
		given(valueOps.get("approvalKey::koreainvestment")).willReturn("test-key");

		// when
		String result = approvalKeyService.get();

		// then
		assertEquals("test-key", result);
	}

	@Test
	void getApprovalKey_없으면_예외() {
		given(redisTemplate.opsForValue()).willReturn(valueOps);
		given(valueOps.get(anyString())).willReturn(null);

		assertThrows(CustomRuntimeException.class, () -> approvalKeyService.get());
	}
}