package com.example.sellerhelp.config;

import com.example.sellerhelp.appuser.entity.User;
import com.example.sellerhelp.appuser.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class AuditConfig {

    private final UserRepository userRepository;

    @Bean
    public AuditorAware<User> auditorProvider() {
        return new AuditorAwareImpl();
    }

    // You can define this as an inner class for encapsulation
    private class AuditorAwareImpl implements AuditorAware<User> {

        @Override
        public Optional<User> getCurrentAuditor() {
            // 1. Get the current Authentication object from the SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // 2. Check if there is an authenticated user
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.empty(); // No user logged in
            }

            // 3. Get the principal (the user's identity)
            Object principal = authentication.getPrincipal();

            // 4. Extract the email (username) from the principal
            String email;
            if (principal instanceof UserDetails) {
                email = ((UserDetails) principal).getUsername();
            } else {
                email = principal.toString();
            }

            // 5. Use the email to fetch the full User entity from the database
            return userRepository.findByEmail(email);
        }
    }
}