package com.mbd.cmscommon.di;

import com.mbd.cmscommon.data.local.CmsDatabase;
import com.mbd.cmscommon.data.local.dao.SemesterSubjectDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
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
public final class DaoModule_ProvideSemesterSubjectDaoFactory implements Factory<SemesterSubjectDao> {
  private final Provider<CmsDatabase> dbProvider;

  public DaoModule_ProvideSemesterSubjectDaoFactory(Provider<CmsDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public SemesterSubjectDao get() {
    return provideSemesterSubjectDao(dbProvider.get());
  }

  public static DaoModule_ProvideSemesterSubjectDaoFactory create(
      Provider<CmsDatabase> dbProvider) {
    return new DaoModule_ProvideSemesterSubjectDaoFactory(dbProvider);
  }

  public static SemesterSubjectDao provideSemesterSubjectDao(CmsDatabase db) {
    return Preconditions.checkNotNullFromProvides(DaoModule.INSTANCE.provideSemesterSubjectDao(db));
  }
}
