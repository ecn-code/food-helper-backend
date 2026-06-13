package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "MediaResponse", description = "Stored media metadata")
public record MediaResponse(
        @Schema(description = "Media identifier", example = "12")
        Long id,
        @Schema(description = "Normalized stored file name", example = "apple.jpg")
        String fileName,
        @Schema(description = "Stored MIME type", example = "image/jpeg")
        String contentType,
        @Schema(description = "Stored image size in bytes", example = "84231")
        Integer sizeBytes,
        @Schema(description = "Stored image width in pixels", example = "1280")
        Integer width,
        @Schema(description = "Stored image height in pixels", example = "960")
        Integer height
) {
}
