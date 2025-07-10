package com.example.mockstalk.domain.user.controller;

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
import org.springframework.web.bind.annotation.RestController;

import com.example.mockstalk.common.response.ResponseMessage;
import com.example.mockstalk.domain.user.dto.request.DeleteRequestDto;
import com.example.mockstalk.domain.user.dto.request.SignupRequestDto;
import com.example.mockstalk.domain.user.dto.request.UpdateRequestDto;
import com.example.mockstalk.domain.auth.security.CustomUserDetails;
import com.example.mockstalk.domain.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;

	// 회원가입
	@PostMapping("/signup")
	public ResponseEntity<ResponseMessage<?>> signup(@Valid @RequestBody SignupRequestDto dto) {
		userService.signup(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(ResponseMessage.success("회원가입 완료."));
	}

	// 프로필 조회
	@GetMapping("/me")
	public ResponseEntity<ResponseMessage<?>> findMe(@AuthenticationPrincipal CustomUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		return ResponseEntity.ok(ResponseMessage.success("정보 조회", userService.findMe(userId)));
	}

	// 회원탈퇴
	@DeleteMapping("/me")
	public ResponseEntity<ResponseMessage<?>> deleteMe(@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestBody DeleteRequestDto dto) {
		String email = userDetails.getUser().getEmail();
		userService.deleteMe(email, dto);
		return ResponseEntity.ok(ResponseMessage.success("탈퇴가 완료 되었습니다."));
	}

	// 타 유저조회 지갑주소로
	@GetMapping("/wallet/{walletAddress}")
	public ResponseEntity<ResponseMessage<?>> findByWallet(@PathVariable String walletAddress) {
		return ResponseEntity.ok(ResponseMessage.success("타 유저 조회", userService.findByWallet(walletAddress)));
	}

	// 유저 정보 수정
	@PatchMapping("/me")
	public ResponseEntity<ResponseMessage<?>> updateMe(@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestBody UpdateRequestDto dto) {
		Long userId = userDetails.getUser().getId();
		userService.updateMe(userId, dto);
		return ResponseEntity.ok(ResponseMessage.success("유저 정보 수정 완료"));
	}

}