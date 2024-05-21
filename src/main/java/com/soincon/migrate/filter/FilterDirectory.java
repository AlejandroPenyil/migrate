package com.soincon.migrate.filter;

import lombok.Data;

@Data
public class FilterDirectory {
    private int page = 0;
    private int size = 1000;
    private Content content;
}
