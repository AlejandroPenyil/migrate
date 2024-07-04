package com.soincon.migrate.iservice;

import com.soincon.migrate.security.AuthenticationUser;
import com.soincon.migrate.security.Token;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface Autorithation {
    @POST("authenticate")
    Call<Token> findFiles(@Body AuthenticationUser authenticationUser);
}
