package com.autotrack.service;

import com.autotrack.dto.AuthRequest;
import com.autotrack.dto.AuthResponse;
import com.autotrack.dto.UserSummary;
import com.autotrack.entity.AppUser;
import com.autotrack.exception.NotFoundException;
import com.autotrack.repository.AppUserRepository;
import com.autotrack.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final AppUserRepository userRepository;
    private final JwtService jwtService;

    public AuthService(
            AuthenticationManager authenticationManager,
            AppUserRepository userRepository,
            JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public AuthResponse login(AuthRequest request) {
        String email = request.email().trim().toLowerCase();
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password()));

        AppUser user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("User not found."));
        String token = jwtService.issueToken(user);
        UserSummary summary = new UserSummary(user.getId(), user.getName(), user.getEmail(), user.getRole());
        return new AuthResponse(token, "Bearer", jwtService.expiresInSeconds(), summary);
    }

    public UserSummary getByEmail(String email) {
        AppUser user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("User not found."));
        return new UserSummary(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
