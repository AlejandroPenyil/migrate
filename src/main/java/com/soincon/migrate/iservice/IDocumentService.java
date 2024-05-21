package com.soincon.migrate.iservice;

import com.soincon.migrate.dto.DocumentDto;
import com.soincon.migrate.dto.FileDto;
import retrofit2.Call;
import retrofit2.http.GET;

import java.util.List;

public interface IDocumentService {
    @GET("document/findRootDocuments")
    Call<List<DocumentDto>> findRootDocuments();
}
