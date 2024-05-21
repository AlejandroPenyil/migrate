package com.soincon.migrate.filter;

import lombok.Data;

@Data
public class Content {
    private String id;
    private String exactName;
    private String parentDirectoryId;
    private String pathBase;
}
