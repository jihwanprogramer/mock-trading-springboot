package com.example.mockstalk.domain.user.service;

import com.example.mockstalk.common.error.CustomRuntimeException;
import com.example.mockstalk.common.error.ExceptionCode;
import com.example.mockstalk.domain.auth.service.JwtTokenService;
import com.example.mockstalk.domain.user.dto.request.DeleteRequestDto;
import com.example.mockstalk.domain.user.dto.request.SignupRequestDto;
import com.example.mockstalk.domain.user.dto.request.UpdateRequestDto;
import com.example.mockstalk.domain.user.dto.response.FindResponseDto;
import com.example.mockstalk.domain.user.entity.User;
import com.example.mockstalk.domain.user.entity.UserRole;
import com.example.mockstalk.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.example.mockstalk.domain.user.entity.UserRole.USER;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    JwtTokenService tokenService;

    @Mock
    org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    User user;

    @BeforeEach
    void setUp() {
        user = new User("tester@example.com", "encodedPw", "닉네임", "0x123", USER);
    }

    @Test
    void signup_success() {
        SignupRequestDto dto = new SignupRequestDto("tester@example.com", "1234", "닉네임", "0x123", USER);

        given(userRepository.existsByEmail(dto.getEmail())).willReturn(false);
        given(passwordEncoder.encode(dto.getPassword())).willReturn("encodedPw");

        userService.signup(dto);

        then(userRepository).should().save(any(User.class));
    }

    @Test
    void signup_fail_duplicateEmail() {
        SignupRequestDto dto = new SignupRequestDto("tester@example.com", "1234", "닉네임", "0x123", USER);
        given(userRepository.existsByEmail(dto.getEmail())).willReturn(true);

        assertThatThrownBy(() -> userService.signup(dto))
                .isInstanceOf(CustomRuntimeException.class)
                .hasMessageContaining(ExceptionCode.EMAIL_ALREADY_EXISTS.getMessage());
    }

    @Test
    void findMe_success() {
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        FindResponseDto result = userService.findMe(1L);

        assertThat(result.getEmail()).isEqualTo("tester@example.com");
    }

    @Test
    void findMe_fail_notFound() {
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findMe(1L))
                .isInstanceOf(CustomRuntimeException.class)
                .hasMessageContaining(ExceptionCode.NOT_FOUND_USER.getMessage());
    }

    @Test
    void deleteMe_success() {
        DeleteRequestDto dto = new DeleteRequestDto("1234");
        ReflectionTestUtils.setField(user, "id", 1L);


        given(userRepository.findByEmail("tester@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches(dto.getPassword(), user.getPassword())).willReturn(true);

        userService.deleteMe("tester@example.com", dto);

        then(tokenService).should().deleteRefreshToken(anyLong());
        then(userRepository).should().delete(user);
    }

    @Test
    void deleteMe_fail_invalidPassword() {
        DeleteRequestDto dto = new DeleteRequestDto("wrong");

        given(userRepository.findByEmail("tester@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches(dto.getPassword(), user.getPassword())).willReturn(false);

        assertThatThrownBy(() -> userService.deleteMe("tester@example.com", dto))
                .isInstanceOf(CustomRuntimeException.class)
                .hasMessageContaining(ExceptionCode.INVALID_PASSWORD.getMessage());
    }

    @Test
    void findByWallet_success() {
        given(userRepository.findByWalletAddress("0x123")).willReturn(Optional.of(user));

        FindResponseDto result = userService.findByWallet("0x123");

        assertThat(result.getEmail()).isEqualTo("tester@example.com");
    }

    @Test
    void findByWallet_fail() {
        given(userRepository.findByWalletAddress("0x999")).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByWallet("0x999"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("지갑주소를 찾을 수 없습니다.");
    }

    @Test
    void updateMe_success() {
        UpdateRequestDto dto = new UpdateRequestDto("새닉네임", "1234", "5678");

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(dto.getOldPassword(), user.getPassword())).willReturn(true);
        given(passwordEncoder.encode(dto.getNewPassword())).willReturn("encodedNewPw");

        userService.updateMe(1L, dto);

        assertThat(user.getNickname()).isEqualTo("새닉네임");
    }

    @Test
    void updateMe_fail_invalidPassword() {
        UpdateRequestDto dto = new UpdateRequestDto("새닉네임", "wrongOld", "new");

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(dto.getOldPassword(), user.getPassword())).willReturn(false);

        assertThatThrownBy(() -> userService.updateMe(1L, dto))
                .isInstanceOf(CustomRuntimeException.class)
                .hasMessageContaining(ExceptionCode.INVALID_PASSWORD.getMessage());
    }
}
