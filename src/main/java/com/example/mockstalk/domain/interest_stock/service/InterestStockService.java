package com.example.mockstalk.domain.interest_stock.service;

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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InterestStockService {

    private final InterestStockRepository interestStockRepository;
    private final StockRepository stockRepository;

    public void addInterest(User user, InterestRequestDto dto) {

       Stock stock = stockRepository.findByStockNameAndStockCode(dto.getStockName(),dto.getStockCode())
               .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 종목 입니다."));

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
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관심종목 입니다."));
        // 현재 유저의 관심 종목인지 확인
        if (!interest.getUser().getId().equals(userDetails.getUser().getId())) {
            throw new IllegalArgumentException("현재 유저의 관심 종목이 아닙니다.");
        }

        interestStockRepository.delete(interest);

    }
}
