package com.mbd.cmsdesktop.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.jan.supabase.SupabaseClient;
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
public final class SupabaseModule_ProvidePostgrestFactory implements Factory<Postgrest> {
  private final Provider<SupabaseClient> clientProvider;

  public SupabaseModule_ProvidePostgrestFactory(Provider<SupabaseClient> clientProvider) {
    this.clientProvider = clientProvider;
  }

  @Override
  public Postgrest get() {
    return providePostgrest(clientProvider.get());
  }

  public static SupabaseModule_ProvidePostgrestFactory create(
      Provider<SupabaseClient> clientProvider) {
    return new SupabaseModule_ProvidePostgrestFactory(clientProvider);
  }

  public static Postgrest providePostgrest(SupabaseClient client) {
    return Preconditions.checkNotNullFromProvides(SupabaseModule.INSTANCE.providePostgrest(client));
  }
}
