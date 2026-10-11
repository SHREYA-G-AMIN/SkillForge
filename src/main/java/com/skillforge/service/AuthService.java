package com.skillforge.service;

import com.skillforge.dto.AuthDtos.AuthResponse;
import com.skillforge.dto.AuthDtos.LoginRequest;
import com.skillforge.dto.AuthDtos.RegisterRequest;
import com.skillforge.entity.RevokedToken;
import com.skillforge.entity.Role;
import com.skillforge.entity.StudentProfile;
import com.skillforge.entity.User;
import com.skillforge.exception.DuplicateResourceException;
import com.skillforge.exception.ResourceNotFoundException;
import com.skillforge.repository.RevokedTokenRepository;
import com.skillforge.repository.StudentProfileRepository;
import com.skillforge.repository.UserRepository;
import com.skillforge.security.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final StudentProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RevokedTokenRepository revokedTokenRepository;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        String usn = req.usn().trim().toUpperCase();

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email is already registered");
        }
        if (profileRepository.existsByUsn(usn)) {
            throw new DuplicateResourceException("USN is already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setRole(Role.STUDENT);
        User saved = userRepository.save(user);

        StudentProfile profile = new StudentProfile();
        profile.setUser(saved);
        profile.setName(req.name().trim());
        profile.setUsn(usn);
        profileRepository.save(profile);

        return new AuthResponse(jwtService.generateToken(email, Role.STUDENT), email, Role.STUDENT.name());
    }

    public AuthResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();

        // Throws BadCredentialsException on a wrong email/password -> handled as 401
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, req.password()));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return new AuthResponse(jwtService.generateToken(user.getEmail(), user.getRole()),
                user.getEmail(), user.getRole().name());
    }

    /** Revokes the given token so it cannot be used again, even before it expires. */
    @Transactional
    public void logout(String token) {
        Claims claims = jwtService.parseClaims(token);
        revokedTokenRepository.deleteByExpiresAtBefore(Instant.now());   // housekeeping: expired tokens are invalid anyway
        if (!revokedTokenRepository.existsByJti(claims.getId())) {
            revokedTokenRepository.save(new RevokedToken(claims.getId(), claims.getExpiration().toInstant()));
        }
    }
}
