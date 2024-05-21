package com.soincon.migrate.filter;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class PaginatedList<T> {
    private List<T> results = new LinkedList<>();
    private int page;
    private int size;
    private long rownum;
}
