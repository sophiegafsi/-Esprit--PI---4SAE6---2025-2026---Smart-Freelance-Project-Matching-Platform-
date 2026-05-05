package com.example.recompense.DTO;

public record CertificatePayloadDTO(
        String fileName,
        String contentType,
        String base64Data
) {
}
