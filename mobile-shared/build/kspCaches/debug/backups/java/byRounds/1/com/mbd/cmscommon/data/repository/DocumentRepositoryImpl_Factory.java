package com.mbd.cmscommon.data.repository;

import com.mbd.cmscommon.auth.SessionManager;
import com.mbd.cmscommon.data.local.dao.DocumentDao;
import com.mbd.cmscommon.data.sync.SyncCheckpointStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.jan.supabase.postgrest.Postgrest;
import io.github.jan.supabase.storage.Storage;
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
public final class DocumentRepositoryImpl_Factory implements Factory<DocumentRepositoryImpl> {
  private final Provider<Postgrest> postgrestProvider;

  private final Provider<Storage> storageProvider;

  private final Provider<DocumentDao> documentDaoProvider;

  private final Provider<SyncCheckpointStore> checkpointStoreProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  public DocumentRepositoryImpl_Factory(Provider<Postgrest> postgrestProvider,
      Provider<Storage> storageProvider, Provider<DocumentDao> documentDaoProvider,
      Provider<SyncCheckpointStore> checkpointStoreProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.postgrestProvider = postgrestProvider;
    this.storageProvider = storageProvider;
    this.documentDaoProvider = documentDaoProvider;
    this.checkpointStoreProvider = checkpointStoreProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public DocumentRepositoryImpl get() {
    return newInstance(postgrestProvider.get(), storageProvider.get(), documentDaoProvider.get(), checkpointStoreProvider.get(), sessionManagerProvider.get());
  }

  public static DocumentRepositoryImpl_Factory create(Provider<Postgrest> postgrestProvider,
      Provider<Storage> storageProvider, Provider<DocumentDao> documentDaoProvider,
      Provider<SyncCheckpointStore> checkpointStoreProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new DocumentRepositoryImpl_Factory(postgrestProvider, storageProvider, documentDaoProvider, checkpointStoreProvider, sessionManagerProvider);
  }

  public static DocumentRepositoryImpl newInstance(Postgrest postgrest, Storage storage,
      DocumentDao documentDao, SyncCheckpointStore checkpointStore, SessionManager sessionManager) {
    return new DocumentRepositoryImpl(postgrest, storage, documentDao, checkpointStore, sessionManager);
  }
}
