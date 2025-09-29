package com.constructionmanager.ui.screens.dashboard;

import com.constructionmanager.domain.repository.MaterialRepository;
import com.constructionmanager.domain.repository.ProjectRepository;
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
public final class DashboardViewModel_Factory implements Factory<DashboardViewModel> {
  private final Provider<ProjectRepository> projectRepositoryProvider;

  private final Provider<MaterialRepository> materialRepositoryProvider;

  public DashboardViewModel_Factory(Provider<ProjectRepository> projectRepositoryProvider,
      Provider<MaterialRepository> materialRepositoryProvider) {
    this.projectRepositoryProvider = projectRepositoryProvider;
    this.materialRepositoryProvider = materialRepositoryProvider;
  }

  @Override
  public DashboardViewModel get() {
    return newInstance(projectRepositoryProvider.get(), materialRepositoryProvider.get());
  }

  public static DashboardViewModel_Factory create(
      Provider<ProjectRepository> projectRepositoryProvider,
      Provider<MaterialRepository> materialRepositoryProvider) {
    return new DashboardViewModel_Factory(projectRepositoryProvider, materialRepositoryProvider);
  }

  public static DashboardViewModel newInstance(ProjectRepository projectRepository,
      MaterialRepository materialRepository) {
    return new DashboardViewModel(projectRepository, materialRepository);
  }
}
