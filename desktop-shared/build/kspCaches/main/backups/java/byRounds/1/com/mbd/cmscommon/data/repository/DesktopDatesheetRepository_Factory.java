package com.mbd.cmscommon.data.repository;

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
public final class DesktopDatesheetRepository_Factory implements Factory<DesktopDatesheetRepository> {
  private final Provider<Postgrest> postgrestProvider;

  public DesktopDatesheetRepository_Factory(Provider<Postgrest> postgrestProvider) {
    this.postgrestProvider = postgrestProvider;
  }

  @Override
  public DesktopDatesheetRepository get() {
    return newInstance(postgrestProvider.get());
  }

  public static DesktopDatesheetRepository_Factory create(Provider<Postgrest> postgrestProvider) {
    return new DesktopDatesheetRepository_Factory(postgrestProvider);
  }

  public static DesktopDatesheetRepository newInstance(Postgrest postgrest) {
    return new DesktopDatesheetRepository(postgrest);
  }
}
