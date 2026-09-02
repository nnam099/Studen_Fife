package com.sosinhvien.app.di;

import android.content.Context;

public final class Injector {

    private static AppComponent appComponent;

    private Injector() {
    }

    public static AppComponent getAppComponent(Context context) {
        if (appComponent == null) {
            appComponent = DaggerAppComponent.builder()
                    .context(context.getApplicationContext())
                    .networkModule(new NetworkModule())
                    .repositoryModule(new RepositoryModule())
                    .build();
        }
        return appComponent;
    }
}
