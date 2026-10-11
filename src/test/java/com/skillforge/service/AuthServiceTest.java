package com.skillforge.service;

import com.skillforge.dto.AuthDtos.AuthResponse;
import com.skillforge.dto.AuthDtos.LoginRequest;
import com.skillforge.dto.AuthDtos.RegisterRequest;
import com.skillforge.entity.RevokedToken;
import com.skillforge.entity.Role;
import com.skillforge.entity.StudentProfile;
import com.skillforge.entity.User;
import com.skillforge.exception.DuplicateResourceException;
import com.skillforge.repository.RevokedTokenRepository;
import com.skillforge.repository.StudentProfileRepository;
import com.skillforge.repository.UserRepository;
import com.skillforge.security.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock StudentProfileRepository profileRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authenticationManager;
    @Mock JwtService jwtService;
    @Mock RevokedTokenRepository revokedTokenRepository;
    @InjectMocks AuthService authService;

    private final RegisterRequest registerReq =
            new RegisterRequest("Asha", "nnm24cs001", "a@b.com", "Passw0rd!");

    @Test
    void register_success_hashesPasswordAndCreatesStudentWithProfile() {
        when(passwordEncoder.encode("Passw0rd!")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken("a@b.com", Role.STUDENT)).thenReturn("token");

        AuthResponse res = authService.register(registerReq);

        assertEquals("token", res.token());
        assertEquals("STUDENT", res.role());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("hashed", userCaptor.getValue().getPasswordHash());
        assertEquals(Role.STUDENT, userCaptor.getValue().getRole());

        ArgumentCaptor<StudentProfile> profileCaptor = ArgumentCaptor.forClass(StudentProfile.class);
        verify(profileRepository).save(profileCaptor.capture());
        assertEquals("NNM24CS001", profileCaptor.getValue().getUsn());   // normalised to upper-case
    }

    @Test
    void register_duplicateEmail_throwsAndSavesNothing() {
        when(userRepository.existsByEmail("a@b.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(registerReq));

        verify(userRepository, never()).save(any());
        verify(profileRepository, never()).save(any());
    }

    @Test
    void register_duplicateUsn_throws() {
        when(profileRepository.existsByUsn("NNM24CS001")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(registerReq));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_success_returnsToken() {
        User user = new User();
        user.setEmail("a@b.com");
        user.setRole(Role.STUDENT);
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("a@b.com", Role.STUDENT)).thenReturn("token");

        AuthResponse res = authService.login(new LoginRequest("a@b.com", "Passw0rd!"));

        assertEquals("token", res.token());
        assertEquals("a@b.com", res.email());
    }

    @Test
    void login_wrongPassword_throwsBadCredentials() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginRequest("a@b.com", "wrong")));

        verifyNoInteractions(jwtService);
    }

    @Test
    void logout_revokesToken() {
        Claims claims = mock(Claims.class);
        when(claims.getId()).thenReturn("jti-1");
        when(claims.getExpiration()).thenReturn(new Date(System.currentTimeMillis() + 60_000));
        when(jwtService.parseClaims("tok")).thenReturn(claims);

        authService.logout("tok");

        ArgumentCaptor<RevokedToken> captor = ArgumentCaptor.forClass(RevokedToken.class);
        verify(revokedTokenRepository).save(captor.capture());
        assertEquals("jti-1", captor.getValue().getJti());
    }

    @Test
    void logout_alreadyRevoked_doesNotSaveTwice() {
        Claims claims = mock(Claims.class);
        when(claims.getId()).thenReturn("jti-1");
        when(jwtService.parseClaims("tok")).thenReturn(claims);
        when(revokedTokenRepository.existsByJti("jti-1")).thenReturn(true);

        authService.logout("tok");

        verify(revokedTokenRepository, never()).save(any());
    }
}
