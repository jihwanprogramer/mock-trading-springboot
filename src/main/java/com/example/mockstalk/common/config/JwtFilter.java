package com.example.mockstalk.common.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.mockstalk.common.jwttoken.JwtTokenService;
import com.example.mockstalk.domain.user.entity.User;
import com.example.mockstalk.domain.user.entity.UserRole;
import com.example.mockstalk.domain.user.service.CustomUserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j

public class JwtFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final JwtTokenService tokenService;

	public JwtFilter(JwtUtil jwtUtil, JwtTokenService tokenService) {
		this.jwtUtil = jwtUtil;
		this.tokenService = tokenService;

	}

	@Override
	protected void doFilterInternal(HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		String url = request.getRequestURI();

		// 로그인/회원가입/토큰 재발급 필터 통과
		if (url.equals("/api/auth/login")
			|| url.equals("/api/users/signup")
			|| url.equals("/api/auth/reissue")
			|| url.equals("/health")
		) {
			filterChain.doFilter(request, response);
			return;
		}

		// Authorization 헤더에서 JWT 추출
		String bearerJwt = request.getHeader("Authorization");
		if (bearerJwt == null) {
			// 토큰이 없는 경우 400을 반환합니다.
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "JWT 토큰이 필요합니다.");
			return;
		}

		// "Bearer" 제거
		String jwt = jwtUtil.substringToken(bearerJwt);

		try {
			// 로그아웃된(블랙리스트) 토큰인지 확인
			if (tokenService.isBlacklisted(jwt)) {
				log.warn("블랙리스트 등록된 토큰");
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그아웃된 토큰입니다.");
				return;
			}
			// JWT 토큰에서 클레임(파싱) 추출
			Claims claims = jwtUtil.extractClaims(jwt);

			//Access 토큰 타입 검사
			String tokenType = claims.get("tokenType", String.class);
			if (!"access".equals(tokenType)) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access Token이 필요합니다.");
				return;
			}

			// 1. JWT Claims에서 사용자 정보 꺼냄
			String email = claims.get("email", String.class);
			String nickname = claims.get("nickname", String.class);
			Long userId = Long.parseLong(claims.getSubject());
			String role = claims.get("user", String.class);

			// User 엔티티 객체를 직접 생성 (DB 조회 없이)
			User user = User.builder()
				.id(userId)
				.email(email)
				.password("")
				.nickname(nickname)
				.userRole(UserRole.valueOf(role.replace("ROLE_", "")))
				.build();

			// CustomUserDetails 생성
			CustomUserDetails customUserDetails = new CustomUserDetails(user);

			// 인증 객체 생성 및 등록
			UsernamePasswordAuthenticationToken authentication =
				new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

			SecurityContextHolder.getContext().setAuthentication(authentication);

			//관리자 접근 체크
			if (url.startsWith("/admin")) {
				String userRole = claims.get("userRole", String.class);
				if (!UserRole.ADMIN.name().equals(userRole)) {
					response.sendError(HttpServletResponse.SC_FORBIDDEN, "관리자 권한이 없습니다.");
					return;
				}
				filterChain.doFilter(request, response);
				return;
			}

			filterChain.doFilter(request, response);
		} catch (SecurityException | MalformedJwtException e) {
			log.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.", e);
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않는 JWT 서명입니다.");
		} catch (ExpiredJwtException e) {
			log.error("Expired JWT token, 만료된 JWT token 입니다.", e);
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "만료된 JWT 토큰입니다.");
		} catch (UnsupportedJwtException e) {
			log.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.", e);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "지원되지 않는 JWT 토큰입니다.");
		} catch (Exception e) {
			log.error("Invalid JWT token, 유효하지 않는 JWT 토큰 입니다.", e);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "유효하지 않는 JWT 토큰입니다.");
		}

	}
}
