package com.sosinhvien.app.di;

import com.sosinhvien.app.data.database.AppDatabase;
import com.sosinhvien.app.data.network.ApiService;
import com.sosinhvien.app.data.repository.finance.DefaultFinanceRepository;
import com.sosinhvien.app.data.repository.finance.FinanceLocalDataSource;
import com.sosinhvien.app.data.repository.finance.FinanceRemoteDataSource;
import com.sosinhvien.app.data.repository.finance.FinanceRepository;
import com.sosinhvien.app.data.repository.finance.RetrofitFinanceRemoteDataSource;
import com.sosinhvien.app.data.repository.finance.RoomFinanceLocalDataSource;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class RepositoryModule {

    @Provides
    @Singleton
    public FinanceRepository provideFinanceRepository(FinanceLocalDataSource localDataSource, FinanceRemoteDataSource remoteDataSource) {
        return new DefaultFinanceRepository(localDataSource, remoteDataSource);
    }

    @Provides
    @Singleton
    public FinanceLocalDataSource provideFinanceLocalDataSource(AppDatabase database) {
        return new RoomFinanceLocalDataSource(database);
    }

    @Provides
    @Singleton
    public FinanceRemoteDataSource provideFinanceRemoteDataSource(ApiService apiService) {
        return new RetrofitFinanceRemoteDataSource(apiService);
    }
}
