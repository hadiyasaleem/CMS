package com.mbd.cmscommon.data.repository;

import com.mbd.cmscommon.auth.SessionManager;
import com.mbd.cmscommon.data.local.dao.SessionStudentDao;
import com.mbd.cmscommon.data.local.dao.StudentLinkRequestDao;
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
public final class StudentLinkRequestRepositoryImpl_Factory implements Factory<StudentLinkRequestRepositoryImpl> {
  private final Provider<Postgrest> postgrestProvider;

  private final Provider<StudentLinkRequestDao> requestDaoProvider;

  private final Provider<SessionStudentDao> sessionStudentDaoProvider;

  private final Provider<SyncCheckpointStore> checkpointStoreProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  public StudentLinkRequestRepositoryImpl_Factory(Provider<Postgrest> postgrestProvider,
      Provider<StudentLinkRequestDao> requestDaoProvider,
      Provider<SessionStudentDao> sessionStudentDaoProvider,
      Provider<SyncCheckpointStore> checkpointStoreProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.postgrestProvider = postgrestProvider;
    this.requestDaoProvider = requestDaoProvider;
    this.sessionStudentDaoProvider = sessionStudentDaoProvider;
    this.checkpointStoreProvider = checkpointStoreProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public StudentLinkRequestRepositoryImpl get() {
    return newInstance(postgrestProvider.get(), requestDaoProvider.get(), sessionStudentDaoProvider.get(), checkpointStoreProvider.get(), sessionManagerProvider.get());
  }

  public static StudentLinkRequestRepositoryImpl_Factory create(
      Provider<Postgrest> postgrestProvider, Provider<StudentLinkRequestDao> requestDaoProvider,
      Provider<SessionStudentDao> sessionStudentDaoProvider,
      Provider<SyncCheckpointStore> checkpointStoreProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new StudentLinkRequestRepositoryImpl_Factory(postgrestProvider, requestDaoProvider, sessionStudentDaoProvider, checkpointStoreProvider, sessionManagerProvider);
  }

  public static StudentLinkRequestRepositoryImpl newInstance(Postgrest postgrest,
      StudentLinkRequestDao requestDao, SessionStudentDao sessionStudentDao,
      SyncCheckpointStore checkpointStore, SessionManager sessionManager) {
    return new StudentLinkRequestRepositoryImpl(postgrest, requestDao, sessionStudentDao, checkpointStore, sessionManager);
  }
}
