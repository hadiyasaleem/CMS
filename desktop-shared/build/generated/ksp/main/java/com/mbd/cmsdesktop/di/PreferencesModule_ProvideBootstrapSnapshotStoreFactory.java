package com.mbd.cmsdesktop.di;

import com.mbd.cmsdesktop.data.cache.DesktopBootstrapSnapshotStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class PreferencesModule_ProvideBootstrapSnapshotStoreFactory implements Factory<DesktopBootstrapSnapshotStore> {
  @Override
  public DesktopBootstrapSnapshotStore get() {
    return provideBootstrapSnapshotStore();
  }

  public static PreferencesModule_ProvideBootstrapSnapshotStoreFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static DesktopBootstrapSnapshotStore provideBootstrapSnapshotStore() {
    return Preconditions.checkNotNullFromProvides(PreferencesModule.INSTANCE.provideBootstrapSnapshotStore());
  }

  private static final class InstanceHolder {
    private static final PreferencesModule_ProvideBootstrapSnapshotStoreFactory INSTANCE = new PreferencesModule_ProvideBootstrapSnapshotStoreFactory();
  }
}
