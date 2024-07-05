package com.soincon.migrate.iservice;

import es.snc.common.persistence.PaginatedList;
import es.snc.common.persistence.filter.Filter;
import es.snc.document.manager.dto.FileDto;
import es.snc.document.manager.dto.PathDto;
import es.snc.document.manager.persistence.filter.FileFilter;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import java.util.List;

public interface FileService {
    @GET("files")
    Call<List<FileDto>> findFiles();

    @POST("files/searchAll")
    Call<PaginatedList<FileDto>> searchFilesByFilter(@Body Filter<FileFilter> filterDirectory);

    @POST("resources/searchByPath")
    Call<List<FileDto>> searchByPath(@Query("includeResourcePath") boolean b,
                                     @Query("includeFileDescendants") boolean b1,
                                     @Query("includeDirectoryDescendants") boolean b2,
                                     @Query("level") int i,
                                     @Body PathDto pathDto);
}
