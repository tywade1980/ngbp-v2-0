package com.constructionmanager.di

import android.content.Context
import com.constructionmanager.data.subscriber.LauncherClient
import com.constructionmanager.data.subscriber.PrefsSubscriberStore
import com.constructionmanager.data.subscriber.SubscriberStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

/** The subscriber front door: the platform launcher client and its on-phone state. */
@Module
@InstallIn(SingletonComponent::class)
object SubscriberModule {

    @Provides
    @Singleton
    fun provideSubscriberStore(@ApplicationContext context: Context): SubscriberStore =
        PrefsSubscriberStore(context.getSharedPreferences(PrefsSubscriberStore.FILE, Context.MODE_PRIVATE))

    @Provides
    @Singleton
    fun provideLauncherClient(client: OkHttpClient): LauncherClient = LauncherClient(client)
}
