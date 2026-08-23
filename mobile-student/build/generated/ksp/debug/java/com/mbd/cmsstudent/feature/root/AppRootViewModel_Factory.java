package com.mbd.cmsstudent.feature.root;

import com.mbd.cmscommon.auth.SessionManager;
import com.mbd.cmscommon.data.sync.StartupBootstrapTracker;
import com.mbd.cmscommon.data.sync.SyncEngine;
import com.mbd.cmscommon.domain.repository.UserRepository;
import com.mbd.cmsstudent.feature.common.CurrentStudentProvider;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class AppRootViewModel_Factory implements Factory<AppRootViewModel> {
  private final Provider<SessionManager> sessionManagerProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<SyncEngine> syncEngineProvider;

  private final Provider<CurrentStudentProvider> currentStudentProvider;

  private final Provider<StartupBootstrapTracker> startupBootstrapTrackerProvider;

  public AppRootViewModel_Factory(Provider<SessionManager> sessionManagerProvider,
      Provider<UserRepository> userRepositoryProvider, Provider<SyncEngine> syncEngineProvider,
      Provider<CurrentStudentProvider> currentStudentProvider,
      Provider<StartupBootstrapTracker> startupBootstrapTrackerProvider) {
    this.sessionManagerProvider = sessionManagerProvider;
    this.userRepositoryProvider = userRepositoryProvider;
    this.syncEngineProvider = syncEngineProvider;
    this.currentStudentProvider = currentStudentProvider;
    this.startupBootstrapTrackerProvider = startupBootstrapTrackerProvider;
  }

  @Override
  public AppRootViewModel get() {
    return newInstance(sessionManagerProvider.get(), userRepositoryProvider.get(), syncEngineProvider.get(), currentStudentProvider.get(), startupBootstrapTrackerProvider.get());
  }

  public static AppRootViewModel_Factory create(Provider<SessionManager> sessionManagerProvider,
      Provider<UserRepository> userRepositoryProvider, Provider<SyncEngine> syncEngineProvider,
      Provider<CurrentStudentProvider> currentStudentProvider,
      Provider<StartupBootstrapTracker> startupBootstrapTrackerProvider) {
    return new AppRootViewModel_Factory(sessionManagerProvider, userRepositoryProvider, syncEngineProvider, currentStudentProvider, startupBootstrapTrackerProvider);
  }

  public static AppRootViewModel newInstance(SessionManager sessionManager,
      UserRepository userRepository, SyncEngine syncEngine,
      CurrentStudentProvider currentStudentProvider,
      StartupBootstrapTracker startupBootstrapTracker) {
    return new AppRootViewModel(sessionManager, userRepository, syncEngine, currentStudentProvider, startupBootstrapTracker);
  }
}
