package com.example.mockstalk.common.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.mockstalk.domain.user.entity.UserRole;

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
	private final UserDetailsService userDetailsService;

	public JwtFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;

	}

	@Override
	protected void doFilterInternal(HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		String url = request.getRequestURI();

		// 로그인/회원가입 필터 통과
		if (url.equals("/users/login") || url.equals("/users/signup")) {
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

		// "Brarer" 제거
		String jwt = jwtUtil.substringToken(bearerJwt);

		try {
			// Claims = JWT 안에 담긴 사용자 정보
			Claims claims = jwtUtil.extractClaims(jwt);

			//Access 토큰 타입 검사
			String tokenType = claims.get("tokenType", String.class);
			if (!"access".equals(tokenType)) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access Token이 필요합니다.");
				return;
			}

			// email로 유저 조회
			String email = claims.get("email", String.class);
			UserDetails userDetails = userDetailsService.loadUserByUsername(email);

			// SecurityContext 인증 정보 설정 -> 인증 통과됨
			UsernamePasswordAuthenticationToken authentication =
				new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
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
			throw e;
		}

	}
}
