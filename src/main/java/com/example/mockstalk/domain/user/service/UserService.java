package com.example.mockstalk.domain.user.service;

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
	private final JwtUtil jwtUtil;

	public void signup(SignupRequestDto dto) {
		if (userRepository.existsByEmail(dto.getEmail())) {
			throw new CustomRuntimeException(ExceptionCode.EMAIL_ALREADY_EXISTS);
		}
		String encode = passwordEncoder.encode(dto.getPassword());
		User user = new User(dto.getEmail(), encode, dto.getNickname(), dto.getWalletAddress(), dto.getUserRole());
		userRepository.save(user);

	}

	public LoginResponseDto login(LoginRequestDto dto) {
		User user = userRepository.findByEmail(dto.getEmail())
			.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.NOT_FOUND_EMAIL));
		String accessToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole());
		String refreshToken = jwtUtil.createRefreshToken(user.getId());

		return new LoginResponseDto(user.getId(), accessToken, refreshToken);

	}

	public FindResponseDto findMe(String email) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication.getPrincipal() == null) {
			throw new CustomRuntimeException(ExceptionCode.USER_MISMATCH_EXCEPTION);
		}

		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new CustomRuntimeException(ExceptionCode.NOT_FOUND_EMAIL));

		return new FindResponseDto(user);

	}

	@Transactional
	public void deleteMe(String email, DeleteRequestDto dto) {

		if (email == null) {
			throw new IllegalArgumentException("인증된 사용자가 없습니다");
		}
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new IllegalArgumentException("이메일을 찾을 수 없습니다."));
		if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
			throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
		}

		userRepository.deleteUserByEmail(email);

	}

	public FindResponseDto findByWallet(String walletAddress) {
		User user = userRepository.findByWalletAddress(walletAddress)
			.orElseThrow(() -> new IllegalArgumentException("지갑주소를 찾을 수 없습니다."));

		return new FindResponseDto(user);
	}

	@Transactional
	public void updateMe(String email, UpdateRequestDto dto) {

		if (email == null) {
			throw new IllegalArgumentException("인증된 사용자가 없습니다");
		}

		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new IllegalArgumentException("이메일을 찾을 수 없습니다."));

		if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
			throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
		}

		String encodedNewPassword = passwordEncoder.encode(dto.getNewPassword());

		user.updateUser(dto.getNickname(), encodedNewPassword);
	}
}
