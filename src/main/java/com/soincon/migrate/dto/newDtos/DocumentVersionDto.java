package com.soincon.migrate.dto.newDtos;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class DocumentVersionDto {
    private Long idDocument;
    private Integer idVersion;
    private String versionStatus;
    private String mimeType;
    private String reason;
    private Integer idUser;
    private String externalCode;
    private String uploadDate;
}
