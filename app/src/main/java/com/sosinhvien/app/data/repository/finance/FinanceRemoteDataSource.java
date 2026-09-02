package com.sosinhvien.app.data.repository.finance;

import com.sosinhvien.app.data.network.model.SyncModels;

public interface FinanceRemoteDataSource {

    SyncModels.PullResponse pullChanges(String userId, long lastSyncTime);

    SyncModels.PushResponse pushChanges(String userId, SyncModels.PushRequest request);
}
