package com.mbd.cmsdesktop.data.repository;

import com.mbd.cmsdesktop.auth.DesktopRoleResolver;
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
public final class DesktopUserRepositoryImpl_Factory implements Factory<DesktopUserRepositoryImpl> {
  private final Provider<Postgrest> postgrestProvider;

  private final Provider<DesktopRoleResolver> roleResolverProvider;

  private final Provider<DesktopBootstrapSnapshotStore> snapshotStoreProvider;

  public DesktopUserRepositoryImpl_Factory(Provider<Postgrest> postgrestProvider,
      Provider<DesktopRoleResolver> roleResolverProvider,
      Provider<DesktopBootstrapSnapshotStore> snapshotStoreProvider) {
    this.postgrestProvider = postgrestProvider;
    this.roleResolverProvider = roleResolverProvider;
    this.snapshotStoreProvider = snapshotStoreProvider;
  }

  @Override
  public DesktopUserRepositoryImpl get() {
    return newInstance(postgrestProvider.get(), roleResolverProvider.get(), snapshotStoreProvider.get());
  }

  public static DesktopUserRepositoryImpl_Factory create(Provider<Postgrest> postgrestProvider,
      Provider<DesktopRoleResolver> roleResolverProvider,
      Provider<DesktopBootstrapSnapshotStore> snapshotStoreProvider) {
    return new DesktopUserRepositoryImpl_Factory(postgrestProvider, roleResolverProvider, snapshotStoreProvider);
  }

  public static DesktopUserRepositoryImpl newInstance(Postgrest postgrest,
      DesktopRoleResolver roleResolver, DesktopBootstrapSnapshotStore snapshotStore) {
    return new DesktopUserRepositoryImpl(postgrest, roleResolver, snapshotStore);
  }
}
