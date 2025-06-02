package com.example.mockstalk.domain.user.service;

import com.example.mockstalk.domain.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Stream;

@Getter
public class CustomUserDetails implements UserDetails {

    private User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // USER, ADMIN 역할을 "ROLE_USER", "ROLE_ADMIN" 형태로 변환
        return Stream.of(user.getUserRole())// UserRole enum 하나를 스트림으로 만듦
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name())) // ROLE_ 붙이고 권한 객체로 변환
                .toList();

    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    public Long getId(){
        return user.getId();
    }
    public String getNickname(){
        return user.getNickname();
    }
}
