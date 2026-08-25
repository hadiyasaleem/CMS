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
public final class CalendarRepositoryImpl_Factory implements Factory<CalendarRepositoryImpl> {
  private final Provider<Postgrest> postgrestProvider;

  public CalendarRepositoryImpl_Factory(Provider<Postgrest> postgrestProvider) {
    this.postgrestProvider = postgrestProvider;
  }

  @Override
  public CalendarRepositoryImpl get() {
    return newInstance(postgrestProvider.get());
  }

  public static CalendarRepositoryImpl_Factory create(Provider<Postgrest> postgrestProvider) {
    return new CalendarRepositoryImpl_Factory(postgrestProvider);
  }

  public static CalendarRepositoryImpl newInstance(Postgrest postgrest) {
    return new CalendarRepositoryImpl(postgrest);
  }
}
