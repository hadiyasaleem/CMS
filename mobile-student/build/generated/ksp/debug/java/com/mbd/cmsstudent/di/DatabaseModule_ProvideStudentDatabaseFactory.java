package com.mbd.cmsstudent.di;

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
public final class DatabaseModule_ProvideStudentDatabaseFactory implements Factory<CmsDatabase> {
  private final Provider<Context> contextProvider;

  public DatabaseModule_ProvideStudentDatabaseFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public CmsDatabase get() {
    return provideStudentDatabase(contextProvider.get());
  }

  public static DatabaseModule_ProvideStudentDatabaseFactory create(
      Provider<Context> contextProvider) {
    return new DatabaseModule_ProvideStudentDatabaseFactory(contextProvider);
  }

  public static CmsDatabase provideStudentDatabase(Context context) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideStudentDatabase(context));
  }
}
