package com.eliascanalesnieto.foodhelper.presentation;

import com.eliascanalesnieto.foodhelper.domain.MediaUpload;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "PhotoUploadRequest", description = "Photo payload encoded as base64")
public record PhotoUploadRequest(
        @Schema(description = "Original file name", example = "apple.png")
        @NotBlank String fileName,
        @Schema(description = "Original MIME type reported by the client", example = "image/png")
        @NotBlank String contentType,
        @Schema(description = "Base64-encoded image binary", example = "iVBORw0KGgoAAAANSUhEUgAA...")
        @NotBlank String base64Data
) {
    public MediaUpload toDomain() {
        return MediaUpload.builder()
                .fileName(fileName)
                .contentType(contentType)
                .base64Data(base64Data)
                .build();
    }
}
