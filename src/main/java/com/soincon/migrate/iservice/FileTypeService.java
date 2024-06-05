package com.soincon.migrate.iservice;


import com.soincon.migrate.dto.oldDtos.FileTypeDto;
import retrofit2.Call;
import retrofit2.http.GET;

import java.util.List;

public interface FileTypeService {
    @GET("file-types")
    Call<List<FileTypeDto>> getFileTypes();
}
