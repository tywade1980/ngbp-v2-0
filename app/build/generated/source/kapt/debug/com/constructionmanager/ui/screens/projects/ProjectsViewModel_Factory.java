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
public final class ProjectsViewModel_Factory implements Factory<ProjectsViewModel> {
  private final Provider<ProjectRepository> projectRepositoryProvider;

  public ProjectsViewModel_Factory(Provider<ProjectRepository> projectRepositoryProvider) {
    this.projectRepositoryProvider = projectRepositoryProvider;
  }

  @Override
  public ProjectsViewModel get() {
    return newInstance(projectRepositoryProvider.get());
  }

  public static ProjectsViewModel_Factory create(
      Provider<ProjectRepository> projectRepositoryProvider) {
    return new ProjectsViewModel_Factory(projectRepositoryProvider);
  }

  public static ProjectsViewModel newInstance(ProjectRepository projectRepository) {
    return new ProjectsViewModel(projectRepository);
  }
}
