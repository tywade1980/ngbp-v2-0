package com.constructionmanager.ui.screens.labor;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class LaborManagementViewModel_Factory implements Factory<LaborManagementViewModel> {
  @Override
  public LaborManagementViewModel get() {
    return newInstance();
  }

  public static LaborManagementViewModel_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static LaborManagementViewModel newInstance() {
    return new LaborManagementViewModel();
  }

  private static final class InstanceHolder {
    private static final LaborManagementViewModel_Factory INSTANCE = new LaborManagementViewModel_Factory();
  }
}
