package com.soincon.migrate.iservice;

import com.soincon.migrate.dto.newDtos.DocumentDto;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface IDocumentService {
    @GET("document/findRootDocuments")
    Call<List<DocumentDto>> findRootDocuments();

    @POST("document/pathbase")
    Call<DocumentDto> createDocument(@Query("pathBase") String path, @Body DocumentDto documentDto);

    @GET("document/search")
    Call<List<DocumentDto>> searchDocument(@Query("name") String name,
                                           @Query("typeDoc") String typeDoc,
                                           @Query("idParent") Long id,
                                           @Query("idDocument") Long idDocument);

    @GET("document/{id}")
    Call<DocumentDto> getDocument(@Path("id") long id);

    @PUT("document/{id}")
    Call<DocumentDto> updateDocument(@Path("id") long id, @Body DocumentDto documentDto);

    @GET("document/searchByPathAndIdParent")
    Call<List<DocumentDto>> searchByPathBase(@Query("pathBase") String PathBase,
                                             @Query("idParent") Integer idParent,
                                             @Query("isFile") boolean f);

    @POST("document/moveDocument")
    Call<List<DocumentDto>> moveDocument(@Query("idTarget") Integer idTarget,
                                         @Query("pathBase") String pathBase,
                                         @Body List<Integer> ids);

    @POST("document/copyDocument")
    Call<List<DocumentDto>> copyDocument(@Query("idTarget") Integer idTarget,
                                         @Query("pathBase") String pathBase,
                                         @Query("isOnlyContent") boolean content,
                                         @Body List<Integer> ids);

    @GET("legacyfile/findByUUID/{idDocument}")
    Call<DocumentDto> findDocumentByUUID(@Path("idDocument") String idDocument);
}
