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
public final class FineRepositoryImpl_Factory implements Factory<FineRepositoryImpl> {
  private final Provider<Postgrest> postgrestProvider;

  public FineRepositoryImpl_Factory(Provider<Postgrest> postgrestProvider) {
    this.postgrestProvider = postgrestProvider;
  }

  @Override
  public FineRepositoryImpl get() {
    return newInstance(postgrestProvider.get());
  }

  public static FineRepositoryImpl_Factory create(Provider<Postgrest> postgrestProvider) {
    return new FineRepositoryImpl_Factory(postgrestProvider);
  }

  public static FineRepositoryImpl newInstance(Postgrest postgrest) {
    return new FineRepositoryImpl(postgrest);
  }
}
