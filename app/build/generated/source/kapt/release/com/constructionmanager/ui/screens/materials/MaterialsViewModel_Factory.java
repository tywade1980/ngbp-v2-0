package com.constructionmanager.ui.screens.materials;

import com.constructionmanager.domain.repository.MaterialRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class MaterialsViewModel_Factory implements Factory<MaterialsViewModel> {
  private final Provider<MaterialRepository> materialRepositoryProvider;

  public MaterialsViewModel_Factory(Provider<MaterialRepository> materialRepositoryProvider) {
    this.materialRepositoryProvider = materialRepositoryProvider;
  }

  @Override
  public MaterialsViewModel get() {
    return newInstance(materialRepositoryProvider.get());
  }

  public static MaterialsViewModel_Factory create(
      Provider<MaterialRepository> materialRepositoryProvider) {
    return new MaterialsViewModel_Factory(materialRepositoryProvider);
  }

  public static MaterialsViewModel newInstance(MaterialRepository materialRepository) {
    return new MaterialsViewModel(materialRepository);
  }
}
