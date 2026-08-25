package com.mbd.cmsdesktop.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.jan.supabase.postgrest.Postgrest;
import io.github.jan.supabase.storage.Storage;
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
public final class DesktopDocumentRepository_Factory implements Factory<DesktopDocumentRepository> {
  private final Provider<Postgrest> postgrestProvider;

  private final Provider<Storage> storageProvider;

  public DesktopDocumentRepository_Factory(Provider<Postgrest> postgrestProvider,
      Provider<Storage> storageProvider) {
    this.postgrestProvider = postgrestProvider;
    this.storageProvider = storageProvider;
  }

  @Override
  public DesktopDocumentRepository get() {
    return newInstance(postgrestProvider.get(), storageProvider.get());
  }

  public static DesktopDocumentRepository_Factory create(Provider<Postgrest> postgrestProvider,
      Provider<Storage> storageProvider) {
    return new DesktopDocumentRepository_Factory(postgrestProvider, storageProvider);
  }

  public static DesktopDocumentRepository newInstance(Postgrest postgrest, Storage storage) {
    return new DesktopDocumentRepository(postgrest, storage);
  }
}
