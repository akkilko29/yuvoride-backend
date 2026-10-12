package com.yuvoride.backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String store(MultipartFile file, String directory);

    String storeReference(String clientUri);

    void delete(String relativePath);

    String getUrl(String relativePath);
}
