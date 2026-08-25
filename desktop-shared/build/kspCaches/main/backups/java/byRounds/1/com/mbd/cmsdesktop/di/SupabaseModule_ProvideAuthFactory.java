package com.mbd.cmsdesktop.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.jan.supabase.SupabaseClient;
import io.github.jan.supabase.auth.Auth;
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
public final class SupabaseModule_ProvideAuthFactory implements Factory<Auth> {
  private final Provider<SupabaseClient> clientProvider;

  public SupabaseModule_ProvideAuthFactory(Provider<SupabaseClient> clientProvider) {
    this.clientProvider = clientProvider;
  }

  @Override
  public Auth get() {
    return provideAuth(clientProvider.get());
  }

  public static SupabaseModule_ProvideAuthFactory create(Provider<SupabaseClient> clientProvider) {
    return new SupabaseModule_ProvideAuthFactory(clientProvider);
  }

  public static Auth provideAuth(SupabaseClient client) {
    return Preconditions.checkNotNullFromProvides(SupabaseModule.INSTANCE.provideAuth(client));
  }
}
