package com.soincon.migrate.retroFit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class RetroFitJWT {
    private static Retrofit retrofit;
    private static final String baseUrl = System.getProperty("api.security.base.url");

    public static Retrofit getInstanceRetrofit(){

        if (retrofit == null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder().readTimeout(60, TimeUnit.SECONDS);


            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }
}
