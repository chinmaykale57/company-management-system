package com.example.sellerhelp.security;

import com.example.sellerhelp.appuser.entity.Role;
import com.example.sellerhelp.appuser.entity.User;
import com.example.sellerhelp.appuser.repository.UserRepository;
import com.example.sellerhelp.appuser.repository.RoleRepository;
import com.example.sellerhelp.constant.UserRole;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.PrintWriter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Permit login, signup, and OAuth2 endpoints
                        .requestMatchers("/api/auth/**", "/oauth2/**").permitAll()
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler((request, response, authentication) -> {
                            OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
                            OAuth2User oAuth2User = token.getPrincipal();

                            String email = (String) oAuth2User.getAttributes().get("email");

                            // Find or create a user in your database
                            User user = userRepository.findByEmail(email)
                                    .orElseGet(() -> {
                                        User newUser = new User();
                                        newUser.setEmail(email);
                                        newUser.setName((String) oAuth2User.getAttributes().get("name"));
                                         Role customerRole = roleRepository.findByName(UserRole.valueOf("DEALER")).orElseThrow();
                                         newUser.setRole(customerRole);
                                        newUser.setPassword(passwordEncoder().encode("OAUTH2_DUMMY_PASSWORD")); // Dummy password
                                        return userRepository.save(newUser);
                                    });

                            String jwtToken = jwtUtil.generateToken(user.getEmail());

                            // Return the JWT token in the response
                            response.setContentType("application/json");
                            PrintWriter out = response.getWriter();
                            out.print("{\"token\":\"" + jwtToken + "\"}");
                            out.flush();
                        })
                );

        return http.build();
    }
}