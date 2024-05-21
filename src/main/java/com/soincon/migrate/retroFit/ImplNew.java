package com.soincon.migrate.retroFit;

import com.soincon.migrate.dto.newDtos.DocumentCreateDto;
import com.soincon.migrate.dto.newDtos.DocumentDto;
import com.soincon.migrate.dto.newDtos.FilterDocument;
import com.soincon.migrate.filter.FilterDirectory;
import com.soincon.migrate.filter.PaginatedList;
import com.soincon.migrate.iservice.Autorithation;
import com.soincon.migrate.iservice.IDocumentService;
import com.soincon.migrate.iservice.legacyfileService;
import com.soincon.migrate.security.AutentecationUser;
import com.soincon.migrate.security.Token;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;

public class ImplNew {
    IDocumentService iDocumentService;
    legacyfileService legacyfileService;
    Autorithation  autorithation;

    public ImplNew() throws IOException {
        autorithation = RetroFitJWT.getInstanceRetrofit().create(Autorithation.class);
        AutentecationUser autentecationUser = new AutentecationUser();
        Call<Token> call= autorithation.findFiles(autentecationUser);
        Response<Token> response = call.execute();
        Token token = response.body();
        System.out.println(token.getToken());
        iDocumentService = RetroFitNew.getInstanceRetrofit(token.getToken()).create(IDocumentService.class);
        legacyfileService = RetroFitNew.getInstanceRetrofit(token.getToken()).create(legacyfileService.class);
    }


    public List<DocumentDto> findDocuments() throws IOException {
        Call<List<DocumentDto>> call = iDocumentService.findRootDocuments();
        Response<List<DocumentDto>> response = call.execute();
        return response.body();
    }

    public DocumentDto createDocument(DocumentDto document, String path) throws IOException {
        Call<DocumentDto> call = iDocumentService.createDocument(path,document);
        Response<DocumentDto> response = call.execute();
        return response.body();
    }

    public List<DocumentCreateDto> findAllDocuments(FilterDocument filterDirectory) throws IOException {
        Call<PaginatedList<DocumentCreateDto>> call = legacyfileService.createDocument(filterDirectory);
        Response<PaginatedList<DocumentCreateDto>> response = call.execute();
        assert response.body() != null;
        return response.body().getResults();
    }
}
