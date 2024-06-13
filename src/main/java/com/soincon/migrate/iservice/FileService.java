package com.soincon.migrate.iservice;


import com.soincon.migrate.dto.oldDtos.FileDto;
import com.soincon.migrate.dto.oldDtos.PathDto;
import com.soincon.migrate.filter.FilterDirectory;
import com.soincon.migrate.filter.PaginatedList;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import java.util.List;

public interface FileService {
    @GET("files")
    Call<List<FileDto>> findFiles();

//    @GET("files/{id}")
//    Call<FileDto> getFiles(@Path("id") String id);
//
//    @PUT("files/{id}")
//    Call<FileDto> updateFile(@Body FileDto dto,
//                             @Path("id") String id);
//
//    @POST("files/logical")
//    Call<FileDto> createFile(@Query("false") boolean createInexsistentFolderPath,
//                             @Query("false") boolean createInexsistentFileType,
//                             @Body FileDto dto);

    @POST("files/searchAll")
    Call<PaginatedList<FileDto>> searchFilesByFilter(@Body FilterDirectory filterDirectory);

    @POST("resources/searchByPath")
    Call<List<FileDto>> searchByPath(@Query("includeResourcePath") boolean b,
                                     @Query("includeFileDescendants") boolean b1,
                                     @Query("includeDirectoryDescendants") boolean b2,
                                     @Query("level") int i,
                                     @Body PathDto pathDto);
}
