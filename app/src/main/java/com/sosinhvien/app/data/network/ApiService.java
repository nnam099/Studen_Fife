package com.sosinhvien.app.data.network;

import com.sosinhvien.app.data.network.model.AuthModels;
import com.sosinhvien.app.data.network.model.SyncModels;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/auth/register")
    Call<AuthModels.AuthResponse> register(@Body AuthModels.RegisterRequest request);

    @POST("api/auth/login")
    Call<AuthModels.AuthResponse> login(@Body AuthModels.LoginRequest request);

    @POST("api/sync/pull")
    Call<SyncModels.PullResponse> pull(@Body SyncModels.PullRequest request);

    @POST("api/sync/push")
    Call<SyncModels.PushResponse> push(@Body SyncModels.PushRequest request);
}
