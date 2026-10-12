package com.yuvoride.backend.service.impl;

import com.yuvoride.backend.config.AppProperties;
import com.yuvoride.backend.exception.BadRequestException;
import com.yuvoride.backend.service.FileStorageService;
import org.springframework.stereotype.Service;
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
}
