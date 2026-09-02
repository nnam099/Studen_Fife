package com.sosinhvien.app.data.repository.finance;

import androidx.annotation.NonNull;

import com.sosinhvien.app.data.network.ApiService;
import com.sosinhvien.app.data.network.model.SyncModels;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Response;

@Singleton
public class RetrofitFinanceRemoteDataSource implements FinanceRemoteDataSource {

    private final ApiService apiService;

    @Inject
    public RetrofitFinanceRemoteDataSource(@NonNull ApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public SyncModels.PullResponse pullChanges(String userId, long lastSyncTime) {
        try {
            Call<SyncModels.PullResponse> call = apiService.pull(new SyncModels.PullRequest(lastSyncTime));
            Response<SyncModels.PullResponse> response = call.execute();
            if (!response.isSuccessful()) {
                throw new IOException("Pull finance data failed with code: " + response.code());
            }
            if (response.body() == null) {
                return new SyncModels.PullResponse();
            }
            return response.body();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to pull finance data from remote source.", exception);
        }
    }

    @Override
    public SyncModels.PushResponse pushChanges(String userId, SyncModels.PushRequest request) {
        try {
            Call<SyncModels.PushResponse> call = apiService.push(request);
            Response<SyncModels.PushResponse> response = call.execute();
            if (!response.isSuccessful()) {
                throw new IOException("Push finance data failed with code: " + response.code());
            }
            if (response.body() == null) {
                return new SyncModels.PushResponse();
            }
            return response.body();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to push finance data to remote source.", exception);
        }
    }
}
