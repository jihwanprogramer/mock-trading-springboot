package com.example.mockstalk.domain.interest_stock.controller;


import com.example.mockstalk.domain.interest_stock.dto.request.InterestRequestDto;
import com.example.mockstalk.domain.interest_stock.dto.response.InterestResponseDto;
import com.example.mockstalk.domain.interest_stock.service.InterestStockService;
import com.example.mockstalk.domain.user.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class InterestStockController {

    private final InterestStockService interestStockService;

    // 관심 종목 등록
    @PostMapping("/interests")
    public ResponseEntity<String> addInterest(@AuthenticationPrincipal CustomUserDetails userDetails,
                                       @RequestBody InterestRequestDto dto){
        interestStockService.addInterest(userDetails.getUser(),dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("관심 종목 등록 완료");
    }
    // 관심 종목 찾기
    @GetMapping("/interests")
    public ResponseEntity<List<InterestResponseDto>> findInterest(@AuthenticationPrincipal CustomUserDetails userDetails){

        return ResponseEntity.ok(interestStockService.findInterest(userDetails.getUser()));
    }
    // 관심 종목 삭제
    @DeleteMapping("/interests/{interestId}")
    public ResponseEntity<Void> deleteInterest(@AuthenticationPrincipal CustomUserDetails userDetails,
                                               @PathVariable Long interestId){
        interestStockService.deleteInterest(userDetails,interestId);
        return ResponseEntity.noContent().build();
    }
}
