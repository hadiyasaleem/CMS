package com.mbd.cmsteacher.feature.hub;

import com.mbd.cmscommon.auth.SessionManager;
import com.mbd.cmscommon.domain.repository.NotificationRepository;
import com.mbd.cmscommon.domain.repository.StudentLinkRequestRepository;
import com.mbd.cmscommon.domain.repository.TeacherRepository;
import com.mbd.cmscommon.domain.repository.UserRepository;
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider;
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
public final class MenuViewModel_Factory implements Factory<MenuViewModel> {
  private final Provider<SessionManager> sessionManagerProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<TeacherRepository> teacherRepositoryProvider;

  private final Provider<TeacherAssignmentsProvider> assignmentsProvider;

  private final Provider<NotificationRepository> notificationRepositoryProvider;

  private final Provider<StudentLinkRequestRepository> linkRequestRepositoryProvider;

  public MenuViewModel_Factory(Provider<SessionManager> sessionManagerProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<TeacherRepository> teacherRepositoryProvider,
      Provider<TeacherAssignmentsProvider> assignmentsProvider,
      Provider<NotificationRepository> notificationRepositoryProvider,
      Provider<StudentLinkRequestRepository> linkRequestRepositoryProvider) {
    this.sessionManagerProvider = sessionManagerProvider;
    this.userRepositoryProvider = userRepositoryProvider;
    this.teacherRepositoryProvider = teacherRepositoryProvider;
    this.assignmentsProvider = assignmentsProvider;
    this.notificationRepositoryProvider = notificationRepositoryProvider;
    this.linkRequestRepositoryProvider = linkRequestRepositoryProvider;
  }

  @Override
  public MenuViewModel get() {
    return newInstance(sessionManagerProvider.get(), userRepositoryProvider.get(), teacherRepositoryProvider.get(), assignmentsProvider.get(), notificationRepositoryProvider.get(), linkRequestRepositoryProvider.get());
  }

  public static MenuViewModel_Factory create(Provider<SessionManager> sessionManagerProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<TeacherRepository> teacherRepositoryProvider,
      Provider<TeacherAssignmentsProvider> assignmentsProvider,
      Provider<NotificationRepository> notificationRepositoryProvider,
      Provider<StudentLinkRequestRepository> linkRequestRepositoryProvider) {
    return new MenuViewModel_Factory(sessionManagerProvider, userRepositoryProvider, teacherRepositoryProvider, assignmentsProvider, notificationRepositoryProvider, linkRequestRepositoryProvider);
  }

  public static MenuViewModel newInstance(SessionManager sessionManager,
      UserRepository userRepository, TeacherRepository teacherRepository,
      TeacherAssignmentsProvider assignmentsProvider, NotificationRepository notificationRepository,
      StudentLinkRequestRepository linkRequestRepository) {
    return new MenuViewModel(sessionManager, userRepository, teacherRepository, assignmentsProvider, notificationRepository, linkRequestRepository);
  }
}
