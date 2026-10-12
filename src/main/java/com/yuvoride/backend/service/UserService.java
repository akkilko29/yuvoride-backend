package com.yuvoride.backend.service;

import com.yuvoride.backend.dto.user.UpdateUserRequest;
import com.yuvoride.backend.dto.user.UserResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserResponse getCurrentUser(Long userId);
    UserResponse updateCurrentUser(Long userId, UpdateUserRequest request);
    UserResponse updateProfilePhoto(Long userId, MultipartFile file);
}
