// package com.example.mockstalk.common.websocket;
//
// import java.net.URI;
// import java.util.List;
// import java.util.concurrent.BlockingQueue;
// import java.util.concurrent.ExecutorService;
// import java.util.concurrent.Executors;
//
// import org.springframework.stereotype.Component;
//
// import com.example.mockstalk.domain.stock.repository.StockRepository;
//
// import jakarta.websocket.ContainerProvider;
// import jakarta.websocket.WebSocketContainer;
// import lombok.RequiredArgsConstructor;
//
// @Component
// @RequiredArgsConstructor
// public class WebSocketClientManager {
// 	private final ApprovalKeyService approvalKeyService;
// 	private final StockRepository stockRepository;
// 	private final SubscribeMessageGenerator messageGenerator;
// 	private final MessageSender messageSender;
//
// 	private static final String WS_URI = "ws://ops.koreainvestment.com:21000/tryitout/H0STCNT0";
// 	private static final int THREAD_COUNT = 10;
//
// 	public void connect() throws Exception {
// 		String approvalKey = approvalKeyService.get();
//
// 		WebSocketContainer container = ContainerProvider.getWebSocketContainer();
// 		URI uri = new URI(WS_URI);
// 		container.connectToServer(messageSender, uri);
// 		Thread.sleep(200);
//
// 		List<String> stockCodes = stockRepository.findAllStockCodes();
// 		ExecutorService generatorPool = Executors.newFixedThreadPool(THREAD_COUNT);
// 		BlockingQueue<MessageSender.RetryMessage> queue = messageGenerator.generate(stockCodes, approvalKey,
// 			generatorPool);
//
// 		messageSender.start(queue, stockCodes.size());
// 		messageSender.startMessageProcessor();
// 	}
// }
//

