package com.soincon.migrate.dto.oldDtos;

import lombok.Data;

@Data
public class FileTypeDto {
    private String mimeType;
    private String name;
    private String extension;
}
