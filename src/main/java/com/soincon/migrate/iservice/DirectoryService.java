package com.soincon.migrate.iservice;

import es.snc.common.persistence.PaginatedList;
import es.snc.common.persistence.filter.Filter;
import es.snc.document.manager.dto.DirectoryDto;
import es.snc.document.manager.dto.PathDto;
import es.snc.document.manager.persistence.filter.DirectoryFilter;

import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface DirectoryService {
    @GET("/directories/{id}")
    Call<DirectoryDto> getDirectories(@Path("id") String id);

//    @GET("/directories")
//    Call<List<DirectoryDto>> findDirectories();
//
//    @POST("/directories/create-all/logical")
//    Call<List<DirectoryDto>> createDirectory(@Body List<DirectoryDto> dto);
//
//    @PUT("/directories/{id}")
//    Call<DirectoryDto> updateDirectory(@Body DirectoryDto dto,
//                                       @Path("id") String id);
//
//    @POST("/directories/searchOne")
//    Call<DirectoryDto> searchDirectoryByFilter(@Body FilterDirectory filterDirectory,
//                                               @Query("firstResult") Boolean firstResult);

    @POST("directories/searchAll")
    Call<PaginatedList<DirectoryDto>> searchDirectoryByFilterAll(@Body Filter<DirectoryFilter> filterDirectory);

    @POST("resources/searchByPath")
    Call<List<DirectoryDto>> searchByPath(@Query("includeResourcePath") boolean iResource,
                                          @Query("includeFileDescendants") boolean iFile,
                                          @Query("includeDirectoryDescendants") boolean iDirectory,
                                          @Query("level") Integer level,
                                          @Body PathDto pathDto);

}
