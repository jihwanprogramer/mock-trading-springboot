package com.example.mockstalk.domain.interest_stock.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mockstalk.common.response.ResponseMessage;
import com.example.mockstalk.domain.interest_stock.dto.request.InterestRequestDto;
import com.example.mockstalk.domain.interest_stock.service.InterestStockService;
import com.example.mockstalk.domain.user.service.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class InterestStockController {

	private final InterestStockService interestStockService;

	// 관심 종목 등록
	@PostMapping("/interests")
	public ResponseEntity<ResponseMessage<?>> addInterest(@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestBody InterestRequestDto dto) {
		interestStockService.addInterest(userDetails.getUser(), dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(ResponseMessage.success("관심종목 등록이 되었습니다."));
	}

	// 관심 종목 찾기
	@GetMapping("/interests")
	public ResponseEntity<ResponseMessage<?>> findInterest(
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		return ResponseEntity.ok(ResponseMessage.success(interestStockService.findInterest(userDetails.getUser())));
	}

	// 관심 종목 삭제
	@DeleteMapping("/interests/{interestId}")
	public ResponseEntity<ResponseMessage<?>> deleteInterest(@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable Long interestId) {
		interestStockService.deleteInterest(userDetails, interestId);
		return ResponseEntity.ok(ResponseMessage.success("관심 종목이 삭제 되었습니다."));
	}
}
