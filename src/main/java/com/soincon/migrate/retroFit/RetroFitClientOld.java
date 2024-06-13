package com.soincon.migrate.retroFit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class RetroFitClientOld {
    private static Retrofit retrofit;

    public static Retrofit getInstanceRetrofit() throws IOException {
        Properties prop = new Properties();
        InputStream in = RetroFitClientOld.class.getClassLoader().getResourceAsStream("application.properties");
        prop.load(in);
        in.close();

//        String baseUrl = prop.getProperty("api.base.url");
        String baseUrl = System.getProperty("api.base.url");

        if (retrofit == null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder().readTimeout(600, TimeUnit.SECONDS);


            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }

}
