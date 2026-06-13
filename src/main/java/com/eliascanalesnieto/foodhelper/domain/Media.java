package com.eliascanalesnieto.foodhelper.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Media {
    Long id;
    String fileName;
    String contentType;
    Integer sizeBytes;
    Integer width;
    Integer height;
    byte[] data;
}
