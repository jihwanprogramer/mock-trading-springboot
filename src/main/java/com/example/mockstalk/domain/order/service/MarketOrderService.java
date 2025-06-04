package com.example.mockstalk.domain.order.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.mockstalk.common.error.CustomRuntimeException;
import com.example.mockstalk.common.error.ExceptionCode;
import com.example.mockstalk.domain.account.entity.Account;
import com.example.mockstalk.domain.account.repository.AccountRepository;
import com.example.mockstalk.domain.holdings.entity.Holdings;
import com.example.mockstalk.domain.holdings.repository.HoldingsRepository;
import com.example.mockstalk.domain.order.dto.MarketOrderRequestDto;
import com.example.mockstalk.domain.order.dto.MarketOrderResponseDto;
import com.example.mockstalk.domain.order.entity.Order;
import com.example.mockstalk.domain.order.entity.OrderStatus;
import com.example.mockstalk.domain.order.entity.Type;
import com.example.mockstalk.domain.order.repository.OrderRepository;
import com.example.mockstalk.domain.stock.entity.Stock;
import com.example.mockstalk.domain.stock.repository.StockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MarketOrderService {

	private final OrderRepository orderRepository;
	private final AccountRepository accountRepository;
	private final StockRepository stockRepository;
	private final HoldingsRepository holdingsRepository;

	//로직 흐름 정리
	// 계좌 조회 -> 주식정보 조회 -> 현재 주식 가격조회 ->총 매수 금액 계산 -> 계좌 잔고 차감(엔티티에서 예외처리) ->주문객체 생성 및 저장 -> 응답 dto 반환
	public MarketOrderResponseDto saveMarketBuy(UserDetails userDetails, Long accountId,
		MarketOrderRequestDto marketOrderRequestDto) {

		Account account = accountRepository.findById(accountId).orElseThrow(() -> new CustomRuntimeException(
			ExceptionCode.ACCOUNT_NOT_FOUND //임시 예외처리코드
		));

		if (!account.getUser().getEmail().equals(userDetails.getUsername())) {
			throw new CustomRuntimeException(ExceptionCode.UNAUTHORIZED_ACCOUNT_ACCESS);
		}

		Stock stock = stockRepository.findById(marketOrderRequestDto.getStockId())
			.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.STOCK_NOT_FOUND)); //임시 예외처리코드
		//BigDecimal totalPrice =stock 가격 * marketOrderRequestDto.getQuantity();

		//account.decreaseCurrentBalance(BigDecimal.valueOf(totalPrice));

		Order order = Order.builder()
			.account(account)
			.stock(stock)
			.type(Type.MARKET_BUY)
			//주식 가격 불러오는거 추후 추가
			.quantity(marketOrderRequestDto.getQuantity())
			.price(null)
			.orderStatus(
				OrderStatus.COMPLETED) //고민해야할 부분: 시장가라 빠른 매수가 가능하지만 대량의 주문이 들어오면 조금의 지연이라도 존재할테니 Pending 으로 처리하는게맞나?
			.build();

		orderRepository.save(order);
		return MarketOrderResponseDto.from(order);
	}

	public MarketOrderResponseDto saveMarketSell(UserDetails userDetails, Long accountId,
		MarketOrderRequestDto marketOrderRequestDto) {

		Account account = accountRepository.findById(accountId).orElseThrow(() -> new CustomRuntimeException(
			ExceptionCode.ACCOUNT_NOT_FOUND //임시 예외처리코드
		));

		if (!account.getUser().getEmail().equals(userDetails.getUsername())) {
			throw new CustomRuntimeException(ExceptionCode.UNAUTHORIZED_ACCOUNT_ACCESS);
		}

		Stock stock = stockRepository.findById(marketOrderRequestDto.getStockId())
			.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.STOCK_NOT_FOUND));

		Holdings holding = holdingsRepository.findByAccountAndStock(account, stock)
			.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.HOLDINGS_NOT_FOUND));

		holding.decreaseQuantity(marketOrderRequestDto.getQuantity());

		Order order = Order.builder()
			.account(account)
			.stock(stock)
			.type(Type.MARKET_SELL)
			//주식 가격 불러오는거 추후 추가
			.quantity(marketOrderRequestDto.getQuantity())
			.price(null)
			.orderStatus(OrderStatus.COMPLETED)
			.build();

		orderRepository.save(order);
		return MarketOrderResponseDto.from(order);
	}
}
