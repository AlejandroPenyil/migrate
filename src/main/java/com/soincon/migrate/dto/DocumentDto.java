package com.soincon.migrate.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DocumentDto {
    private String name;
    private String mimeType;
    private String typeDoc;
    private Long idParent;
}
