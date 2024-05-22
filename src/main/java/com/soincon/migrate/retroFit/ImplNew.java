package com.soincon.migrate.retroFit;

import com.soincon.migrate.dto.newDtos.DocumentDto;
import com.soincon.migrate.iservice.Autorithation;
import com.soincon.migrate.iservice.IDocumentService;
import com.soincon.migrate.security.AutentecationUser;
import com.soincon.migrate.security.Token;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;

public class ImplNew {
    IDocumentService iDocumentService;
    Autorithation autorithation;

    public ImplNew() throws IOException {
        autorithation = RetroFitJWT.getInstanceRetrofit().create(Autorithation.class);
        AutentecationUser autentecationUser = new AutentecationUser();
        Call<Token> call = autorithation.findFiles(autentecationUser);
        Response<Token> response = call.execute();
        Token token = response.body();
        iDocumentService = RetroFitNew.getInstanceRetrofit(token.getToken()).create(IDocumentService.class);
    }


    public List<DocumentDto> findDocuments() throws IOException {
        Call<List<DocumentDto>> call = iDocumentService.findRootDocuments();
        Response<List<DocumentDto>> response = call.execute();
        return response.body();
    }

    public DocumentDto createDocument(DocumentDto document, String path) throws IOException {
        Call<DocumentDto> call = iDocumentService.createDocument(path, document);
        Response<DocumentDto> response = call.execute();
        return response.body();
    }

    public List<DocumentDto> search(DocumentDto documentDto) throws IOException {
        Call<List<DocumentDto>> call = iDocumentService.searchDocument(documentDto.getName(), documentDto.getTypeDoc());
        Response<List<DocumentDto>> response = call.execute();
        assert response.body() != null;
        return response.body();
    }

    public DocumentDto getDocument(long id) throws IOException {
        Call<DocumentDto> call = iDocumentService.getDocument(id);
        Response<DocumentDto> response = call.execute();
        return response.body();
    }
}
