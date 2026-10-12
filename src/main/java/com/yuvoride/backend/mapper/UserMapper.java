package com.yuvoride.backend.mapper;

import com.yuvoride.backend.config.AppProperties;
import com.yuvoride.backend.dto.user.UserResponse;
import com.yuvoride.backend.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class UserMapper {

    private final AppProperties properties;

    public UserMapper(AppProperties properties) {
        this.properties = properties;
    }

    public UserResponse toResponse(User user) {
        String photo = user.getProfilePhoto();
        if (StringUtils.hasText(photo) && !photo.startsWith("http")) {
            String base = properties.getStorage().getPublicBaseUrl();
            photo = base.endsWith("/") ? base + photo : base + "/" + photo;
        }
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .profilePhoto(photo)
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
