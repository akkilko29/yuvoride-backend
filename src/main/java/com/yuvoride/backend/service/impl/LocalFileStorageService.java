package com.yuvoride.backend.service.impl;

import com.yuvoride.backend.config.AppProperties;
import com.yuvoride.backend.exception.BadRequestException;
import com.yuvoride.backend.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    private static final Set<String> ALLOWED = Set.of("image/jpeg", "image/png", "image/webp");

    private final AppProperties properties;

    public LocalFileStorageService(AppProperties properties) {
        this.properties = properties;
    }

    @Override
    public String store(MultipartFile file, String directory) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Photo file is required");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED.contains(contentType)) {
            throw new BadRequestException("Only JPEG, PNG, and WebP images are allowed");
        }
        String extension = switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
        try {
            Path root = Path.of(properties.getStorage().getLocalDir()).toAbsolutePath().normalize();
            Path targetDir = root.resolve(directory).normalize();
            if (!targetDir.startsWith(root)) {
                throw new BadRequestException("Invalid storage path");
            }
            Files.createDirectories(targetDir);
            String filename = UUID.randomUUID() + extension;
            Path target = targetDir.resolve(filename);
            file.transferTo(target);
            return directory + "/" + filename;
        } catch (IOException ex) {
            throw new BadRequestException("Unable to store file");
        }
    }

    @Override
    public String storeReference(String clientUri) {
        if (!StringUtils.hasText(clientUri)) {
            return null;
        }
        String value = clientUri.trim();
        if (value.startsWith("file:") || value.startsWith("content:") || value.startsWith("ph://")) {
            return "unverified:" + value;
        }
        return value;
    }

    @Override
    public void delete(String relativePath) {
        if (!StringUtils.hasText(relativePath) || relativePath.startsWith("unverified:") || relativePath.startsWith("http")) {
            return;
        }
        try {
            Path root = Path.of(properties.getStorage().getLocalDir()).toAbsolutePath().normalize();
            Path target = root.resolve(relativePath).normalize();
            if (target.startsWith(root)) {
                Files.deleteIfExists(target);
            }
        } catch (IOException ignored) {
            // best-effort cleanup
        }
    }

    @Override
    public String getUrl(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            return null;
        }
        if (relativePath.startsWith("http") || relativePath.startsWith("unverified:")) {
            return null;
        }
        String base = properties.getStorage().getPublicBaseUrl();
        return base.endsWith("/") ? base + relativePath : base + "/" + relativePath;
    }
}
