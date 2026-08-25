package com.mbd.cmsdesktop.data.repository;

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
public final class DesktopSessionMarksRepositoryImpl_Factory implements Factory<DesktopSessionMarksRepositoryImpl> {
  private final Provider<Postgrest> postgrestProvider;

  public DesktopSessionMarksRepositoryImpl_Factory(Provider<Postgrest> postgrestProvider) {
    this.postgrestProvider = postgrestProvider;
  }

  @Override
  public DesktopSessionMarksRepositoryImpl get() {
    return newInstance(postgrestProvider.get());
  }

  public static DesktopSessionMarksRepositoryImpl_Factory create(
      Provider<Postgrest> postgrestProvider) {
    return new DesktopSessionMarksRepositoryImpl_Factory(postgrestProvider);
  }

  public static DesktopSessionMarksRepositoryImpl newInstance(Postgrest postgrest) {
    return new DesktopSessionMarksRepositoryImpl(postgrest);
  }
}
