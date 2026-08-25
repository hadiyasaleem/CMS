package com.mbd.cmsdesktop.data.repository;

import com.mbd.cmsdesktop.data.cache.DesktopBootstrapSnapshotStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.jan.supabase.postgrest.Postgrest;
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
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class DesktopAcademicSessionRepositoryImpl_Factory implements Factory<DesktopAcademicSessionRepositoryImpl> {
  private final Provider<Postgrest> postgrestProvider;

  private final Provider<DesktopBootstrapSnapshotStore> snapshotStoreProvider;

  public DesktopAcademicSessionRepositoryImpl_Factory(Provider<Postgrest> postgrestProvider,
      Provider<DesktopBootstrapSnapshotStore> snapshotStoreProvider) {
    this.postgrestProvider = postgrestProvider;
    this.snapshotStoreProvider = snapshotStoreProvider;
  }

  @Override
  public DesktopAcademicSessionRepositoryImpl get() {
    return newInstance(postgrestProvider.get(), snapshotStoreProvider.get());
  }

  public static DesktopAcademicSessionRepositoryImpl_Factory create(
      Provider<Postgrest> postgrestProvider,
      Provider<DesktopBootstrapSnapshotStore> snapshotStoreProvider) {
    return new DesktopAcademicSessionRepositoryImpl_Factory(postgrestProvider, snapshotStoreProvider);
  }

  public static DesktopAcademicSessionRepositoryImpl newInstance(Postgrest postgrest,
      DesktopBootstrapSnapshotStore snapshotStore) {
    return new DesktopAcademicSessionRepositoryImpl(postgrest, snapshotStore);
  }
}
