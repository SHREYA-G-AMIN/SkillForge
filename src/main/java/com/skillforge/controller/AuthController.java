package com.skillforge.controller;

import com.skillforge.dto.AuthDtos.AuthResponse;
import com.skillforge.dto.AuthDtos.LoginRequest;
import com.skillforge.dto.AuthDtos.RegisterRequest;
import com.skillforge.exception.BadRequestException;
import com.skillforge.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader("Authorization") String authorization) {
        if (!authorization.startsWith("Bearer ")) {
            throw new BadRequestException("Invalid Authorization header");
        }
        authService.logout(authorization.substring(7));
    }
}
