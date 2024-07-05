package com.soincon.migrate.iservice;

import emisuite.documentmanager.dto.DocumentVersionDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface IDocumentVersionService {
    @POST("document-version/create")
    Call<DocumentVersionDto> createDocument(@Body DocumentVersionDto documentCreateDto);
}
