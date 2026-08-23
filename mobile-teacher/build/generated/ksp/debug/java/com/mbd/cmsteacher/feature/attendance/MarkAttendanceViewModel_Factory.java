package com.mbd.cmsteacher.feature.attendance;

import com.mbd.cmscommon.auth.SessionManager;
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository;
import com.mbd.cmscommon.domain.repository.NotificationRepository;
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository;
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
public final class MarkAttendanceViewModel_Factory implements Factory<MarkAttendanceViewModel> {
  private final Provider<TeacherAssignmentsProvider> assignmentsProvider;

  private final Provider<SessionAttendanceRepository> attendanceRepositoryProvider;

  private final Provider<AcademicSessionRepository> sessionRepositoryProvider;

  private final Provider<NotificationRepository> notificationRepositoryProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  public MarkAttendanceViewModel_Factory(Provider<TeacherAssignmentsProvider> assignmentsProvider,
      Provider<SessionAttendanceRepository> attendanceRepositoryProvider,
      Provider<AcademicSessionRepository> sessionRepositoryProvider,
      Provider<NotificationRepository> notificationRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.assignmentsProvider = assignmentsProvider;
    this.attendanceRepositoryProvider = attendanceRepositoryProvider;
    this.sessionRepositoryProvider = sessionRepositoryProvider;
    this.notificationRepositoryProvider = notificationRepositoryProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public MarkAttendanceViewModel get() {
    return newInstance(assignmentsProvider.get(), attendanceRepositoryProvider.get(), sessionRepositoryProvider.get(), notificationRepositoryProvider.get(), sessionManagerProvider.get());
  }

  public static MarkAttendanceViewModel_Factory create(
      Provider<TeacherAssignmentsProvider> assignmentsProvider,
      Provider<SessionAttendanceRepository> attendanceRepositoryProvider,
      Provider<AcademicSessionRepository> sessionRepositoryProvider,
      Provider<NotificationRepository> notificationRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new MarkAttendanceViewModel_Factory(assignmentsProvider, attendanceRepositoryProvider, sessionRepositoryProvider, notificationRepositoryProvider, sessionManagerProvider);
  }

  public static MarkAttendanceViewModel newInstance(TeacherAssignmentsProvider assignmentsProvider,
      SessionAttendanceRepository attendanceRepository, AcademicSessionRepository sessionRepository,
      NotificationRepository notificationRepository, SessionManager sessionManager) {
    return new MarkAttendanceViewModel(assignmentsProvider, attendanceRepository, sessionRepository, notificationRepository, sessionManager);
  }
}
