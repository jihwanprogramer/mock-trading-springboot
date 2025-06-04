package com.example.mockstalk.domain.interest_stock.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.mockstalk.common.error.CustomRuntimeException;
import com.example.mockstalk.common.error.ExceptionCode;
import com.example.mockstalk.domain.interest_stock.dto.request.InterestRequestDto;
import com.example.mockstalk.domain.interest_stock.dto.response.InterestResponseDto;
import com.example.mockstalk.domain.interest_stock.entity.InterestStock;
import com.example.mockstalk.domain.interest_stock.repository.InterestStockRepository;
import com.example.mockstalk.domain.stock.entity.Stock;
import com.example.mockstalk.domain.stock.repository.StockRepository;
import com.example.mockstalk.domain.user.entity.User;
import com.example.mockstalk.domain.user.service.CustomUserDetails;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InterestStockService {

	private final InterestStockRepository interestStockRepository;
	private final StockRepository stockRepository;

	public void addInterest(User user, InterestRequestDto dto) {

		Stock stock = stockRepository.findByStockNameAndStockCode(dto.getStockName(), dto.getStockCode())
			.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.STOCK_NOT_FOUND));

		InterestStock interest = new InterestStock(user, stock);
		interestStockRepository.save(interest);

	}

	@Transactional
	public List<InterestResponseDto> findInterest(User user) {
		List<InterestResponseDto> responseList = new ArrayList<>();
		List<InterestStock> list = interestStockRepository.findAllByUser(user);

		for (InterestStock i : list) {
			InterestResponseDto dto = new InterestResponseDto(
				i.getStock().getStockName(),
				i.getStock().getStockCode()
			);
			responseList.add(dto);
		}

		return responseList;

	}

	public void deleteInterest(CustomUserDetails userDetails, Long interestId) {
		InterestStock interest = interestStockRepository.findById(interestId)
			.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.STOCK_NOT_FOUND));
		// 현재 유저의 관심 종목인지 확인
		if (!interest.getUser().getId().equals(userDetails.getUser().getId())) {
			throw new CustomRuntimeException(ExceptionCode.USER_MISMATCH_EXCEPTION);
		}

		interestStockRepository.delete(interest);

	}
}
