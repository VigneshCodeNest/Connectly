package com.connectly.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    void init();
    String store(MultipartFile file, String subDirectory);
    Resource loadAsResource(String filename, String subDirectory);
    void delete(String filename, String subDirectory);
    String getMimeType(String filename);
}
