package com.mbd.cmscommon.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.jan.supabase.postgrest.Postgrest;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class MarkEditRequestRepositoryImpl_Factory implements Factory<MarkEditRequestRepositoryImpl> {
  private final Provider<Postgrest> postgrestProvider;

  public MarkEditRequestRepositoryImpl_Factory(Provider<Postgrest> postgrestProvider) {
    this.postgrestProvider = postgrestProvider;
  }

  @Override
  public MarkEditRequestRepositoryImpl get() {
    return newInstance(postgrestProvider.get());
  }

  public static MarkEditRequestRepositoryImpl_Factory create(
      Provider<Postgrest> postgrestProvider) {
    return new MarkEditRequestRepositoryImpl_Factory(postgrestProvider);
  }

  public static MarkEditRequestRepositoryImpl newInstance(Postgrest postgrest) {
    return new MarkEditRequestRepositoryImpl(postgrest);
  }
}
