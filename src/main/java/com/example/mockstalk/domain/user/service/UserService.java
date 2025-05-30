package com.example.mockstalk.domain.user.service;

import com.example.mockstalk.common.config.JwtUtil;
import com.example.mockstalk.domain.user.dto.request.LoginRequestDto;
import com.example.mockstalk.domain.user.dto.request.SignupRequestDto;
import com.example.mockstalk.domain.user.dto.response.FindResponseDto;
import com.example.mockstalk.domain.user.dto.response.LoginResponseDto;
import com.example.mockstalk.domain.user.entity.User;
import com.example.mockstalk.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public void signup(SignupRequestDto dto) {
        if(userRepository.existsByEmail(dto.getEmail())){
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        String encode = passwordEncoder.encode(dto.getPassword());
        User user = new User(dto.getEmail(), encode, dto.getNickname(), dto.getWalletAddress(), dto.getUserRole());
        userRepository.save(user);

    }

    public LoginResponseDto login(LoginRequestDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일 입니다."));
        String token = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole());

        return new LoginResponseDto(user.getId(),token);

    }


    public FindResponseDto findMe(HttpServletRequest request) {
        String email = (String) request.getAttribute("email");

        if(email == null){
            throw new IllegalArgumentException("인증된 사용자가 없습니다");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return new FindResponseDto(user);
    }
}
