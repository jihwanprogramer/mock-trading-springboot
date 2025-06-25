package com.example.mockstalk.domain.user.service;

import com.example.mockstalk.common.jwttoken.JwtTokenService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.mockstalk.common.config.JwtUtil;
import com.example.mockstalk.common.error.CustomRuntimeException;
import com.example.mockstalk.common.error.ExceptionCode;
import com.example.mockstalk.domain.user.dto.request.DeleteRequestDto;
import com.example.mockstalk.domain.user.dto.request.LoginRequestDto;
import com.example.mockstalk.domain.user.dto.request.SignupRequestDto;
import com.example.mockstalk.domain.user.dto.request.UpdateRequestDto;
import com.example.mockstalk.domain.user.dto.response.FindResponseDto;
import com.example.mockstalk.domain.user.dto.response.LoginResponseDto;
import com.example.mockstalk.domain.user.entity.User;
import com.example.mockstalk.domain.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenService tokenService;


	public void signup(SignupRequestDto dto) {
		if (userRepository.existsByEmail(dto.getEmail())) {
			throw new CustomRuntimeException(ExceptionCode.EMAIL_ALREADY_EXISTS);
		}
		String encode = passwordEncoder.encode(dto.getPassword());
		User user = new User(dto.getEmail(), encode, dto.getNickname(), dto.getWalletAddress(), dto.getUserRole());
		userRepository.save(user);

	}

	public FindResponseDto findMe(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.NOT_FOUND_USER));
		return new FindResponseDto(user);
	}

	@Transactional
	public void deleteMe(String email, DeleteRequestDto dto) {

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.NOT_FOUND_USER));

		if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
			throw new CustomRuntimeException(ExceptionCode.INVALID_PASSWORD);
		}
		tokenService.deleteRefreshToken(user.getId());
		userRepository.delete(user);

	}

	public FindResponseDto findByWallet(String walletAddress) {
		User user = userRepository.findByWalletAddress(walletAddress)
				.orElseThrow(() -> new IllegalArgumentException("지갑주소를 찾을 수 없습니다."));

		return new FindResponseDto(user);
	}

	@Transactional
	public void updateMe(Long userId, UpdateRequestDto dto) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.NOT_FOUND_USER));

		if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
			throw new CustomRuntimeException(ExceptionCode.INVALID_PASSWORD);
		}
		String encodedNewPassword = passwordEncoder.encode(dto.getNewPassword());
		user.updateUser(dto.getNickname(), encodedNewPassword);
	}


}
