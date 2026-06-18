package com.constructionmanager.di

import android.content.Context
import com.constructionmanager.data.cloud.FirebaseInit
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseApp(@ApplicationContext context: Context): FirebaseApp =
        FirebaseInit.ensureInitialized(context)

    @Provides
    @Singleton
    fun provideFirestore(app: FirebaseApp): FirebaseFirestore =
        FirebaseFirestore.getInstance(app)

    @Provides
    @Singleton
    fun provideFirebaseAuth(app: FirebaseApp): FirebaseAuth =
        FirebaseAuth.getInstance(app)
}
