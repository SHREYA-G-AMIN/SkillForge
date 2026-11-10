package com.skillforge.opportunity.integration;

import com.skillforge.entity.User;
import com.skillforge.exception.ResourceNotFoundException;
import com.skillforge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Primary
@RequiredArgsConstructor
public class SecurityCurrentUserProvider implements CurrentUserProvider {

    private final UserRepository userRepository;

    @Override
    public UserContext getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AuthenticationCredentialsNotFoundException("User is not authenticated");
        }

        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        String role = auth.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse(user.getRole().name());

        return new UserContext(user.getId(), user.getEmail(), role);
    }
}
