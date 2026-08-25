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
public final class DesktopStudentLinkRequestRepositoryImpl_Factory implements Factory<DesktopStudentLinkRequestRepositoryImpl> {
  private final Provider<Postgrest> postgrestProvider;

  public DesktopStudentLinkRequestRepositoryImpl_Factory(Provider<Postgrest> postgrestProvider) {
    this.postgrestProvider = postgrestProvider;
  }

  @Override
  public DesktopStudentLinkRequestRepositoryImpl get() {
    return newInstance(postgrestProvider.get());
  }

  public static DesktopStudentLinkRequestRepositoryImpl_Factory create(
      Provider<Postgrest> postgrestProvider) {
    return new DesktopStudentLinkRequestRepositoryImpl_Factory(postgrestProvider);
  }

  public static DesktopStudentLinkRequestRepositoryImpl newInstance(Postgrest postgrest) {
    return new DesktopStudentLinkRequestRepositoryImpl(postgrest);
  }
}
