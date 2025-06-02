package com.example.mockstalk.domain.user.controller;

import com.example.mockstalk.domain.user.dto.request.DeleteRequestDto;
import com.example.mockstalk.domain.user.dto.request.LoginRequestDto;
import com.example.mockstalk.domain.user.dto.request.SignupRequestDto;
import com.example.mockstalk.domain.user.dto.request.UpdateRequestDto;
import com.example.mockstalk.domain.user.dto.response.FindResponseDto;
import com.example.mockstalk.domain.user.dto.response.LoginResponseDto;
import com.example.mockstalk.domain.user.entity.User;
import com.example.mockstalk.domain.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;


    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequestDto dto){

        userService.signup(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 완료되었습니다.");
    }
    // 로그인
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto dto){

        return ResponseEntity.ok(userService.login(dto));
    }
    // 프로필 조회
    @GetMapping("/me")
    public ResponseEntity<FindResponseDto> findMe(HttpServletRequest request){
        return ResponseEntity.ok(userService.findMe(request));
    }

    // 회원탈퇴
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(HttpServletRequest request, @RequestBody DeleteRequestDto dto) {
        userService.deleteMe(request,dto);
        return ResponseEntity.noContent().build();
    }
    //타 유저조회 지갑주소로
    @GetMapping("/wallet/{walletAddress}")
    public ResponseEntity<FindResponseDto> findByWallet(@PathVariable String walletAddress){

        return ResponseEntity.ok(userService.findByWallet(walletAddress));

    }
    // 유저 정보 수정
    @PatchMapping("/me")
    public ResponseEntity<String> updateMe(HttpServletRequest request,@RequestBody UpdateRequestDto dto){
        userService.updateMe(request, dto);
        return ResponseEntity.ok("정보 수정 완료");
    }
}
