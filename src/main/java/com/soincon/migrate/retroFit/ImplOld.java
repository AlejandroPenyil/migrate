package com.soincon.migrate.retroFit;

import com.soincon.migrate.dto.FileDto;
import com.soincon.migrate.iservice.FileService;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;

public class ImplOld {
    FileService fileService;

    public ImplOld() throws IOException {
        fileService = RetroFitClientOld.getInstanceRetrofit().create(FileService.class);
    }


    public List<FileDto> findFiles() throws IOException {
        Call<List<FileDto>> call = fileService.findFiles();
        Response<List<FileDto>> response = call.execute();
        return response.body();
    }
}
