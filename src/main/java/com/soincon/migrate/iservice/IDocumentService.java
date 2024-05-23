package com.soincon.migrate.iservice;

import com.soincon.migrate.dto.newDtos.DocumentDto;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface IDocumentService {
    @GET("document/findRootDocuments")
    Call<List<DocumentDto>> findRootDocuments();

    @POST("document/{pathbase}")
    Call<DocumentDto> createDocument(@Query("pathbase") String path, @Body DocumentDto documentDto);

    @GET("document/search")
    Call<List<DocumentDto>> searchDocument(@Query("name") String name,
                                           @Query("typeDoc") String typeDoc,
                                           @Query("idParent") Long id,
                                           @Query("idDocument") Long idDocument);

    @GET("document/{id}")
    Call<DocumentDto> getDocument(@Path("id") long id);
}
