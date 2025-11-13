package com.example.sellerhelp.controller;

import com.example.sellerhelp.appuser.dto.UserLoginRequestDto;
import com.example.sellerhelp.appuser.dto.UserLoginResponseDto;
import com.example.sellerhelp.appuser.entity.User;
import com.example.sellerhelp.common.ApiResponseDto;
import com.example.sellerhelp.security.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<UserLoginResponseDto>> login(
            @RequestBody @Valid UserLoginRequestDto loginRequest) {

        UserLoginResponseDto dto = authService.login(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        );

        return ResponseEntity.ok(ApiResponseDto.ok(dto));
    }

    //search sort and filter in one endpoint
    // You would also add a @PostMapping("/signup") here
    // that calls a method in UserService to create a new user.
}