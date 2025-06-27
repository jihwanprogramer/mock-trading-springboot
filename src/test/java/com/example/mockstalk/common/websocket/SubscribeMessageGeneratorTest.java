package com.example.mockstalk.common.websocket;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class SubscribeMessageGeneratorTest {

	@InjectMocks
	private SubscribeMessageGenerator generator;

	@Test
	void buildSubscribeMessage_정상작동() throws Exception {
		// given
		String stockCode = "005930";
		String approvalKey = "key";

		// when
		String json = generator.build(stockCode, approvalKey);

		// then
		JsonNode node = new ObjectMapper().readTree(json);
		assertEquals(stockCode, node.path("body").path("input").path("tr_key").asText());
		assertEquals(approvalKey, node.path("header").path("approval_key").asText());
	}
}