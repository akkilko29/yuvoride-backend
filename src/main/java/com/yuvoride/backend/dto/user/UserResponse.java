package com.yuvoride.backend.dto.user;

import com.yuvoride.backend.enums.UserRole;
import com.yuvoride.backend.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class UserResponse {
    private final Long id;
    private final String name;
    private final String phone;
    private final String email;
    private final String profilePhoto;
    private final UserRole role;
    private final UserStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;
}
