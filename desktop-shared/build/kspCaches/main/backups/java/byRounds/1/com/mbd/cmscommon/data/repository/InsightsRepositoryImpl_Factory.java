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
public final class InsightsRepositoryImpl_Factory implements Factory<InsightsRepositoryImpl> {
  private final Provider<Postgrest> postgrestProvider;

  public InsightsRepositoryImpl_Factory(Provider<Postgrest> postgrestProvider) {
    this.postgrestProvider = postgrestProvider;
  }

  @Override
  public InsightsRepositoryImpl get() {
    return newInstance(postgrestProvider.get());
  }

  public static InsightsRepositoryImpl_Factory create(Provider<Postgrest> postgrestProvider) {
    return new InsightsRepositoryImpl_Factory(postgrestProvider);
  }

  public static InsightsRepositoryImpl newInstance(Postgrest postgrest) {
    return new InsightsRepositoryImpl(postgrest);
  }
}
