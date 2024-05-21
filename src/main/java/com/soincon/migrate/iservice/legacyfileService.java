package com.soincon.migrate.iservice;

import com.soincon.migrate.dto.newDtos.DocumentCreateDto;
import com.soincon.migrate.dto.newDtos.DocumentDto;
import com.soincon.migrate.dto.newDtos.FilterDocument;
import com.soincon.migrate.filter.FilterDirectory;
import com.soincon.migrate.filter.PaginatedList;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface legacyfileService {
    //TODO crear el filtro para buscar los documents
    @POST("legacyfile/searchAll")
    Call<PaginatedList<DocumentCreateDto>> createDocument(@Body FilterDocument documentDto);
}
