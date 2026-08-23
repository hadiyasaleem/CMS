package com.mbd.cmscommon.data.repository;

import com.mbd.cmscommon.auth.SessionManager;
import com.mbd.cmscommon.data.local.dao.SessionFeeDao;
import com.mbd.cmscommon.data.sync.SyncCheckpointStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.jan.supabase.postgrest.Postgrest;
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
public final class SessionFeeRepositoryImpl_Factory implements Factory<SessionFeeRepositoryImpl> {
  private final Provider<Postgrest> postgrestProvider;

  private final Provider<SessionFeeDao> feeDaoProvider;

  private final Provider<SyncCheckpointStore> checkpointStoreProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  public SessionFeeRepositoryImpl_Factory(Provider<Postgrest> postgrestProvider,
      Provider<SessionFeeDao> feeDaoProvider, Provider<SyncCheckpointStore> checkpointStoreProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.postgrestProvider = postgrestProvider;
    this.feeDaoProvider = feeDaoProvider;
    this.checkpointStoreProvider = checkpointStoreProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public SessionFeeRepositoryImpl get() {
    return newInstance(postgrestProvider.get(), feeDaoProvider.get(), checkpointStoreProvider.get(), sessionManagerProvider.get());
  }

  public static SessionFeeRepositoryImpl_Factory create(Provider<Postgrest> postgrestProvider,
      Provider<SessionFeeDao> feeDaoProvider, Provider<SyncCheckpointStore> checkpointStoreProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new SessionFeeRepositoryImpl_Factory(postgrestProvider, feeDaoProvider, checkpointStoreProvider, sessionManagerProvider);
  }

  public static SessionFeeRepositoryImpl newInstance(Postgrest postgrest, SessionFeeDao feeDao,
      SyncCheckpointStore checkpointStore, SessionManager sessionManager) {
    return new SessionFeeRepositoryImpl(postgrest, feeDao, checkpointStore, sessionManager);
  }
}
