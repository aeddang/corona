package com.dagger.module.app

import android.content.Context
import com.ironleft.corona.store.Repository
import com.ironleft.corona.store.SettingPreference
import com.skeleton.module.network.NetworkFactory

import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
class RepositoryModule {

    @Provides
    @Singleton
    fun provideRepository(@Named("appContext") ctx: Context,
                          setting:SettingPreference,
                          networkFactory: NetworkFactory


    ): Repository = Repository(ctx, setting, networkFactory)
}