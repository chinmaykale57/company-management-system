package com.example.sellerhelp.security;

import com.example.sellerhelp.appuser.entity.User;
import com.example.sellerhelp.appuser.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final UserRepository userRepository;

    /**
     * Retrieves the full User entity for the currently authenticated user.
     * This is the single source of truth for identifying the current user.
     * @return The authenticated User entity.
     * @throws NoSuchElementException if the authenticated user cannot be found in the database.
     */
    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;

        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else if (principal != null) {
            email = principal.toString();
        } else {
            // This case should ideally not be hit if endpoints are properly secured
            throw new IllegalStateException("Authentication principal is null, cannot identify current user.");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Authenticated user '" + email + "' not found in database."));
    }
}