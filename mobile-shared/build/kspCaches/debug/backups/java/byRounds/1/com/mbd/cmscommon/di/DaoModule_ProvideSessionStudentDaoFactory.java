package com.mbd.cmscommon.di;

import com.mbd.cmscommon.data.local.CmsDatabase;
import com.mbd.cmscommon.data.local.dao.SessionStudentDao;
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
public final class DaoModule_ProvideSessionStudentDaoFactory implements Factory<SessionStudentDao> {
  private final Provider<CmsDatabase> dbProvider;

  public DaoModule_ProvideSessionStudentDaoFactory(Provider<CmsDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public SessionStudentDao get() {
    return provideSessionStudentDao(dbProvider.get());
  }

  public static DaoModule_ProvideSessionStudentDaoFactory create(Provider<CmsDatabase> dbProvider) {
    return new DaoModule_ProvideSessionStudentDaoFactory(dbProvider);
  }

  public static SessionStudentDao provideSessionStudentDao(CmsDatabase db) {
    return Preconditions.checkNotNullFromProvides(DaoModule.INSTANCE.provideSessionStudentDao(db));
  }
}
