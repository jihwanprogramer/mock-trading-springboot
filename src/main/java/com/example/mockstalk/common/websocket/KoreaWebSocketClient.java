// package com.example.mockstalk.common.websocket;
//
// import java.net.URI;
// import java.time.Duration;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.concurrent.BlockingQueue;
// import java.util.concurrent.ExecutorService;
// import java.util.concurrent.Executors;
// import java.util.concurrent.LinkedBlockingQueue;
// import java.util.concurrent.TimeUnit;
//
// import org.springframework.data.redis.core.RedisTemplate;
//
// import com.example.mockstalk.common.error.CustomRuntimeException;
// import com.example.mockstalk.common.error.ExceptionCode;
// import com.example.mockstalk.domain.stock.repository.StockRepository;
// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.JsonNode;
// import com.fasterxml.jackson.databind.ObjectMapper;
//
// import jakarta.websocket.ClientEndpoint;
// import jakarta.websocket.CloseReason;
// import jakarta.websocket.ContainerProvider;
// import jakarta.websocket.OnClose;
// import jakarta.websocket.OnError;
// import jakarta.websocket.OnMessage;
// import jakarta.websocket.OnOpen;
// import jakarta.websocket.Session;
// import jakarta.websocket.WebSocketContainer;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
//
// @ClientEndpoint
// @RequiredArgsConstructor
// @Slf4j
// public class KoreaWebSocketClient {
// 	private Session session;
// 	private final ObjectMapper objectMapper = new ObjectMapper();
// 	private final RedisTemplate<String, Object> redisTemplate;
// 	private final StockRepository stockRepository;
// 	private boolean reconnecting = false;
// 	private final BlockingQueue<RetryMessage> messageQueue = new LinkedBlockingQueue<>();
//
// 	private static final int THREAD_COUNT = 10;
// 	private static final int MAX_RETRY = 5;
// 	private static final int WS_CONNECT_DELAY_MS = 200;
//
// 	public String getApprovalKey() {
// 		String approvalKey = (String)redisTemplate.opsForValue().get("approvalKey::koreainvestment");
// 		if (approvalKey == null || approvalKey.isBlank()) {
// 			throw new CustomRuntimeException(ExceptionCode.NOT_FOUND_APPROVALKEY);
// 		}
// 		return approvalKey;
// 	}
//
// 	private void connectWebSocket() throws Exception {
// 		WebSocketContainer container = ContainerProvider.getWebSocketContainer();
// 		String hantuUri = "ws://ops.koreainvestment.com:21000/tryitout/H0STCNT0";
// 		URI uri = new URI(hantuUri);
// 		container.connectToServer(this, uri);
// 		Thread.sleep(WS_CONNECT_DELAY_MS);
// 	}
//
// 	private List<String> getAllStockCodes() {
// 		return stockRepository.findAllStockCodes();
// 	}
//
// 	public BlockingQueue<RetryMessage> generateSubscribeMessages(List<String> stockCodes, String approvalKey,
// 		ExecutorService generatorPool) {
//
// 		// 병렬 메시지 생성
// 		for (String code : stockCodes) {
// 			generatorPool.submit(() -> {
// 				try {
// 					String msg = buildSubscribeMessage(code, approvalKey);
// 					messageQueue.put(new RetryMessage(msg, 0));
// 				} catch (Exception e) {
// 					System.err.println("메시지 생성 실패: " + code);
// 				}
// 			});
// 		}
// 		generatorPool.shutdown();
//
// 		return messageQueue;
// 	}
//
// 	private void startSenderThread(List<String> stockCodes, Long startTime, ExecutorService generatorPool) {
// 		// 전송 스레드
// 		new Thread(() -> {
// 			try {
// 				while (!generatorPool.isTerminated() || !messageQueue.isEmpty()) {
// 					RetryMessage retryMessage = messageQueue.poll(1, TimeUnit.SECONDS);
// 					if (retryMessage == null)
// 						continue;
//
// 					session.getAsyncRemote().sendText(retryMessage.message, result -> {
// 						if (!result.isOK()) {
// 							if (retryMessage.attempt < MAX_RETRY) {
// 								System.err.printf("전송 실패 (%d회차), 재시도 \n", retryMessage.attempt + 1);
// 								messageQueue.offer(new RetryMessage(retryMessage.message, retryMessage.attempt + 1));
// 							} else {
// 								System.err.println("최대 재시도 초과, 전송 포기");
// 							}
// 						} else {
// 							System.out.println("전송 성공");
// 						}
// 					});
// 				}
//
// 				long endTime = System.currentTimeMillis();
// 				System.out.printf("[구독 완료] 총 %d 종목, 처리 시간: %dms%n", stockCodes.size(), (endTime - startTime));
//
// 			} catch (Exception e) {
// 				System.err.println("전송 스레드 오류: " + e.getMessage());
// 			}
// 		}).start();
// 	}
//
// 	public void connect() throws Exception {
// 		String approvalKey = getApprovalKey();
// 		connectWebSocket();
// 		List<String> stockCodes = getAllStockCodes();
// 		long startTime = System.currentTimeMillis();
// 		ExecutorService generatorPool = Executors.newFixedThreadPool(THREAD_COUNT);
// 		generateSubscribeMessages(stockCodes, approvalKey, generatorPool);
// 		startSenderThread(stockCodes, startTime, generatorPool);
// 	}
//
// 	@OnOpen
// 	public void onOpen(Session session) {
// 		this.session = session;
// 		System.out.println("WebSocket 연결 완료");
// 	}
//
// 	@OnMessage
// 	public void onMessage(String message) {
// 		System.out.println("수신 메시지: " + message);
// 		try {
// 			// JSON 메시지와 파이프 구분 메시지 구분
// 			if (message.startsWith("{")) {
// 				// JSON 메시지 처리 (SUBSCRIBE SUCCESS 등)
// 				JsonNode root = objectMapper.readTree(message);
// 				String msgCd = root.path("body").path("msg_cd").asText();
//
// 				if ("OPSP0000".equals(msgCd)) {
// 					System.out.println("체결 데이터 아님. 저장 생략");
// 				}
//
// 				// 예외적인 JSON 메시지 처리 시 여기에 추가
// 			} else {
// 				// 파이프(|)로 구분된 실시간 체결 메시지 처리
// 				String[] parts = message.split("\\|");
//
// 				if (parts.length < 4) {
// 					System.out.println("형식 오류로 메시지 무시");
// 					return;
// 				}
//
// 				String data = parts[3]; // 체결데이터 구간
//
// 				String[] fields = data.split("\\^");
// 				if (fields.length < 3) {
// 					System.out.println("필드 수 부족으로 저장 생략");
// 					return;
// 				}
//
// 				String code = fields[0];        // 종목코드
// 				String price = fields[2];       // 현재가
//
// 				redisTemplate.opsForValue().set("stockPrice:" + code, price, Duration.ofMinutes(5));
// 				System.out.printf("[%s] 현재가 %s 저장됨%n", code, price);
// 			}
//
// 		} catch (JsonProcessingException e) {
// 			log.warn("JSON 파싱 오류 발생: {}", message, e);
// 			redisTemplate.opsForSet().add("errorMessages", message); // 사후 분석용 저장
// 		} catch (Exception e) {
// 			log.error("예상치 못한 오류 발생: {}", message, e);
// 		}
// 	}
//
// 	public String buildSubscribeMessage(String stockCode, String approvalKey) throws Exception {
// 		Map<String, Object> header = new HashMap<>();
// 		header.put("approval_key", approvalKey);
// 		header.put("custtype", "P");
// 		header.put("tr_type", "1");
// 		header.put("content-type", "utf-8");
//
// 		Map<String, Object> input = new HashMap<>();
// 		input.put("tr_id", "H0STCNT0");
// 		input.put("tr_key", stockCode);
//
// 		Map<String, Object> body = new HashMap<>();
// 		body.put("input", input);
//
// 		Map<String, Object> message = new HashMap<>();
// 		message.put("header", header);
// 		message.put("body", body);
//
// 		return objectMapper.writeValueAsString(message);
// 	}
//
// 	private void attemptReconnect() {
// 		if (reconnecting)
// 			return;
// 		reconnecting = true;
//
// 		new Thread(() -> {
// 			int retryDelay = 3000;
// 			while (true) {
// 				try {
// 					System.out.println("WebSocket 재연결 시도 중...");
// 					connect();
// 					reconnecting = false;
// 					System.out.println("WebSocket 재연결 성공");
// 					break;
// 				} catch (Exception e) {
// 					System.err.println("재연결 실패, " + (retryDelay / 1000) + "초 후 재시도");
// 					try {
// 						Thread.sleep(retryDelay);
// 					} catch (InterruptedException ie) {
// 						break;
// 					}
// 				}
// 			}
// 		}).start();
// 	}
//
// 	@OnClose
// 	public void onClose(Session session, CloseReason reason) {
// 		System.out.println("WebSocket 종료됨: " + reason);
// 		attemptReconnect();
// 	}
//
// 	@OnError
// 	public void onError(Session session, Throwable t) {
// 		System.err.println("WebSocket 오류: " + t.getMessage());
// 		attemptReconnect();
// 	}
//
// 	private static class RetryMessage {
// 		final String message;
// 		final int attempt;
//
// 		RetryMessage(String message, int attempt) {
// 			this.message = message;
// 			this.attempt = attempt;
// 		}
// 	}
// }
