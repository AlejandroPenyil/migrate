package com.soincon.migrate.dto.newDtos;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DocumentDto {
    private Integer idDocument;
    private String name;
    private String mimeType;
    private String typeDoc;
    private Long idParent;
}
