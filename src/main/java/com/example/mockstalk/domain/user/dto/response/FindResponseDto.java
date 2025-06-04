package com.example.mockstalk.domain.user.dto.response;

import com.example.mockstalk.domain.user.entity.User;
import lombok.Getter;

@Getter
public class FindResponseDto {


    private Long id;

    private String email;

    private String nickname;

    private String walletAddress;


    public FindResponseDto(User user){
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.walletAddress = user.getWalletAddress();
    }
}
