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
public final class DatesheetRepositoryImpl_Factory implements Factory<DatesheetRepositoryImpl> {
  private final Provider<Postgrest> postgrestProvider;

  public DatesheetRepositoryImpl_Factory(Provider<Postgrest> postgrestProvider) {
    this.postgrestProvider = postgrestProvider;
  }

  @Override
  public DatesheetRepositoryImpl get() {
    return newInstance(postgrestProvider.get());
  }

  public static DatesheetRepositoryImpl_Factory create(Provider<Postgrest> postgrestProvider) {
    return new DatesheetRepositoryImpl_Factory(postgrestProvider);
  }

  public static DatesheetRepositoryImpl newInstance(Postgrest postgrest) {
    return new DatesheetRepositoryImpl(postgrest);
  }
}
