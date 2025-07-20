package com.example.mockstalk.domain.auth.controller;

import com.example.mockstalk.domain.auth.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/secure")
    public ResponseEntity<String> secureEndpoint(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok("Hello, " + userDetails.getUser().getEmail());
    }
}
