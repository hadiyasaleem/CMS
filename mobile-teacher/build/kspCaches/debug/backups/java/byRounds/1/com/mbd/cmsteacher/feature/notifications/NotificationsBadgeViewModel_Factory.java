package com.mbd.cmsteacher.feature.notifications;

import com.mbd.cmscommon.auth.SessionManager;
import com.mbd.cmscommon.domain.repository.NotificationRepository;
import com.mbd.cmscommon.domain.repository.TeacherRepository;
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
public final class NotificationsBadgeViewModel_Factory implements Factory<NotificationsBadgeViewModel> {
  private final Provider<NotificationRepository> repositoryProvider;

  private final Provider<TeacherRepository> teacherRepositoryProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  public NotificationsBadgeViewModel_Factory(Provider<NotificationRepository> repositoryProvider,
      Provider<TeacherRepository> teacherRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.teacherRepositoryProvider = teacherRepositoryProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public NotificationsBadgeViewModel get() {
    return newInstance(repositoryProvider.get(), teacherRepositoryProvider.get(), sessionManagerProvider.get());
  }

  public static NotificationsBadgeViewModel_Factory create(
      Provider<NotificationRepository> repositoryProvider,
      Provider<TeacherRepository> teacherRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new NotificationsBadgeViewModel_Factory(repositoryProvider, teacherRepositoryProvider, sessionManagerProvider);
  }

  public static NotificationsBadgeViewModel newInstance(NotificationRepository repository,
      TeacherRepository teacherRepository, SessionManager sessionManager) {
    return new NotificationsBadgeViewModel(repository, teacherRepository, sessionManager);
  }
}
