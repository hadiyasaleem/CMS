package com.mbd.cmsdesktop.data.repository;

import androidx.datastore.core.DataStore;
import androidx.datastore.preferences.core.Preferences;
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
public final class DesktopNotificationRepositoryImpl_Factory implements Factory<DesktopNotificationRepositoryImpl> {
  private final Provider<Postgrest> postgrestProvider;

  private final Provider<DataStore<Preferences>> dataStoreProvider;

  public DesktopNotificationRepositoryImpl_Factory(Provider<Postgrest> postgrestProvider,
      Provider<DataStore<Preferences>> dataStoreProvider) {
    this.postgrestProvider = postgrestProvider;
    this.dataStoreProvider = dataStoreProvider;
  }

  @Override
  public DesktopNotificationRepositoryImpl get() {
    return newInstance(postgrestProvider.get(), dataStoreProvider.get());
  }

  public static DesktopNotificationRepositoryImpl_Factory create(
      Provider<Postgrest> postgrestProvider, Provider<DataStore<Preferences>> dataStoreProvider) {
    return new DesktopNotificationRepositoryImpl_Factory(postgrestProvider, dataStoreProvider);
  }

  public static DesktopNotificationRepositoryImpl newInstance(Postgrest postgrest,
      DataStore<Preferences> dataStore) {
    return new DesktopNotificationRepositoryImpl(postgrest, dataStore);
  }
}
