package com.soincon.migrate.retroFit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class RetroFitNew {
    private static Retrofit retrofit;
    private static String baseUrl;
    private static String tken;

    public static Retrofit getInstanceRetrofit() {
//        tken=token;
        baseUrl = System.getProperty("api2.base.url");
        if (retrofit == null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                    .readTimeout(600, TimeUnit.SECONDS)
                    /*.addInterceptor(new AuthInterceptor(token))*/;


            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }

    public static String getToken(){
        return tken;
    }
}
