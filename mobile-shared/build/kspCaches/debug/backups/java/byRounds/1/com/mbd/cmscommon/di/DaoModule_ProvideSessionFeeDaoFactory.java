package com.mbd.cmscommon.di;

import com.mbd.cmscommon.data.local.CmsDatabase;
import com.mbd.cmscommon.data.local.dao.SessionFeeDao;
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
public final class DaoModule_ProvideSessionFeeDaoFactory implements Factory<SessionFeeDao> {
  private final Provider<CmsDatabase> dbProvider;

  public DaoModule_ProvideSessionFeeDaoFactory(Provider<CmsDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public SessionFeeDao get() {
    return provideSessionFeeDao(dbProvider.get());
  }

  public static DaoModule_ProvideSessionFeeDaoFactory create(Provider<CmsDatabase> dbProvider) {
    return new DaoModule_ProvideSessionFeeDaoFactory(dbProvider);
  }

  public static SessionFeeDao provideSessionFeeDao(CmsDatabase db) {
    return Preconditions.checkNotNullFromProvides(DaoModule.INSTANCE.provideSessionFeeDao(db));
  }
}
