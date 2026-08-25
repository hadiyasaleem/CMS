package com.mbd.cmscommon.auth;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.jan.supabase.functions.Functions;
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
public final class AdminUserProvisioner_Factory implements Factory<AdminUserProvisioner> {
  private final Provider<Functions> functionsProvider;

  public AdminUserProvisioner_Factory(Provider<Functions> functionsProvider) {
    this.functionsProvider = functionsProvider;
  }

  @Override
  public AdminUserProvisioner get() {
    return newInstance(functionsProvider.get());
  }

  public static AdminUserProvisioner_Factory create(Provider<Functions> functionsProvider) {
    return new AdminUserProvisioner_Factory(functionsProvider);
  }

  public static AdminUserProvisioner newInstance(Functions functions) {
    return new AdminUserProvisioner(functions);
  }
}
