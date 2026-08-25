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
public final class DesktopCurriculumRepositoryImpl_Factory implements Factory<DesktopCurriculumRepositoryImpl> {
  private final Provider<Postgrest> postgrestProvider;

  public DesktopCurriculumRepositoryImpl_Factory(Provider<Postgrest> postgrestProvider) {
    this.postgrestProvider = postgrestProvider;
  }

  @Override
  public DesktopCurriculumRepositoryImpl get() {
    return newInstance(postgrestProvider.get());
  }

  public static DesktopCurriculumRepositoryImpl_Factory create(
      Provider<Postgrest> postgrestProvider) {
    return new DesktopCurriculumRepositoryImpl_Factory(postgrestProvider);
  }

  public static DesktopCurriculumRepositoryImpl newInstance(Postgrest postgrest) {
    return new DesktopCurriculumRepositoryImpl(postgrest);
  }
}
