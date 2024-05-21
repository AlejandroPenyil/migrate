package com.soincon.migrate.dto.newDtos;

import com.soincon.migrate.filter.Content;
import lombok.Data;

@Data
public class FilterDocument {
    private int page = 0;
    private int size = 1000;
    private ContentNew content;
}
