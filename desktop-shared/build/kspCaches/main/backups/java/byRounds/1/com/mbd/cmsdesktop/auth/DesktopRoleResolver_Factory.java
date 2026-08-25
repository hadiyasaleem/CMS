package com.mbd.cmsdesktop.auth;

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
public final class DesktopRoleResolver_Factory implements Factory<DesktopRoleResolver> {
  private final Provider<Postgrest> postgrestProvider;

  public DesktopRoleResolver_Factory(Provider<Postgrest> postgrestProvider) {
    this.postgrestProvider = postgrestProvider;
  }

  @Override
  public DesktopRoleResolver get() {
    return newInstance(postgrestProvider.get());
  }

  public static DesktopRoleResolver_Factory create(Provider<Postgrest> postgrestProvider) {
    return new DesktopRoleResolver_Factory(postgrestProvider);
  }

  public static DesktopRoleResolver newInstance(Postgrest postgrest) {
    return new DesktopRoleResolver(postgrest);
  }
}
