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
public final class DesktopSessionAttendanceRepositoryImpl_Factory implements Factory<DesktopSessionAttendanceRepositoryImpl> {
  private final Provider<Postgrest> postgrestProvider;

  public DesktopSessionAttendanceRepositoryImpl_Factory(Provider<Postgrest> postgrestProvider) {
    this.postgrestProvider = postgrestProvider;
  }

  @Override
  public DesktopSessionAttendanceRepositoryImpl get() {
    return newInstance(postgrestProvider.get());
  }

  public static DesktopSessionAttendanceRepositoryImpl_Factory create(
      Provider<Postgrest> postgrestProvider) {
    return new DesktopSessionAttendanceRepositoryImpl_Factory(postgrestProvider);
  }

  public static DesktopSessionAttendanceRepositoryImpl newInstance(Postgrest postgrest) {
    return new DesktopSessionAttendanceRepositoryImpl(postgrest);
  }
}
