package com.autotrack.dto;

import com.autotrack.entity.Role;

public record UserSummary(Long id, String name, String email, Role role) {}
