package com.constructionmanager.data.repository;

import com.constructionmanager.data.database.dao.ProjectDao;
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
public final class ProjectRepositoryImpl_Factory implements Factory<ProjectRepositoryImpl> {
  private final Provider<ProjectDao> projectDaoProvider;

  public ProjectRepositoryImpl_Factory(Provider<ProjectDao> projectDaoProvider) {
    this.projectDaoProvider = projectDaoProvider;
  }

  @Override
  public ProjectRepositoryImpl get() {
    return newInstance(projectDaoProvider.get());
  }

  public static ProjectRepositoryImpl_Factory create(Provider<ProjectDao> projectDaoProvider) {
    return new ProjectRepositoryImpl_Factory(projectDaoProvider);
  }

  public static ProjectRepositoryImpl newInstance(ProjectDao projectDao) {
    return new ProjectRepositoryImpl(projectDao);
  }
}
