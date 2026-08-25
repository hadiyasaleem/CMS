package com.mbd.cmscommon.di;

import com.mbd.cmscommon.data.local.CmsDatabase;
import com.mbd.cmscommon.data.local.dao.SyncStateDao;
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
public final class DaoModule_ProvideSyncStateDaoFactory implements Factory<SyncStateDao> {
  private final Provider<CmsDatabase> dbProvider;

  public DaoModule_ProvideSyncStateDaoFactory(Provider<CmsDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public SyncStateDao get() {
    return provideSyncStateDao(dbProvider.get());
  }

  public static DaoModule_ProvideSyncStateDaoFactory create(Provider<CmsDatabase> dbProvider) {
    return new DaoModule_ProvideSyncStateDaoFactory(dbProvider);
  }

  public static SyncStateDao provideSyncStateDao(CmsDatabase db) {
    return Preconditions.checkNotNullFromProvides(DaoModule.INSTANCE.provideSyncStateDao(db));
  }
}
