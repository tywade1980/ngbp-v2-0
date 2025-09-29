package com.constructionmanager.data.repository;

import android.content.SharedPreferences;
import com.constructionmanager.data.network.AuthApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class AuthRepositoryImpl_Factory implements Factory<AuthRepositoryImpl> {
  private final Provider<AuthApiService> authApiServiceProvider;

  private final Provider<SharedPreferences> sharedPreferencesProvider;

  public AuthRepositoryImpl_Factory(Provider<AuthApiService> authApiServiceProvider,
      Provider<SharedPreferences> sharedPreferencesProvider) {
    this.authApiServiceProvider = authApiServiceProvider;
    this.sharedPreferencesProvider = sharedPreferencesProvider;
  }

  @Override
  public AuthRepositoryImpl get() {
    return newInstance(authApiServiceProvider.get(), sharedPreferencesProvider.get());
  }

  public static AuthRepositoryImpl_Factory create(Provider<AuthApiService> authApiServiceProvider,
      Provider<SharedPreferences> sharedPreferencesProvider) {
    return new AuthRepositoryImpl_Factory(authApiServiceProvider, sharedPreferencesProvider);
  }

  public static AuthRepositoryImpl newInstance(AuthApiService authApiService,
      SharedPreferences sharedPreferences) {
    return new AuthRepositoryImpl(authApiService, sharedPreferences);
  }
}
