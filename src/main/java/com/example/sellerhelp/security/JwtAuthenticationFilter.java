package com.example.sellerhelp.security;

import com.example.sellerhelp.appuser.repository.UserRepository;
import com.example.sellerhelp.common.ApiResponseDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            if (!request.getRequestURI().startsWith("/api/auth/")) {
                response.setStatus(401);
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Missing or invalid token\",\"status\":\"UNAUTHORIZED\",\"timestamp\":\"" +
                                java.time.ZonedDateTime.now() + "\"}"
                );
            }
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String email;



        try {
            email = jwtUtil.extractEmail(token);
        } catch (Exception e) {
            ApiResponseDto<?> error = ApiResponseDto.error("Invalid or malformed token", HttpStatus.UNAUTHORIZED);

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(String.format(
                    "{\"success\":%s,\"message\":\"%s\",\"status\":\"%s\",\"timestamp\":\"%s\"}",
                    error.isSuccess(),
                    error.getMessage(),
                    error.getStatus(),
                    error.getTimestamp()
            ));
            return;
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            var userOptional = userRepository.findByEmail(email);

            if (userOptional.isPresent() && jwtUtil.isTokenValid(token, email)) {
                var user = userOptional.get();
                var authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().getName().name());
                var authToken = new UsernamePasswordAuthenticationToken(user.getEmail(), null, List.of(authority));

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}