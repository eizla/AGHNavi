package com.aghnavi.agh_navi.Api;


import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

        public static final String BASE_URL = "https://campusnavigation.preview.cloudart.pl/";
        public static Retrofit retrofit;

        public static Retrofit getClient(){

            if(retrofit == null){

                OkHttpClient.Builder builder = new OkHttpClient.Builder();
                builder.interceptors().add(new AuthInterceptor("username", "pass"));
                OkHttpClient client = builder.build();


                retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(client)
                        .build();
            }

            return retrofit;
        }

}
