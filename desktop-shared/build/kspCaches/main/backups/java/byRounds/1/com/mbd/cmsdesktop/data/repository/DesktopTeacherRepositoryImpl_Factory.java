package com.mbd.cmsdesktop.data.repository;

import com.mbd.cmscommon.auth.AdminUserProvisioner;
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
public final class DesktopTeacherRepositoryImpl_Factory implements Factory<DesktopTeacherRepositoryImpl> {
  private final Provider<Postgrest> postgrestProvider;

  private final Provider<AdminUserProvisioner> provisionerProvider;

  private final Provider<DesktopBootstrapSnapshotStore> snapshotStoreProvider;

  public DesktopTeacherRepositoryImpl_Factory(Provider<Postgrest> postgrestProvider,
      Provider<AdminUserProvisioner> provisionerProvider,
      Provider<DesktopBootstrapSnapshotStore> snapshotStoreProvider) {
    this.postgrestProvider = postgrestProvider;
    this.provisionerProvider = provisionerProvider;
    this.snapshotStoreProvider = snapshotStoreProvider;
  }

  @Override
  public DesktopTeacherRepositoryImpl get() {
    return newInstance(postgrestProvider.get(), provisionerProvider.get(), snapshotStoreProvider.get());
  }

  public static DesktopTeacherRepositoryImpl_Factory create(Provider<Postgrest> postgrestProvider,
      Provider<AdminUserProvisioner> provisionerProvider,
      Provider<DesktopBootstrapSnapshotStore> snapshotStoreProvider) {
    return new DesktopTeacherRepositoryImpl_Factory(postgrestProvider, provisionerProvider, snapshotStoreProvider);
  }

  public static DesktopTeacherRepositoryImpl newInstance(Postgrest postgrest,
      AdminUserProvisioner provisioner, DesktopBootstrapSnapshotStore snapshotStore) {
    return new DesktopTeacherRepositoryImpl(postgrest, provisioner, snapshotStore);
  }
}
