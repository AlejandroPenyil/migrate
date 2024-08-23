package com.soincon.migrate.retroFit;

import com.soincon.migrate.security.AuthInterceptor;
import emisuite.documentmanager.dto.DocumentDto;
import emisuite.documentmanager.dto.DocumentVersionDto;
import com.soincon.migrate.iservice.Authorization;
import com.soincon.migrate.iservice.IDocumentService;
import com.soincon.migrate.iservice.IDocumentVersionService;
import com.soincon.migrate.security.AuthenticationUser;
import com.soincon.migrate.security.Token;
import lombok.extern.log4j.Log4j2;
import okhttp3.OkHttpClient;
import org.springframework.cache.annotation.Cacheable;
import retrofit2.Call;
import retrofit2.Response;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Log4j2
public class ImplNew {
    IDocumentService iDocumentService;
    IDocumentVersionService iDocumentVersionService;
    Authorization authorization;
    public static Token Jwtoken;

    public ImplNew() throws IOException {
        authorization = RetroFitJWT.getInstanceRetrofit().create(Authorization.class);
        Jwtoken = updateJwt();
//        AuthenticationUser authenticationUser = new AuthenticationUser();
//        Call<Token> call = authorization.findFiles(authenticationUser);
//        Response<Token> response = call.execute();
//        Token token = response.body();
//        Jwtoken = token;
//        iDocumentService = RetroFitNew.getInstanceRetrofit(token.getToken()).create(IDocumentService.class);
//        iDocumentVersionService = RetroFitNew.getInstanceRetrofit(token.getToken()).create(IDocumentVersionService.class);
    }
    @Cacheable(value = "jwtCache")
    public Token updateJwt() throws IOException {
        AuthenticationUser authenticationUser = new AuthenticationUser();
        Call<Token> call = authorization.findFiles(authenticationUser);
        Response<Token> response = call.execute();
        Token token = response.body();

        log.info("Actualizando JWT {}",token.getToken());

//        iDocumentService = RetroFitNew.getInstanceRetrofit(token.getToken()).create(IDocumentService.class);
        iDocumentService = RetroFitNew.getInstanceRetrofit(/*token.getToken()*/).newBuilder().client(new OkHttpClient.Builder()
                .readTimeout(600, TimeUnit.SECONDS)
                .addInterceptor(new AuthInterceptor(token.getToken())).build()).build().create(IDocumentService.class);

        iDocumentVersionService = RetroFitNew.getInstanceRetrofit(/*token.getToken()*/).newBuilder().client(new OkHttpClient.Builder()
                .readTimeout(600, TimeUnit.SECONDS)
                .addInterceptor(new AuthInterceptor(token.getToken())).build()).build().create(IDocumentVersionService.class);

        return token;
    }

    public DocumentDto createDocument(DocumentDto document, String path) throws Exception {
        Call<DocumentDto> call = iDocumentService.createDocument(path, document);
        Response<DocumentDto> response = call.execute();
        return response.body();
    }

    public List<DocumentDto> search(DocumentDto documentDto) throws IOException {
        Call<List<DocumentDto>> call = iDocumentService.searchDocument(documentDto.getName(), documentDto.getTypeDoc(), documentDto.getIdParent(), documentDto.getIdDocument());
        Response<List<DocumentDto>> response = call.execute();
        assert response.body() != null;
        return response.body();
    }

    public DocumentDto getDocument(long id) throws IOException {
        Call<DocumentDto> call = iDocumentService.getDocument(id);
        Response<DocumentDto> response = call.execute();
        return response.body();
    }

    public DocumentVersionDto documentCreateDto(DocumentVersionDto documentCreateDto) throws IOException {
        Call<DocumentVersionDto> call = iDocumentVersionService.createDocument(documentCreateDto);
        Response<DocumentVersionDto> response = call.execute();
        return response.body();
    }

    public DocumentDto updateDocument(DocumentDto documentDto) throws IOException {
        Call<DocumentDto> call = iDocumentService.updateDocument(documentDto.getIdDocument(), documentDto);
        Response<DocumentDto> response = call.execute();
        return response.body();
    }

    public List<DocumentDto> searchByPath(String f) throws IOException {
        String path = f.replace("\\", "/");
        Call<List<DocumentDto>> call = iDocumentService.searchByPathBase(path, null, false);
        Response<List<DocumentDto>> response = call.execute();
        return response.body();
    }

    public List<DocumentDto> searchByPathName(File f) throws IOException {
        String path = f.getName();
        Call<List<DocumentDto>> call = iDocumentService.searchByPathBase(path, null, false);
        Response<List<DocumentDto>> response = call.execute();
        return response.body();
    }

    public List<DocumentDto> moveDocuments(Long id, Integer idTarget, String pathBase) throws IOException {
        Call<List<DocumentDto>> call = iDocumentService.moveDocument(idTarget, pathBase, Collections.singletonList(id));
        Response<List<DocumentDto>> response = call.execute();
        return response.body();
    }

    public DocumentDto findByUUID(String UUID) throws IOException {
        Call<DocumentDto> call = iDocumentService.findDocumentByUUID(UUID);
        Response<DocumentDto> response = call.execute();
        return response.body();
    }
}
