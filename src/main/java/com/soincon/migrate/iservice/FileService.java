package com.soincon.migrate.iservice;


import com.soincon.migrate.dto.FileDto;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface FileService {
    @GET("/files")
    Call<List<FileDto>> findFiles();

    @GET("/files/{id}")
    Call<FileDto> getFiles(@Path("id") String id);

    @PUT("/files/{id}")
    Call<FileDto> updateFile(@Body FileDto dto,
                             @Path("id") String id);

    @POST("/files/logical")
    Call<FileDto> createFile(@Query("false") boolean createInexsistentFolderPath,
                             @Query("false") boolean createInexsistentFileType,
                             @Body FileDto dto);

//    @POST("/files/searchAll")
//    Call<PaginatedList<FileDto>> searchFilesByFilter(@Body FilterDirectory filterDirectory);
}
