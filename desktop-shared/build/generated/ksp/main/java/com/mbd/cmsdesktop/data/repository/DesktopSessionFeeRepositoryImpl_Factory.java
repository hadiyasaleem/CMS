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
public final class DesktopSessionFeeRepositoryImpl_Factory implements Factory<DesktopSessionFeeRepositoryImpl> {
  private final Provider<Postgrest> postgrestProvider;

  public DesktopSessionFeeRepositoryImpl_Factory(Provider<Postgrest> postgrestProvider) {
    this.postgrestProvider = postgrestProvider;
  }

  @Override
  public DesktopSessionFeeRepositoryImpl get() {
    return newInstance(postgrestProvider.get());
  }

  public static DesktopSessionFeeRepositoryImpl_Factory create(
      Provider<Postgrest> postgrestProvider) {
    return new DesktopSessionFeeRepositoryImpl_Factory(postgrestProvider);
  }

  public static DesktopSessionFeeRepositoryImpl newInstance(Postgrest postgrest) {
    return new DesktopSessionFeeRepositoryImpl(postgrest);
  }
}
