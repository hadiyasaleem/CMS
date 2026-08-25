package com.mbd.cmsdesktop.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.jan.supabase.SupabaseClient;
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
public final class SupabaseModule_ProvideStorageFactory implements Factory<Storage> {
  private final Provider<SupabaseClient> clientProvider;

  public SupabaseModule_ProvideStorageFactory(Provider<SupabaseClient> clientProvider) {
    this.clientProvider = clientProvider;
  }

  @Override
  public Storage get() {
    return provideStorage(clientProvider.get());
  }

  public static SupabaseModule_ProvideStorageFactory create(
      Provider<SupabaseClient> clientProvider) {
    return new SupabaseModule_ProvideStorageFactory(clientProvider);
  }

  public static Storage provideStorage(SupabaseClient client) {
    return Preconditions.checkNotNullFromProvides(SupabaseModule.INSTANCE.provideStorage(client));
  }
}
