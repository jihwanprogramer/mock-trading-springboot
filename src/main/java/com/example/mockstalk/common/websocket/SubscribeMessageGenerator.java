package com.example.mockstalk.common.websocket;

//
//@Component
//@RequiredArgsConstructor
//public class SubscribeMessageGenerator {
//	private final ObjectMapper objectMapper;
//
//	public BlockingQueue<MessageSender.RetryMessage> generate(List<String> codes, String approvalKey,
//		ExecutorService pool) {
//		BlockingQueue<MessageSender.RetryMessage> queue = new LinkedBlockingQueue<>();
//
//		for (String code : codes) {
//			pool.submit(() -> {
//				try {
//					String msg = build(code, approvalKey);
//					queue.put(new MessageSender.RetryMessage(msg, 0));
//				} catch (Exception e) {
//					System.err.println("메시지 생성 실패: " + code);
//				}
//			});
//		}
//
//		pool.shutdown();
//		return queue;
//	}
//
//	public String build(String stockCode, String approvalKey) throws Exception {
//		Map<String, Object> header = new HashMap<>();
//		header.put("approval_key", approvalKey);
//		header.put("custtype", "P");
//		header.put("tr_type", "1");
//		header.put("content-type", "utf-8");
//
//		Map<String, Object> input = new HashMap<>();
//		input.put("tr_id", "H0STCNT0");
//		input.put("tr_key", stockCode);
//
//		Map<String, Object> body = new HashMap<>();
//		body.put("input", input);
//
//		Map<String, Object> msg = new HashMap<>();
//		msg.put("header", header);
//		msg.put("body", body);
//
//		return objectMapper.writeValueAsString(msg);
//	}
//}
