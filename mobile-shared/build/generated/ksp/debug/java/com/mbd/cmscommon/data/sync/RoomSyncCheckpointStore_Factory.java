package com.mbd.cmscommon.data.sync;

import com.mbd.cmscommon.data.local.dao.TableSyncStateDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
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
public final class RoomSyncCheckpointStore_Factory implements Factory<RoomSyncCheckpointStore> {
  private final Provider<TableSyncStateDao> daoProvider;

  public RoomSyncCheckpointStore_Factory(Provider<TableSyncStateDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public RoomSyncCheckpointStore get() {
    return newInstance(daoProvider.get());
  }

  public static RoomSyncCheckpointStore_Factory create(Provider<TableSyncStateDao> daoProvider) {
    return new RoomSyncCheckpointStore_Factory(daoProvider);
  }

  public static RoomSyncCheckpointStore newInstance(TableSyncStateDao dao) {
    return new RoomSyncCheckpointStore(dao);
  }
}
