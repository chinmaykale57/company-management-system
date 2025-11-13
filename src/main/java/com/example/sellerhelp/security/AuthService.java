package com.example.sellerhelp.security;

import com.example.sellerhelp.appuser.dto.CreateDealerDto;
import com.example.sellerhelp.appuser.dto.DealerDto;
import com.example.sellerhelp.appuser.dto.UserLoginResponseDto;
import com.example.sellerhelp.appuser.entity.User;
import com.example.sellerhelp.appuser.repository.UserRepository;
import com.example.sellerhelp.appuser.service.DealerService;
import com.example.sellerhelp.common.ApiResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final DealerService dealerService;

    public UserLoginResponseDto login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return new UserLoginResponseDto( user.getId(), user.getName(), user.getEmail(), user.getRole().getName().name(), jwtUtil.generateToken(email)) ;
    }

    /**
     * Public endpoint for a new Dealer to sign up.
     */
    @PostMapping("/signup/dealer")
    public ResponseEntity<ApiResponseDto<DealerDto>> dealerSignup(@Valid @RequestBody CreateDealerDto dto) {
        DealerDto newDealer = dealerService.createDealer(dto);
        return new ResponseEntity<>(ApiResponseDto.ok(newDealer, "Dealer registration successful."), HttpStatus.CREATED);
    }
}