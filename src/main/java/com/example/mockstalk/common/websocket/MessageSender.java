// package com.example.mockstalk.common.websocket;
//
// import java.io.IOException;
// import java.net.URI;
// import java.time.Duration;
// import java.util.List;
// import java.util.concurrent.BlockingQueue;
// import java.util.concurrent.ExecutorService;
// import java.util.concurrent.Executors;
// import java.util.concurrent.LinkedBlockingQueue;
// import java.util.concurrent.TimeUnit;
//
// import org.springframework.data.redis.core.RedisTemplate;
// import org.springframework.stereotype.Component;
//
// import com.example.mockstalk.domain.stock.repository.StockRepository;
// import com.fasterxml.jackson.databind.JsonNode;
// import com.fasterxml.jackson.databind.ObjectMapper;
//
// import jakarta.annotation.PostConstruct;
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
// @Slf4j
// @Component
// @RequiredArgsConstructor
// @ClientEndpoint
// public class MessageSender {
// 	private Session session;
// 	private final StockRepository stockRepository;
// 	private final SubscribeMessageGenerator buildSubscribeMessage;
// 	private final ApprovalKeyService approvalKeyService;
// 	private BlockingQueue<RetryMessage> messageQueue;
// 	private final BlockingQueue<String> receivedMessageQueue = new LinkedBlockingQueue<>();
// 	private final ExecutorService consumerPool = Executors.newFixedThreadPool(5);
// 	private int totalStockCount;
// 	private boolean reconnecting = false;
//
// 	private final ObjectMapper objectMapper;
// 	private final RedisTemplate<String, Object> redisTemplate;
//
// 	@PostConstruct
// 	public void startMessageProcessor() {
// 		for (int i = 0; i < 5; i++) {
// 			consumerPool.submit(() -> {
// 				while (true) {
// 					try {
// 						String message = receivedMessageQueue.take();
// 						handlePriceMessage(message);
// 					} catch (Exception e) {
// 						log.error("수신 메시지 처리 실패", e);
// 					}
// 				}
// 			});
// 		}
// 	}
//
// 	public void start(BlockingQueue<RetryMessage> queue, int stockCount) {
// 		this.messageQueue = queue;
// 		this.totalStockCount = stockCount;
// 		new Thread(this::runSender).start();
// 	}
//
// 	private void runSender() {
// 		try {
// 			while (!messageQueue.isEmpty()) {
// 				RetryMessage msg = messageQueue.poll(1, TimeUnit.SECONDS);
// 				if (msg == null)
// 					continue;
//
// 				session.getAsyncRemote().sendText(msg.message, result -> {
// 					if (!result.isOK()) {
// 						if (msg.attempt < 5) {
// 							messageQueue.offer(new RetryMessage(msg.message, msg.attempt + 1));
// 						} else {
// 							log.warn("전송 실패: 재시도 초과");
// 						}
// 					}
// 				});
// 			}
// 			log.info("[구독 완료] 총 {} 종목", totalStockCount);
// 		} catch (Exception e) {
// 			log.error("전송 스레드 오류", e);
// 		}
// 	}
//
// 	@OnOpen
// 	public void onOpen(Session session) {
// 		this.session = session;
// 		log.info("WebSocket 연결 완료");
// 	}
//
// 	@OnMessage
// 	public void onMessage(String message) {
// 		log.info("전체 수신 메시지: {}", message);
// 		try {
// 			if (!message.startsWith("{")) {
// 				receivedMessageQueue.put(message);
// 			} else {
// 				// JSON 메시지는 기존대로 바로 처리
// 				JsonNode root = objectMapper.readTree(message);
// 				String msgCd = root.path("body").path("msg_cd").asText();
// 				if ("OPSP0000".equals(msgCd)) {
// 					System.out.println("체결 데이터 아님. 저장 생략");
// 				}
// 			}
// 		} catch (Exception e) {
// 			log.error("메시지 큐 추가 또는 JSON 처리 오류", e);
// 		}
// 	}
//
// 	@OnClose
// 	public void onClose(Session session, CloseReason reason) {
// 		log.warn("WebSocket 종료됨: {}", reason);
// 		attemptReconnect();
// 	}
//
// 	@OnError
// 	public void onError(Session session, Throwable t) {
// 		log.error("WebSocket 오류 발생", t);
// 		attemptReconnect();
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
// 					Thread.sleep(retryDelay);
// 					WebSocketContainer container = ContainerProvider.getWebSocketContainer();
// 					container.connectToServer(this, new URI("ws://ops.koreainvestment.com:21000/tryitout/H0STCNT0"));
// 					reconnecting = false;
//
// 					log.info("WebSocket 재연결 성공");
// 					subscribeAll();  // 구독 재요청
//
// 					break;
// 				} catch (Exception e) {
// 					log.warn("WebSocket 재연결 실패, 재시도 대기중...", e);
// 					retryDelay = Math.min(retryDelay * 2, 30000);  // 최대 30초
// 				}
// 			}
// 		}).start();
// 	}
//
// 	private void subscribeAll() throws Exception {
// 		List<String> stockCodes = stockRepository.findAllStockCodes(); // 구독 대상 종목 코드 목록
// 		String approvalKey = approvalKeyService.get();
//
// 		for (String code : stockCodes) {
// 			String subscribeMessage = buildSubscribeMessage.build(code, approvalKey);
// 			sendMessage(subscribeMessage);
// 		}
//
// 		log.info("총 {}건의 종목에 재구독 요청을 보냈습니다.", stockCodes.size());
// 	}
//
// 	private void sendMessage(String message) {
// 		try {
// 			if (session != null && session.isOpen()) {
// 				session.getBasicRemote().sendText(message);
// 			} else {
// 				log.warn("WebSocket 세션이 닫혀있어 메시지를 보낼 수 없습니다.");
// 			}
// 		} catch (IOException e) {
// 			log.error("WebSocket 메시지 전송 실패", e);
// 		}
// 	}
//
// 	private void handlePriceMessage(String message) {
// 		try {
// 			String[] parts = message.split("\\|");
// 			if (parts.length < 4)
// 				return;
//
// 			String[] fields = parts[3].split("\\^");
// 			if (fields.length < 3)
// 				return;
//
// 			String code = fields[0];
// 			String price = fields[2];
//
// 			redisTemplate.opsForValue().set("stockPrice:" + code, price, Duration.ofMinutes(5));
// 			log.info("[{}] 현재가 {} 저장 완료", code, price);
// 		} catch (Exception e) {
// 			log.warn("가격 메시지 파싱 실패: {}", message, e);
// 		}
// 	}
//
// 	public record RetryMessage(String message, int attempt) {
// 	}
// }

