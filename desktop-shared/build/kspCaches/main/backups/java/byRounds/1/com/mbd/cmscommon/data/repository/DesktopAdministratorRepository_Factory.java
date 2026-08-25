package com.mbd.cmscommon.data.repository;

import com.mbd.cmscommon.auth.AdminUserProvisioner;
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
public final class DesktopAdministratorRepository_Factory implements Factory<DesktopAdministratorRepository> {
  private final Provider<Postgrest> postgrestProvider;

  private final Provider<AdminUserProvisioner> provisionerProvider;

  public DesktopAdministratorRepository_Factory(Provider<Postgrest> postgrestProvider,
      Provider<AdminUserProvisioner> provisionerProvider) {
    this.postgrestProvider = postgrestProvider;
    this.provisionerProvider = provisionerProvider;
  }

  @Override
  public DesktopAdministratorRepository get() {
    return newInstance(postgrestProvider.get(), provisionerProvider.get());
  }

  public static DesktopAdministratorRepository_Factory create(Provider<Postgrest> postgrestProvider,
      Provider<AdminUserProvisioner> provisionerProvider) {
    return new DesktopAdministratorRepository_Factory(postgrestProvider, provisionerProvider);
  }

  public static DesktopAdministratorRepository newInstance(Postgrest postgrest,
      AdminUserProvisioner provisioner) {
    return new DesktopAdministratorRepository(postgrest, provisioner);
  }
}
