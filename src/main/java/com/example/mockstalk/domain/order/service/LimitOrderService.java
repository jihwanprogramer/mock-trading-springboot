package com.example.mockstalk.domain.order.service;

import java.math.BigDecimal;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.mockstalk.common.error.CustomRuntimeException;
import com.example.mockstalk.common.error.ExceptionCode;
import com.example.mockstalk.domain.account.entity.Accounts;
import com.example.mockstalk.domain.account.repository.AccountRepository;
import com.example.mockstalk.domain.holdings.entity.Holdings;
import com.example.mockstalk.domain.holdings.repository.HoldingsRepository;
import com.example.mockstalk.domain.order.dto.LimitOrderRequestDto;
import com.example.mockstalk.domain.order.dto.LimitOrderResponseDto;
import com.example.mockstalk.domain.order.entity.Order;
import com.example.mockstalk.domain.order.entity.OrderStatus;
import com.example.mockstalk.domain.order.entity.Type;
import com.example.mockstalk.domain.order.repository.OrderRepository;
import com.example.mockstalk.domain.stock.entity.Stock;
import com.example.mockstalk.domain.stock.repository.StockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LimitOrderService {

	private final OrderRepository orderRepository;
	private final AccountRepository accountRepository;
	private final StockRepository stockRepository;
	private final HoldingsRepository holdingsRepository;

	//로직 논리 정리(2차 통합 때 삭제 예정)
	//1.pathVariable 로 가져온 계정 id 로 조회
	//2.조회한 계좌가 로그인한 사용자의 계좌가 맞는지 확인
	//3.매수하기로한 stock 종목 조회
	//4.입력한 지정가와 수량을 계산하고
	//5.만족한다면 계좌 잔액을 확인하고 차감(주문완료 동시에 차감 -> 만약 주문취소하면 다시 잔액반환)
	//6.주문 생성하고 저장
	public LimitOrderResponseDto saveLimitBuy(UserDetails userDetails, Long accountId,
		LimitOrderRequestDto limitOrderRequestDto) {
		Accounts account = accountRepository.findById(accountId)
			.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.ACCOUNT_NOT_FOUND));

		if (!account.getUser().getEmail().equals(userDetails.getUsername())) {
			throw new CustomRuntimeException(ExceptionCode.UNAUTHORIZED_ACCOUNT_ACCESS);
		}

		Stock stock = stockRepository.findById(limitOrderRequestDto.getStockId())
			.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.STOCK_NOT_FOUND));

		BigDecimal totalPrice = limitOrderRequestDto.getLimitPrice()
			.multiply(BigDecimal.valueOf(limitOrderRequestDto.getQuantity()));

		if (account.getCurrentBalance().compareTo(totalPrice) < 0) {
			throw new CustomRuntimeException(ExceptionCode.INSUFFICIENT_BALANCE);
		}
		account.decreaseCurrentBalance(totalPrice);

		Order order = Order.builder()
			.account(account)
			.stock(stock)
			.type(Type.LIMIT_BUY) // 기존 MARKET_SELL -> LIMIT_BUY로 수정
			.quantity(limitOrderRequestDto.getQuantity())
			.price(totalPrice)
			.orderStatus(OrderStatus.COMPLETED)
			.build();

		orderRepository.save(order);
		return LimitOrderResponseDto.from(order);
	}

	//로직 논리 정리(2차 통합 때 삭제 예정)
	//1. pathVariable로 가져온 계정 id로 계좌 조회
	//2. 조회한 계좌가 로그인한 사용자의 계좌가 맞는지 확인
	//3. 매도하려는 stock 종목 조회
	//4. 해당 종목의 보유 수량 확인
	//5. 입력한 수량만큼 보유 주식에서 차감(주문완료 동시에 차감 -> 주문취소시 수량 복구 예정)
	//6. 주문 생성하고 저장
	public LimitOrderResponseDto saveLimitSell(UserDetails userDetails, Long accountId,
		LimitOrderRequestDto limitOrderRequestDto) {
		Accounts account = accountRepository.findById(accountId)
			.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.ACCOUNT_NOT_FOUND));

		if (!account.getUser().getEmail().equals(userDetails.getUsername())) {
			throw new CustomRuntimeException(ExceptionCode.UNAUTHORIZED_ACCOUNT_ACCESS);
		}

		Stock stock = stockRepository.findById(limitOrderRequestDto.getStockId())
			.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.STOCK_NOT_FOUND));

		Holdings holding = holdingsRepository.findByAccountsAndStock(account, stock)
			.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.HOLDINGS_NOT_FOUND));

		if (holding.getQuantity() < limitOrderRequestDto.getQuantity()) {
			throw new CustomRuntimeException(ExceptionCode.INSUFFICIENT_HOLDINGS);
		}

		BigDecimal totalPrice = limitOrderRequestDto.getLimitPrice()
			.multiply(BigDecimal.valueOf(limitOrderRequestDto.getQuantity()));

		Order order = Order.builder()
			.account(account)
			.stock(stock)
			.type(Type.LIMIT_SELL)
			.quantity(limitOrderRequestDto.getQuantity())
			.price(totalPrice)
			.orderStatus(OrderStatus.COMPLETED)
			.build();

		orderRepository.save(order);
		return LimitOrderResponseDto.from(order);
	}
}
