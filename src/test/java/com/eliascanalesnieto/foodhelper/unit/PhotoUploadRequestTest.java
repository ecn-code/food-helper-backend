package com.eliascanalesnieto.foodhelper.unit;

import static org.assertj.core.api.Assertions.assertThat;

import com.eliascanalesnieto.foodhelper.domain.MediaUpload;
import com.eliascanalesnieto.foodhelper.presentation.PhotoUploadRequest;
import org.junit.jupiter.api.Test;

class PhotoUploadRequestTest {

    @Test
    void shouldPreserveRawBase64DataWithoutDataUriPrefix() {
        String rawBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO7+f8QAAAAASUVORK5CYII=";

        MediaUpload upload = new PhotoUploadRequest("tiny.png", "image/png", rawBase64).toDomain();

        assertThat(upload.getBase64Data()).isEqualTo(rawBase64);
        assertThat(upload.getBase64Data()).doesNotStartWith("data:image/");
    }
}
