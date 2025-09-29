package com.constructionmanager.ui.screens.projects;

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
public final class ProjectDetailsViewModel_Factory implements Factory<ProjectDetailsViewModel> {
  private final Provider<ProjectRepository> projectRepositoryProvider;

  public ProjectDetailsViewModel_Factory(Provider<ProjectRepository> projectRepositoryProvider) {
    this.projectRepositoryProvider = projectRepositoryProvider;
  }

  @Override
  public ProjectDetailsViewModel get() {
    return newInstance(projectRepositoryProvider.get());
  }

  public static ProjectDetailsViewModel_Factory create(
      Provider<ProjectRepository> projectRepositoryProvider) {
    return new ProjectDetailsViewModel_Factory(projectRepositoryProvider);
  }

  public static ProjectDetailsViewModel newInstance(ProjectRepository projectRepository) {
    return new ProjectDetailsViewModel(projectRepository);
  }
}
