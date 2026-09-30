package com.connectly.service.impl;

import com.connectly.exception.BadRequestException;
import com.connectly.exception.ResourceNotFoundException;
import com.connectly.service.StorageService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class LocalStorageServiceImpl implements StorageService {

    private static final Logger logger = LoggerFactory.getLogger(LocalStorageServiceImpl.class);

    private final Path rootLocation;

    public LocalStorageServiceImpl(@Value("${connectly.media.upload-dir:./uploads}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
            Files.createDirectories(rootLocation.resolve("images"));
            Files.createDirectories(rootLocation.resolve("videos"));
            Files.createDirectories(rootLocation.resolve("stickers"));
            Files.createDirectories(rootLocation.resolve("documents"));
            Files.createDirectories(rootLocation.resolve("avatars"));
            logger.info("Media storage directory initialized at: {}", rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directories: " + e.getMessage(), e);
        }
    }

    @Override
    public String store(MultipartFile file, String subDirectory) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Failed to store empty file");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "media");
        
        // Prevent path traversal attack
        if (originalFilename.contains("..")) {
            throw new BadRequestException("Cannot store file with relative path outside current directory: " + originalFilename);
        }

        String extension = "";
        int extIndex = originalFilename.lastIndexOf('.');
        if (extIndex > 0) {
            extension = originalFilename.substring(extIndex).toLowerCase();
        }

        String storedFilename = UUID.randomUUID() + extension;
        String sanitizedSubDir = (subDirectory != null && !subDirectory.trim().isEmpty()) ? subDirectory.trim().toLowerCase() : "general";

        try {
            Path targetDir = this.rootLocation.resolve(sanitizedSubDir).normalize();
            if (!targetDir.startsWith(this.rootLocation)) {
                throw new BadRequestException("Cannot store file outside current storage root");
            }

            Files.createDirectories(targetDir);

            Path destinationFile = targetDir.resolve(storedFilename).normalize();
            if (!destinationFile.startsWith(targetDir)) {
                throw new BadRequestException("Cannot store file outside targeted subdirectory");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            logger.info("Stored file {} successfully at {}", originalFilename, destinationFile);
            return storedFilename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + originalFilename + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Resource loadAsResource(String filename, String subDirectory) {
        try {
            String sanitizedSubDir = (subDirectory != null && !subDirectory.trim().isEmpty()) ? subDirectory.trim().toLowerCase() : "general";
            Path targetDir = this.rootLocation.resolve(sanitizedSubDir).normalize();
            Path file = targetDir.resolve(filename).normalize();

            if (!file.startsWith(this.rootLocation)) {
                throw new BadRequestException("Access denied: Invalid file path traversal");
            }

            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Could not find file: " + filename);
        }
    }

    @Override
    public void delete(String filename, String subDirectory) {
        try {
            String sanitizedSubDir = (subDirectory != null && !subDirectory.trim().isEmpty()) ? subDirectory.trim().toLowerCase() : "general";
            Path file = this.rootLocation.resolve(sanitizedSubDir).resolve(filename).normalize();
            if (file.startsWith(this.rootLocation)) {
                Files.deleteIfExists(file);
            }
        } catch (IOException e) {
            logger.warn("Could not delete file {}: {}", filename, e.getMessage());
        }
    }

    @Override
    public String getMimeType(String filename) {
        if (filename == null) return "application/octet-stream";
        String lower = filename.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".mp4")) return "video/mp4";
        if (lower.endsWith(".webm")) return "video/webm";
        if (lower.endsWith(".mov")) return "video/quicktime";
        if (lower.endsWith(".mkv")) return "video/x-matroska";
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".mp3")) return "audio/mpeg";
        if (lower.endsWith(".ogg")) return "audio/ogg";
        if (lower.endsWith(".wav")) return "audio/wav";
        return "application/octet-stream";
    }
}
