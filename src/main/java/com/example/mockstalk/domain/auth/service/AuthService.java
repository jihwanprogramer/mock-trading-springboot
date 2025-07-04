package com.example.mockstalk.domain.auth.service;

import com.example.mockstalk.domain.auth.jwt.JwtUtil;
import com.example.mockstalk.common.error.CustomRuntimeException;
import com.example.mockstalk.common.error.ExceptionCode;
import com.example.mockstalk.domain.auth.dto.request.LoginRequestDto;
import com.example.mockstalk.domain.auth.dto.response.LoginResponseDto;
import com.example.mockstalk.domain.user.entity.User;
import com.example.mockstalk.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final JwtTokenService tokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponseDto login(LoginRequestDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new CustomRuntimeException(ExceptionCode.NOT_FOUND_EMAIL));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new CustomRuntimeException(ExceptionCode.INVALID_PASSWORD);
        }

        String accessToken = jwtUtil.createToken(user.getId());
        String refreshTokenWithBearer = jwtUtil.createRefreshToken(user.getId());

        // Bearer 제거
        String refreshToken = jwtUtil.substringToken(refreshTokenWithBearer);

        // Redis 저장 (Bearer 없는 토큰만 저장)
        tokenService.storeRefreshToken(user.getId(), refreshToken);

        return new LoginResponseDto(user.getId(), accessToken, refreshToken);
    }

    public void logout(String accessToken,Long userId) {

        // 1. AccessToken의 남은 유효시간 계산
        // 이시간 동안 블랙리스트 유지
        long expiration = jwtUtil.getRemainTime(accessToken);
        // 2. AccessToken 븡랙리스트 등록
        tokenService.blacklistAccessToken(accessToken,expiration);
        // 3. RefreshToken 명시적 삭제
        tokenService.deleteRefreshToken(userId);
    }

    public String reissueAccessToken(String refreshToken, String oldAccessToken) {

        // 1. 토큰 유효성 검사
        boolean isValid = jwtUtil.validateToken(refreshToken);
        if (!isValid) {
            throw new CustomRuntimeException(ExceptionCode.INVALID_REFRESH_TOKEN);
        }
        // 2. 토큰에서 userId 추출
        Long userId = jwtUtil.extractUserId(refreshToken);

        // 3. Redis에 저장된 리프레시 토큰 조회
        String storedToken = tokenService.getStoredRefreshToken(userId);

        // 4. 저장된 토큰과 요청 토큰이 일치하지 않으면 예외
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new CustomRuntimeException(ExceptionCode.INVALID_REFRESH_TOKEN);
        }
        // 5. 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomRuntimeException(ExceptionCode.NOT_FOUND_USER));
        // 6. 기존 Access Token 블랙리스트 등록
        long expiration = jwtUtil.getRemainTime(oldAccessToken);
        tokenService.blacklistAccessToken(oldAccessToken, expiration);
        // 7. 새로운 Access Token 생성
        String newAccessToken = jwtUtil.createToken(user.getId());
        return newAccessToken;
    }

}

