package com.soincon.migrate.retroFit;

import com.soincon.migrate.security.AuthInterceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class RetroFitNew {
    private static Retrofit retrofit;
    private static String baseUrl;

    public static Retrofit getInstanceRetrofit(String token) throws IOException {
        Properties prop = new Properties();
        InputStream in = RetroFitNew.class.getClassLoader().getResourceAsStream("application.properties");
        prop.load(in);
        in.close();

//        baseUrl = prop.getProperty("api2.base.url");

        baseUrl = System.getProperty("api2.base.url");
        if (retrofit == null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                    .readTimeout(600, TimeUnit.SECONDS)
                    .addInterceptor(new AuthInterceptor(token));


            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }

}
