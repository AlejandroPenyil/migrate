package com.soincon.migrate.iservice;

import com.soincon.migrate.dto.oldDtos.DirectoryDto;
import com.soincon.migrate.filter.FilterDirectory;
import com.soincon.migrate.filter.PaginatedList;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface DirectoryService {
    @GET("/directories/{id}")
    Call<DirectoryDto> getDirectories(@Path("id") String id);

    @GET("/directories")
    Call<List<DirectoryDto>> findDirectories();

    @POST("/directories/create-all/logical")
    Call<List<DirectoryDto>> createDirectory(@Body List<DirectoryDto> dto);

    @PUT("/directories/{id}")
    Call<DirectoryDto> updateDirectory(@Body DirectoryDto dto,
                                       @Path("id") String id);

    @POST("/directories/searchOne")
    Call<DirectoryDto> searchDirectoryByFilter(@Body FilterDirectory filterDirectory,
                                               @Query("firstResult") Boolean firstResult);

    @POST("/directories/searchAll")
    Call<PaginatedList<DirectoryDto>> searchDirectoryByFilterAll(@Body FilterDirectory filterDirectory);

    @DELETE("/directories/{id}/purge")
    Call<Void> purgeDirectoryById(@Path("id") String id);
}
