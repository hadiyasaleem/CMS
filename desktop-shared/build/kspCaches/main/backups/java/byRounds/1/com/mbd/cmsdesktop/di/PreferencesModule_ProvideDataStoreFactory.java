package com.mbd.cmsdesktop.di;

import androidx.datastore.core.DataStore;
import androidx.datastore.preferences.core.Preferences;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class PreferencesModule_ProvideDataStoreFactory implements Factory<DataStore<Preferences>> {
  @Override
  public DataStore<Preferences> get() {
    return provideDataStore();
  }

  public static PreferencesModule_ProvideDataStoreFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static DataStore<Preferences> provideDataStore() {
    return Preconditions.checkNotNullFromProvides(PreferencesModule.INSTANCE.provideDataStore());
  }

  private static final class InstanceHolder {
    private static final PreferencesModule_ProvideDataStoreFactory INSTANCE = new PreferencesModule_ProvideDataStoreFactory();
  }
}
