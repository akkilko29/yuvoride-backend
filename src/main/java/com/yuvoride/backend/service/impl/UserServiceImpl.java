package com.yuvoride.backend.service.impl;

import com.yuvoride.backend.dto.user.UpdateUserRequest;
import com.yuvoride.backend.dto.user.UserResponse;
import com.yuvoride.backend.entity.User;
import com.yuvoride.backend.exception.BadRequestException;
import com.yuvoride.backend.exception.ResourceNotFoundException;
import com.yuvoride.backend.mapper.UserMapper;
import com.yuvoride.backend.repository.UserRepository;
import com.yuvoride.backend.service.FileStorageService;
import com.yuvoride.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Long userId) {
        return userMapper.toResponse(getUser(userId));
    }

    @Override
    @Transactional
    public UserResponse updateCurrentUser(Long userId, UpdateUserRequest request) {
        User user = getUser(userId);
        if (StringUtils.hasText(request.getName())) {
            user.setName(request.getName().trim());
        }
        if (request.getEmail() != null) {
            String email = request.getEmail().trim();
            userRepository.findByEmail(email).ifPresent(existing -> {
                if (!existing.getId().equals(userId)) {
                    throw new BadRequestException("Email is already in use");
                }
            });
            user.setEmail(email);
        }
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateProfilePhoto(Long userId, MultipartFile file) {
        User user = getUser(userId);
        String path = fileStorageService.store(file, "public/users/" + userId);
        user.setProfilePhoto(path);
        return userMapper.toResponse(userRepository.save(user));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
