package com.yuvoride.backend.controller;

import com.yuvoride.backend.dto.common.ApiResponse;
import com.yuvoride.backend.dto.user.UpdateUserRequest;
import com.yuvoride.backend.dto.user.UserResponse;
import com.yuvoride.backend.security.CurrentUser;
import com.yuvoride.backend.security.UserPrincipal;
import com.yuvoride.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/users")
@Tag(name = "Users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> me(@CurrentUser UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Profile loaded", userService.getCurrentUser(principal.getId())));
    }

    @PatchMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> update(@CurrentUser UserPrincipal principal,
                                                            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Profile updated", userService.updateCurrentUser(principal.getId(), request)));
    }

    @PostMapping(value = "/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload profile photo")
    public ResponseEntity<ApiResponse<UserResponse>> photo(@CurrentUser UserPrincipal principal,
                                                           @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.ok("Photo updated", userService.updateProfilePhoto(principal.getId(), file)));
    }
}
