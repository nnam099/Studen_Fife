package com.sosinhvien.app.data.network;

import android.content.Context;

import com.sosinhvien.app.util.SessionManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // 10.0.2.2 maps to host's localhost inside the Android Emulator
    private static final String BASE_URL = "http://10.0.2.2:5000/";
    private static ApiService apiService;

    public static synchronized ApiService getApiService(Context context) {
        if (apiService == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(chain -> {
                        SessionManager session = new SessionManager(context.getApplicationContext());
                        String token = session.getToken();
                        
                        Request.Builder builder = chain.request().newBuilder();
                        if (token != null && !token.isEmpty()) {
                            builder.addHeader("Authorization", "Bearer " + token);
                        }
                        return chain.proceed(builder.build());
                    })
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }
}
