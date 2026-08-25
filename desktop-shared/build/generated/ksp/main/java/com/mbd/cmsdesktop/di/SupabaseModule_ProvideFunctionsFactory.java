package com.mbd.cmsdesktop.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.jan.supabase.SupabaseClient;
import io.github.jan.supabase.functions.Functions;
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
public final class SupabaseModule_ProvideFunctionsFactory implements Factory<Functions> {
  private final Provider<SupabaseClient> clientProvider;

  public SupabaseModule_ProvideFunctionsFactory(Provider<SupabaseClient> clientProvider) {
    this.clientProvider = clientProvider;
  }

  @Override
  public Functions get() {
    return provideFunctions(clientProvider.get());
  }

  public static SupabaseModule_ProvideFunctionsFactory create(
      Provider<SupabaseClient> clientProvider) {
    return new SupabaseModule_ProvideFunctionsFactory(clientProvider);
  }

  public static Functions provideFunctions(SupabaseClient client) {
    return Preconditions.checkNotNullFromProvides(SupabaseModule.INSTANCE.provideFunctions(client));
  }
}
