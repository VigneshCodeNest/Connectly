package com.connectly.service.impl;

import com.connectly.constant.MessageType;
import com.connectly.exception.BadRequestException;
import com.connectly.service.MediaValidationService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class MediaValidationServiceImpl implements MediaValidationService {

    // Blocked dangerous & executable extensions
    private static final Set<String> BLOCKED_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".exe", ".bat", ".cmd", ".sh", ".bin", ".msi", ".jar", ".js", ".jsp",
            ".php", ".py", ".vbs", ".scr", ".com", ".pif", ".dll", ".so", ".cgi",
            ".pl", ".asp", ".aspx", ".html", ".htm", ".xhtml", ".ps1", ".vbe", ".wsf"
    ));

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".webp", ".svg", ".bmp"
    ));

    private static final Set<String> ALLOWED_IMAGE_MIMES = new HashSet<>(Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml", "image/bmp"
    ));

    private static final Set<String> ALLOWED_VIDEO_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".mp4", ".webm", ".mov", ".mkv", ".3gp", ".avi"
    ));

    private static final Set<String> ALLOWED_VIDEO_MIMES = new HashSet<>(Arrays.asList(
            "video/mp4", "video/webm", "video/quicktime", "video/x-matroska", "video/3gpp", "video/x-msvideo"
    ));

    private static final Set<String> ALLOWED_STICKER_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".png", ".webp", ".gif", ".svg"
    ));

    private static final Set<String> ALLOWED_STICKER_MIMES = new HashSet<>(Arrays.asList(
            "image/png", "image/webp", "image/gif", "image/svg+xml"
    ));

    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final long MAX_STICKER_SIZE = 5 * 1024 * 1024; // 5MB
    private static final long MAX_VIDEO_SIZE = 50 * 1024 * 1024; // 50MB
    private static final long MAX_DOC_SIZE = 25 * 1024 * 1024; // 25MB

    @Override
    public String validateAndResolveCategory(MultipartFile file, String requestedType) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded media file cannot be empty");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "");
        String extension = "";
        int extIndex = originalFilename.lastIndexOf('.');
        if (extIndex > 0) {
            extension = originalFilename.substring(extIndex).toLowerCase();
        } else {
            throw new BadRequestException("Media file must have a valid file extension");
        }

        if (BLOCKED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Executable and script files (" + extension + ") are strictly forbidden for upload");
        }

        String contentType = file.getContentType() != null ? file.getContentType().toLowerCase().trim() : "application/octet-stream";
        long fileSize = file.getSize();

        String typeUpper = requestedType != null ? requestedType.trim().toUpperCase() : "";

        // Check if sticker
        if ("STICKER".equals(typeUpper)) {
            if (!ALLOWED_STICKER_EXTENSIONS.contains(extension) || !ALLOWED_STICKER_MIMES.contains(contentType)) {
                throw new BadRequestException("Invalid sticker format (" + extension + ", " + contentType + "). Allowed formats: PNG, WEBP, GIF, SVG");
            }
            if (fileSize > MAX_STICKER_SIZE) {
                throw new BadRequestException("Sticker file size exceeds maximum limit of 5MB");
            }
            return "stickers";
        }

        // Check if image
        if ("IMAGE".equals(typeUpper) || ALLOWED_IMAGE_EXTENSIONS.contains(extension) || contentType.startsWith("image/")) {
            if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension) || !ALLOWED_IMAGE_MIMES.contains(contentType)) {
                throw new BadRequestException("Invalid image format (" + extension + ", " + contentType + "). Allowed formats: JPG, JPEG, PNG, GIF, WEBP, SVG, BMP");
            }
            if (fileSize > MAX_IMAGE_SIZE) {
                throw new BadRequestException("Image file size exceeds maximum limit of 10MB");
            }
            return "images";
        }

        // Check if video
        if ("VIDEO".equals(typeUpper) || ALLOWED_VIDEO_EXTENSIONS.contains(extension) || contentType.startsWith("video/")) {
            if (!ALLOWED_VIDEO_EXTENSIONS.contains(extension) || !ALLOWED_VIDEO_MIMES.contains(contentType)) {
                throw new BadRequestException("Invalid video format (" + extension + ", " + contentType + "). Allowed formats: MP4, WEBM, MOV, MKV, 3GP, AVI");
            }
            if (fileSize > MAX_VIDEO_SIZE) {
                throw new BadRequestException("Video file size exceeds maximum limit of 50MB");
            }
            return "videos";
        }

        // Documents / general attachments
        if (fileSize > MAX_DOC_SIZE) {
            throw new BadRequestException("Attachment file size exceeds maximum limit of 25MB");
        }
        return "documents";
    }

    @Override
    public MessageType resolveMessageType(String category) {
        if (category == null) return MessageType.TEXT;
        return switch (category.toLowerCase()) {
            case "images" -> MessageType.IMAGE;
            case "videos" -> MessageType.VIDEO;
            case "stickers" -> MessageType.STICKER;
            default -> MessageType.TEXT;
        };
    }
}
