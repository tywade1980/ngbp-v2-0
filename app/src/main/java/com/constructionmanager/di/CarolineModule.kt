package com.constructionmanager.di

import android.content.Context
import com.constructionmanager.data.caroline.CarolineConnector
import com.constructionmanager.data.caroline.ConnectorSettings
import com.constructionmanager.data.caroline.Outbox
import com.constructionmanager.data.caroline.PrefsConnectorSettings
import com.constructionmanager.data.caroline.PrefsOutbox
import com.constructionmanager.data.caroline.RoomWriters
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

/** The one connector to Caroline, and where it keeps its address, token and outbox (a file excluded from backup). */
@Module
@InstallIn(SingletonComponent::class)
object CarolineModule {

    @Provides
    @Singleton
    fun provideConnectorSettings(@ApplicationContext context: Context): ConnectorSettings =
        PrefsConnectorSettings(context.getSharedPreferences(PrefsConnectorSettings.FILE, Context.MODE_PRIVATE))

    @Provides
    @Singleton
    fun provideOutbox(@ApplicationContext context: Context): Outbox =
        PrefsOutbox(context.getSharedPreferences(PrefsConnectorSettings.FILE, Context.MODE_PRIVATE))

    @Provides
    @Singleton
    fun provideCarolineConnector(client: OkHttpClient, settings: ConnectorSettings, outbox: Outbox): CarolineConnector =
        CarolineConnector(client, settings, outbox)

    /** The room's writers (HOCC 0.6): the same token, the same address, sent once and never retried. */
    @Provides
    @Singleton
    fun provideRoomWriters(connector: CarolineConnector, client: OkHttpClient, settings: ConnectorSettings): RoomWriters =
        RoomWriters(connector, client, settings)
}
