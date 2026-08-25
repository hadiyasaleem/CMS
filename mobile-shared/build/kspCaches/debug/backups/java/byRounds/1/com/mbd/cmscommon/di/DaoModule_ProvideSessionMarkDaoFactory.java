package com.mbd.cmscommon.di;

import com.mbd.cmscommon.data.local.CmsDatabase;
import com.mbd.cmscommon.data.local.dao.SessionMarkDao;
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
public final class DaoModule_ProvideSessionMarkDaoFactory implements Factory<SessionMarkDao> {
  private final Provider<CmsDatabase> dbProvider;

  public DaoModule_ProvideSessionMarkDaoFactory(Provider<CmsDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public SessionMarkDao get() {
    return provideSessionMarkDao(dbProvider.get());
  }

  public static DaoModule_ProvideSessionMarkDaoFactory create(Provider<CmsDatabase> dbProvider) {
    return new DaoModule_ProvideSessionMarkDaoFactory(dbProvider);
  }

  public static SessionMarkDao provideSessionMarkDao(CmsDatabase db) {
    return Preconditions.checkNotNullFromProvides(DaoModule.INSTANCE.provideSessionMarkDao(db));
  }
}
