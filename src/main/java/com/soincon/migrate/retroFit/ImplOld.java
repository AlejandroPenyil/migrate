package com.soincon.migrate.retroFit;

import com.soincon.migrate.dto.oldDtos.FileDto;
import com.soincon.migrate.dto.oldDtos.FileTypeDto;
import com.soincon.migrate.filter.FilterDirectory;
import com.soincon.migrate.filter.PaginatedList;
import com.soincon.migrate.iservice.FileService;
import com.soincon.migrate.iservice.FileTypeService;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;

public class ImplOld {
    FileService fileService;
    FileTypeService fileTypeService;

    public ImplOld() throws IOException {
        fileService = RetroFitClientOld.getInstanceRetrofit().create(FileService.class);
        fileTypeService = RetroFitClientOld.getInstanceRetrofit().create(FileTypeService.class);
    }

    public List<FileTypeDto> getFileTypes() throws IOException {
        Call<List<FileTypeDto>> callTypes = fileTypeService.getFileTypes();
        Response<List<FileTypeDto>> responseTypes = callTypes.execute();
        return responseTypes.body();
    }


    public List<FileDto> findFiles() throws IOException {
        Call<List<FileDto>> call = fileService.findFiles();
        Response<List<FileDto>> response = call.execute();
        return response.body();
    }

    public List<FileDto> searchFileByFilterAll(FilterDirectory filterDirectory) throws IOException {
        Call<PaginatedList<FileDto>> call = fileService.searchFilesByFilter(filterDirectory);
        Response<PaginatedList<FileDto>> response = call.execute();
        assert response.body() != null;
        return  response.body().getResults();
    }
}
