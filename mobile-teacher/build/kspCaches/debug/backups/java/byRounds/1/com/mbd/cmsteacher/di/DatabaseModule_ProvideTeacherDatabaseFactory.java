package com.mbd.cmsteacher.di;

import android.content.Context;
import com.mbd.cmscommon.data.local.CmsDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class DatabaseModule_ProvideTeacherDatabaseFactory implements Factory<CmsDatabase> {
  private final Provider<Context> contextProvider;

  public DatabaseModule_ProvideTeacherDatabaseFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public CmsDatabase get() {
    return provideTeacherDatabase(contextProvider.get());
  }

  public static DatabaseModule_ProvideTeacherDatabaseFactory create(
      Provider<Context> contextProvider) {
    return new DatabaseModule_ProvideTeacherDatabaseFactory(contextProvider);
  }

  public static CmsDatabase provideTeacherDatabase(Context context) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideTeacherDatabase(context));
  }
}
