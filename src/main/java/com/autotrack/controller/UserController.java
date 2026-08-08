package com.autotrack.controller;

import com.autotrack.dto.UserSummary;
import com.autotrack.service.AuthService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    public UserSummary me(@AuthenticationPrincipal Jwt jwt) {
        return authService.getByEmail(jwt.getSubject());
    }
}
