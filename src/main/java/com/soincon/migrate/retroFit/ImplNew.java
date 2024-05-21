package com.soincon.migrate.retroFit;

import com.soincon.migrate.dto.DocumentDto;
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
    Autorithation  autorithation;

    public ImplNew() throws IOException {
        autorithation = RetroFitJWT.getInstanceRetrofit().create(Autorithation.class);
        AutentecationUser autentecationUser = new AutentecationUser();
        Call<Token> call= autorithation.findFiles(autentecationUser);
        Response<Token> response = call.execute();
        Token token = response.body();
        System.out.println(token.getToken());
        iDocumentService = RetroFitNew.getInstanceRetrofit(token.getToken()).create(IDocumentService.class);
    }


    public List<DocumentDto> findDocuments() throws IOException {
        Call<List<DocumentDto>> call = iDocumentService.findRootDocuments();
        Response<List<DocumentDto>> response = call.execute();
        return response.body();
    }
}
