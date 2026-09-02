package com.sosinhvien.app.di;

import android.content.Context;

import com.sosinhvien.app.data.database.AppDatabase;
import com.sosinhvien.app.data.network.ApiClient;
import com.sosinhvien.app.data.network.ApiService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class NetworkModule {

    @Provides
    @Singleton
    public ApiService provideApiService(Context context) {
        return ApiClient.getApiService(context);
    }

    @Provides
    @Singleton
    public AppDatabase provideAppDatabase(Context context) {
        return AppDatabase.getInstance(context);
    }
}
