package com.connectly.service;

import com.connectly.constant.MessageType;
import org.springframework.web.multipart.MultipartFile;

public interface MediaValidationService {
    String validateAndResolveCategory(MultipartFile file, String requestedType);
    MessageType resolveMessageType(String category);
}
