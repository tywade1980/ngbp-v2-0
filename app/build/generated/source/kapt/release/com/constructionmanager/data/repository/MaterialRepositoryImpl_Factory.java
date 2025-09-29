package com.constructionmanager.data.repository;

import com.constructionmanager.data.database.dao.MaterialDao;
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
public final class MaterialRepositoryImpl_Factory implements Factory<MaterialRepositoryImpl> {
  private final Provider<MaterialDao> materialDaoProvider;

  public MaterialRepositoryImpl_Factory(Provider<MaterialDao> materialDaoProvider) {
    this.materialDaoProvider = materialDaoProvider;
  }

  @Override
  public MaterialRepositoryImpl get() {
    return newInstance(materialDaoProvider.get());
  }

  public static MaterialRepositoryImpl_Factory create(Provider<MaterialDao> materialDaoProvider) {
    return new MaterialRepositoryImpl_Factory(materialDaoProvider);
  }

  public static MaterialRepositoryImpl newInstance(MaterialDao materialDao) {
    return new MaterialRepositoryImpl(materialDao);
  }
}
