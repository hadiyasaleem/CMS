package com.mbd.cmsteacher.feature.notifications;

import com.mbd.cmscommon.auth.SessionManager;
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository;
import com.mbd.cmscommon.domain.repository.DepartmentRepository;
import com.mbd.cmscommon.domain.repository.NotificationRepository;
import com.mbd.cmscommon.domain.repository.TeacherRepository;
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
public final class NotificationsViewModel_Factory implements Factory<NotificationsViewModel> {
  private final Provider<NotificationRepository> repositoryProvider;

  private final Provider<AcademicSessionRepository> sessionRepositoryProvider;

  private final Provider<DepartmentRepository> departmentRepositoryProvider;

  private final Provider<TeacherRepository> teacherRepositoryProvider;

  private final Provider<TeacherAssignmentsProvider> assignmentsProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  public NotificationsViewModel_Factory(Provider<NotificationRepository> repositoryProvider,
      Provider<AcademicSessionRepository> sessionRepositoryProvider,
      Provider<DepartmentRepository> departmentRepositoryProvider,
      Provider<TeacherRepository> teacherRepositoryProvider,
      Provider<TeacherAssignmentsProvider> assignmentsProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.sessionRepositoryProvider = sessionRepositoryProvider;
    this.departmentRepositoryProvider = departmentRepositoryProvider;
    this.teacherRepositoryProvider = teacherRepositoryProvider;
    this.assignmentsProvider = assignmentsProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public NotificationsViewModel get() {
    return newInstance(repositoryProvider.get(), sessionRepositoryProvider.get(), departmentRepositoryProvider.get(), teacherRepositoryProvider.get(), assignmentsProvider.get(), sessionManagerProvider.get());
  }

  public static NotificationsViewModel_Factory create(
      Provider<NotificationRepository> repositoryProvider,
      Provider<AcademicSessionRepository> sessionRepositoryProvider,
      Provider<DepartmentRepository> departmentRepositoryProvider,
      Provider<TeacherRepository> teacherRepositoryProvider,
      Provider<TeacherAssignmentsProvider> assignmentsProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new NotificationsViewModel_Factory(repositoryProvider, sessionRepositoryProvider, departmentRepositoryProvider, teacherRepositoryProvider, assignmentsProvider, sessionManagerProvider);
  }

  public static NotificationsViewModel newInstance(NotificationRepository repository,
      AcademicSessionRepository sessionRepository, DepartmentRepository departmentRepository,
      TeacherRepository teacherRepository, TeacherAssignmentsProvider assignmentsProvider,
      SessionManager sessionManager) {
    return new NotificationsViewModel(repository, sessionRepository, departmentRepository, teacherRepository, assignmentsProvider, sessionManager);
  }
}
