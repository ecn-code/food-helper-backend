package com.eliascanalesnieto.foodhelper.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MediaUpload {
    String fileName;
    String contentType;
    String base64Data;
}
