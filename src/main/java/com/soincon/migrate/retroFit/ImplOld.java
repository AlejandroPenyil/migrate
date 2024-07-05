package com.soincon.migrate.retroFit;

import com.soincon.migrate.iservice.DirectoryService;
import com.soincon.migrate.iservice.FileService;
import com.soincon.migrate.iservice.FileTypeService;

import es.snc.common.persistence.PaginatedList;
import es.snc.common.persistence.filter.Filter;
import es.snc.document.manager.dto.DirectoryDto;
import es.snc.document.manager.dto.FileDto;
import es.snc.document.manager.dto.FileTypeDto;
import es.snc.document.manager.dto.PathDto;
import es.snc.document.manager.persistence.filter.DirectoryFilter;
import es.snc.document.manager.persistence.filter.FileFilter;

import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;

public class ImplOld {
    FileService fileService;
    FileTypeService fileTypeService;
    DirectoryService directoryService;

    public ImplOld() {
        fileService = RetroFitClientOld.getInstanceRetrofit().create(FileService.class);
        fileTypeService = RetroFitClientOld.getInstanceRetrofit().create(FileTypeService.class);
        directoryService = RetroFitClientOld.getInstanceRetrofit().create(DirectoryService.class);
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

    public List<FileDto> searchFileByFilterAll(Filter<FileFilter> filterDirectory) throws IOException {
        Call<PaginatedList<FileDto>> call = fileService.searchFilesByFilter(filterDirectory);
        Response<PaginatedList<FileDto>> response = call.execute();
        assert response.body() != null;
        return response.body().getResults();
    }

    public List<DirectoryDto> searchDirectoryByFilterAll(Filter<DirectoryFilter> filterDirectory) throws IOException {
        Call<PaginatedList<DirectoryDto>> call = directoryService.searchDirectoryByFilterAll(filterDirectory);
        Response<PaginatedList<DirectoryDto>> response = call.execute();
        assert response.body() != null;
        return response.body().getResults();
    }

    public DirectoryDto getDirectory(String uuid) throws IOException {
        Call<DirectoryDto> call = directoryService.getDirectories(uuid);
        Response<DirectoryDto> response = call.execute();
        return response.body();
    }

    public List<DirectoryDto> getDirectoryPathBase(PathDto pathDto) throws IOException {
        Call<List<DirectoryDto>> call = directoryService.searchByPath(true, false, false, 0, pathDto);
        Response<List<DirectoryDto>> response = call.execute();
        return response.body();
    }

    public List<FileDto> getFilePathBase(PathDto pathDto) throws IOException {
        Call<List<FileDto>> call = fileService.searchByPath(true, false, false, 0, pathDto);
        Response<List<FileDto>> response = call.execute();
        return response.body();
    }
}
