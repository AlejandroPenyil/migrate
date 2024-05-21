package com.soincon.migrate.dto.oldDtos;

import lombok.Data;

@Data
public class DirectoryDto {
    private String id;
    private String name;
    private String parentDirectoryId;
    private String pathBase;
    private Integer versionLock;
}
