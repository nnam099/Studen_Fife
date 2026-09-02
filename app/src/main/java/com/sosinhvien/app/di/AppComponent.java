package com.sosinhvien.app.di;

import android.content.Context;

import com.sosinhvien.app.data.repository.finance.DefaultFinanceRepository;
import com.sosinhvien.app.data.repository.finance.FinanceRepository;
import com.sosinhvien.app.ui.finance.FinanceViewModel;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

@Singleton
@Component(modules = {NetworkModule.class, RepositoryModule.class})
public interface AppComponent {

    Context context();

    FinanceRepository financeRepository();

    DefaultFinanceRepository defaultFinanceRepository();

    void inject(FinanceViewModel viewModel);

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder context(Context context);

        Builder networkModule(NetworkModule networkModule);

        Builder repositoryModule(RepositoryModule repositoryModule);

        AppComponent build();
    }
}
