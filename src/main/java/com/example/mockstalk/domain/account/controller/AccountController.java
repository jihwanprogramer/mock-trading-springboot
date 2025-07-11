package com.example.mockstalk.domain.account.controller;

import java.math.BigDecimal;

import com.example.mockstalk.domain.auth.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.mockstalk.common.response.ResponseMessage;
import com.example.mockstalk.domain.account.dto.AccountRequestDto;
import com.example.mockstalk.domain.account.dto.AccountSignInRequestDto;
import com.example.mockstalk.domain.account.dto.UpdateAccountRequestDto;
import com.example.mockstalk.domain.account.service.AccountService;
import com.example.mockstalk.domain.account.service.RankService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
public class AccountController {

	private final AccountService accountService;
	private final RankService rankService;

	/**
	 계좌 생성
	 **/
	@PostMapping
	public ResponseEntity<ResponseMessage<?>> saveAccount(@RequestBody AccountRequestDto requestDto,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		accountService.saveAccount(requestDto, userDetails);

		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(ResponseMessage.success("계좌가 정상적으로 생성되었습니다."));
	}

	/**
	 계좌 정보 가져오기. <- 해당 계좌로 로그인 <- 관련 계좌 정보 가져오기
	 1차 통합 이후 구현 예정
	 **/
	@PostMapping("/sign")
	public ResponseEntity<ResponseMessage<?>> signInAccount(
		@RequestBody AccountSignInRequestDto requestDto,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseMessage.success(accountService.loginAccount(requestDto, userDetails)));
	}

	/**
	 계좌 단건 조회
	 로그인 한 User가 해당 계좌의 주인인지 검증하는 로직이 필요.
	 **/
	@GetMapping("/{id}")
	public ResponseEntity<ResponseMessage<?>> getAccount(@PathVariable Long id) {

		accountService.findAccountById(id);

		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseMessage.success("해당 계좌 정보가 정상적으로 조회되었습니다.", accountService.findAccountById(id)));
	}

	/**
	 현재 로그인 계좌 정보 조회
	 **/
	@GetMapping("/info")
	public ResponseEntity<ResponseMessage<?>> getLoginAccountInfo() {
		accountService.findAccountByToken();

		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseMessage.success("해당 계좌 정보가 정상적으로 조회되었습니다.", accountService.findAccountByToken()));
	}

	/**
	 계좌 다건 조회
	 **/
	@GetMapping
	public ResponseEntity<ResponseMessage<?>> getAccount(@AuthenticationPrincipal CustomUserDetails userDetails) {

		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseMessage.success("해당 사용자님의 계좌목록이 정상적으로 조회되었습니다.", accountService.findAccount(userDetails)));
	}

	/**
	 선택 계좌 보유 종목 조회
	 **/
	@GetMapping("/{id}/holdings")
	public ResponseEntity<ResponseMessage<?>> getHoldings(@PathVariable Long id) {

		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseMessage.success("해당 계좌의 보유 종목이 정상적으로 조회되었습니다.", accountService.findHoldingsById(id)));
	}

	/**
	 선택 계좌 삭제
	 **/
	@DeleteMapping("/{id}")
	public ResponseEntity<ResponseMessage<?>> deleteAccount(@PathVariable Long id) {

		accountService.deleteAccount(id);

		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseMessage.success("해당 계좌가 정상적으로 삭제되었습니다."));
	}

	/**
	 선택 계좌 비밀번호 변경
	 **/
	@PatchMapping("/{id}")
	public ResponseEntity<ResponseMessage<?>> updateAccountPassword
	(@PathVariable Long id, @RequestBody UpdateAccountRequestDto requestDto) {

		accountService.updateAccountPassword(id, requestDto);

		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseMessage.success("해당 계좌의 비밀번호가 정상적으로 변경되었습니다."));
	}

	/**
	 * 수익률 전체 랭킹 조회
	 * @param topN
	 * @return
	 */
	@GetMapping("/ranking")
	public ResponseEntity<ResponseMessage<?>> getTopUserProfitRankings(@RequestParam int topN) {
		rankService.getTopUserProfitRankings(topN);

		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseMessage.success("정상적으로 수익률 전체 랭킹을 조회했습니다.", rankService.getTopUserProfitRankings(topN)));
	}

	/**
	 * 초기 자산 기준 수익률 랭킹 조회
	 * @param topN
	 * @param targetInitialBalance
	 * @return
	 */
	@GetMapping("/ranking/initial")
	public ResponseEntity<ResponseMessage<?>> get(
		@RequestParam int topN,
		@RequestParam("initialBalance") BigDecimal targetInitialBalance) {

		rankService.getTopUserProfitRankingsByInitialBalance(topN, targetInitialBalance);

		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseMessage.success("정상적으로 초기 자산 기준 수익률 랭킹을 조회했습니다.",
				rankService.getTopUserProfitRankingsByInitialBalance(topN, targetInitialBalance)));
	}
}
