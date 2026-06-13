package com.eliascanalesnieto.foodhelper.infra;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("media")
public record MediaEntity(
        @Id Long id,
        @Column("file_name") String fileName,
        @Column("content_type") String contentType,
        @Column("size_bytes") Integer sizeBytes,
        @Column("width") Integer width,
        @Column("height") Integer height,
        @Column("data") byte[] data
) {
}
