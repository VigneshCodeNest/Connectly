package com.connectly.controller;

import com.connectly.constant.MessageType;
import com.connectly.dto.response.ApiResponse;
import com.connectly.dto.response.MediaUploadResponse;
import com.connectly.entity.User;
import com.connectly.service.AuthService;
import com.connectly.service.MediaValidationService;
import com.connectly.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/media")
public class MediaController {

    private static final Logger logger = LoggerFactory.getLogger(MediaController.class);

    private final StorageService storageService;
    private final MediaValidationService validationService;
    private final AuthService authService;

    public MediaController(
            StorageService storageService,
            MediaValidationService validationService,
            AuthService authService
    ) {
        this.storageService = storageService;
        this.validationService = validationService;
        this.authService = authService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MediaUploadResponse>> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", required = false) String requestedType
    ) {
        User currentUser = authService.getAuthenticatedUser();
        logger.info("User {} is uploading file: {}, type hint: {}", currentUser.getUsername(), file.getOriginalFilename(), requestedType);

        String category = validationService.validateAndResolveCategory(file, requestedType);
        MessageType mediaType = validationService.resolveMessageType(category);

        String storedFileName = storageService.store(file, category);
        String fileUrl = "/api/v1/media/" + category + "/" + storedFileName;

        MediaUploadResponse response = new MediaUploadResponse(
                fileUrl,
                file.getOriginalFilename(),
                storedFileName,
                file.getContentType(),
                file.getSize(),
                mediaType,
                category
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Media uploaded successfully", response));
    }

    @GetMapping("/{category}/{filename:.+}")
    public ResponseEntity<Resource> getMedia(
            @PathVariable("category") String category,
            @PathVariable("filename") String filename
    ) {
        Resource file = storageService.loadAsResource(filename, category);
        String mimeType = storageService.getMimeType(filename);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .body(file);
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getMediaFlat(@PathVariable("filename") String filename) {
        Resource file = storageService.loadAsResource(filename, "general");
        String mimeType = storageService.getMimeType(filename);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .body(file);
    }
}
