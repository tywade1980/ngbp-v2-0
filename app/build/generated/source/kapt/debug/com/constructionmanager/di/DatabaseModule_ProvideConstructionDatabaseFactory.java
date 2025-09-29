package com.constructionmanager.di;

import android.content.Context;
import com.constructionmanager.data.database.ConstructionDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class DatabaseModule_ProvideConstructionDatabaseFactory implements Factory<ConstructionDatabase> {
  private final Provider<Context> contextProvider;

  public DatabaseModule_ProvideConstructionDatabaseFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public ConstructionDatabase get() {
    return provideConstructionDatabase(contextProvider.get());
  }

  public static DatabaseModule_ProvideConstructionDatabaseFactory create(
      Provider<Context> contextProvider) {
    return new DatabaseModule_ProvideConstructionDatabaseFactory(contextProvider);
  }

  public static ConstructionDatabase provideConstructionDatabase(Context context) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideConstructionDatabase(context));
  }
}
