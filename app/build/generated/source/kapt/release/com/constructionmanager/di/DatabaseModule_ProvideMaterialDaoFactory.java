package com.constructionmanager.di;

import com.constructionmanager.data.database.ConstructionDatabase;
import com.constructionmanager.data.database.dao.MaterialDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class DatabaseModule_ProvideMaterialDaoFactory implements Factory<MaterialDao> {
  private final Provider<ConstructionDatabase> databaseProvider;

  public DatabaseModule_ProvideMaterialDaoFactory(Provider<ConstructionDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public MaterialDao get() {
    return provideMaterialDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideMaterialDaoFactory create(
      Provider<ConstructionDatabase> databaseProvider) {
    return new DatabaseModule_ProvideMaterialDaoFactory(databaseProvider);
  }

  public static MaterialDao provideMaterialDao(ConstructionDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideMaterialDao(database));
  }
}
