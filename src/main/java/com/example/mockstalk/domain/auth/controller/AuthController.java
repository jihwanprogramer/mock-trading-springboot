package com.example.mockstalk.domain.auth.controller;

import com.example.mockstalk.domain.auth.jwt.JwtUtil;
import com.example.mockstalk.common.response.ResponseMessage;
import com.example.mockstalk.domain.auth.service.AuthService;
import com.example.mockstalk.domain.auth.dto.request.LoginRequestDto;
import com.example.mockstalk.domain.auth.dto.response.LoginResponseDto;
import com.example.mockstalk.domain.auth.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ResponseMessage<?>> login(@RequestBody LoginRequestDto dto) {

        // 로그인 처리 (Access + Refresh 토큰 생성)
        LoginResponseDto responseDto = authService.login(dto);  // 여기서 두 토큰을 만들어서 리턴

        // Bearer 제거
        String refreshToken = responseDto.getRefreshToken();

        // Refresh Token을 HttpOnly 쿠키로 설정
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) // HTTPS 환경일 경우 true
                .path("/")
                .maxAge(Duration.ofDays(14))
                .build();

        // Access Token은 body로 응답
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ResponseMessage.success("로그인 완료", responseDto.getAccessToken()));

    }
    //로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ResponseMessage<?>> logout(@AuthenticationPrincipal CustomUserDetails userDetails, HttpServletRequest request){

        // 1. 요청 헤더에서 AccessToken 추출 및 Bearer 제거
        String rawToken = request.getHeader("Authorization");
        String accessToken = jwtUtil.substringToken(rawToken);

        // 2. 로그인된 사용자 ID
        Long userId = userDetails.getUser().getId();

        // 3. 로그아웃 처리
        authService.logout(accessToken, userId);
        return ResponseEntity.ok(ResponseMessage.success("로그아웃 완료"));
    }
    // AccessToken 재발급
    @PostMapping("/reissue")
    public ResponseEntity<ResponseMessage<?>> reissue(@CookieValue("refreshToken") String refreshToken, HttpServletRequest request) {
        // 1. 요청 헤더에서 AccessToken 추출 및 Bearer 제거
        String rawToken = request.getHeader("Authorization");
        String accessToken = jwtUtil.substringToken(rawToken);

        String newAccessToken = authService.reissueAccessToken(refreshToken,accessToken);
        return ResponseEntity.ok(ResponseMessage.success("토큰 재발급 성공", newAccessToken));
    }

}
