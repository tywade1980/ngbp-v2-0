package com.constructionmanager;

import android.app.Application;
import androidx.annotation.CallSuper;
import dagger.hilt.android.internal.managers.ApplicationComponentManager;
import dagger.hilt.android.internal.managers.ComponentSupplier;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.internal.GeneratedComponentManagerHolder;
import dagger.hilt.internal.UnsafeCasts;
import java.lang.Object;
import java.lang.Override;
import javax.annotation.processing.Generated;

/**
 * A generated base class to be extended by the @dagger.hilt.android.HiltAndroidApp annotated class. If using the Gradle plugin, this is swapped as the base class via bytecode transformation.
 */
@Generated("dagger.hilt.android.processor.internal.androidentrypoint.ApplicationGenerator")
public abstract class Hilt_ConstructionManagerApplication extends Application implements GeneratedComponentManagerHolder {
  private boolean injected = false;

  private final ApplicationComponentManager componentManager = new ApplicationComponentManager(new ComponentSupplier() {
    @Override
    public Object get() {
      return DaggerConstructionManagerApplication_HiltComponents_SingletonC.builder()
          .applicationContextModule(new ApplicationContextModule(Hilt_ConstructionManagerApplication.this))
          .build();
    }
  });

  @Override
  public final ApplicationComponentManager componentManager() {
    return componentManager;
  }

  @Override
  public final Object generatedComponent() {
    return this.componentManager().generatedComponent();
  }

  @CallSuper
  @Override
  public void onCreate() {
    hiltInternalInject();
    super.onCreate();
  }

  protected void hiltInternalInject() {
    if (!injected) {
      injected = true;
      // This is a known unsafe cast, but is safe in the only correct use case:
      // ConstructionManagerApplication extends Hilt_ConstructionManagerApplication
      ((ConstructionManagerApplication_GeneratedInjector) generatedComponent()).injectConstructionManagerApplication(UnsafeCasts.<ConstructionManagerApplication>unsafeCast(this));
    }
  }
}
