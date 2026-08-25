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
public final class DesktopSessionTimetableRepositoryImpl_Factory implements Factory<DesktopSessionTimetableRepositoryImpl> {
  private final Provider<Postgrest> postgrestProvider;

  public DesktopSessionTimetableRepositoryImpl_Factory(Provider<Postgrest> postgrestProvider) {
    this.postgrestProvider = postgrestProvider;
  }

  @Override
  public DesktopSessionTimetableRepositoryImpl get() {
    return newInstance(postgrestProvider.get());
  }

  public static DesktopSessionTimetableRepositoryImpl_Factory create(
      Provider<Postgrest> postgrestProvider) {
    return new DesktopSessionTimetableRepositoryImpl_Factory(postgrestProvider);
  }

  public static DesktopSessionTimetableRepositoryImpl newInstance(Postgrest postgrest) {
    return new DesktopSessionTimetableRepositoryImpl(postgrest);
  }
}
