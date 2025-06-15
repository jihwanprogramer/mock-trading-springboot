package com.example.mockstalk.domain.price.intraday_candles.websocket;

import java.net.URI;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.mockstalk.common.hantutoken.TokenService;
import com.example.mockstalk.domain.stock.repository.StockRepository;

import jakarta.annotation.PostConstruct;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.CloseReason;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import lombok.RequiredArgsConstructor;

@Component
@ClientEndpoint
@RequiredArgsConstructor
public class HantuWebSocketClient {

	private Session session;
	private final IntradayCandleProcessor candleProcessor;
	private final TokenService tokenService;
	private final StockRepository stockRepository;

	@PostConstruct
	public void init() {
		connect();
	}

	public void connect() {
		String url = "wss://openapi.koreainvestment.com:9443/websocket";
		WebSocketContainer container = ContainerProvider.getWebSocketContainer();

		try {
			container.connectToServer(this, new URI(url));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@OnOpen
	public void onOpen(Session session) {
		this.session = session;
		System.out.println("웹소켓 연결 성공");

		String token = tokenService.getAccessToken().getAccess_token();
		List<String> stockCodes = stockRepository.findAllStockCodes();

		for (String code : stockCodes) {
			String msg = String.format(
				"{\"header\": {\"token\": \"%s\", \"tr_type\": \"1\", \"tr_id\": \"FHKST01010100\" }, " +
					"\"body\": {\"input\": {\"tr_id\": \"FHKST01010100\", \"tr_key\": \"%s\"}}}",
				token, code
			);
			session.getAsyncRemote().sendText(msg);
		}
	}

	@OnMessage
	public void onMessage(String message) {
		candleProcessor.process(message);
	}

	@OnError
	public void onError(Session session, Throwable throwable) {
		System.err.println("오류 발생: " + throwable.getMessage());
	}

	@OnClose
	private void onClose(Session session, CloseReason reason) {
		System.out.println("연결 종료됨: " + reason.getReasonPhrase());
	}
}
