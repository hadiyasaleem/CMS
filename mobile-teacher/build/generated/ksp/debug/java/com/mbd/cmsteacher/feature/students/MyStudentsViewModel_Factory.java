package com.mbd.cmsteacher.feature.students;

import com.mbd.cmscommon.domain.repository.AcademicSessionRepository;
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
public final class MyStudentsViewModel_Factory implements Factory<MyStudentsViewModel> {
  private final Provider<TeacherAssignmentsProvider> assignmentsProvider;

  private final Provider<AcademicSessionRepository> sessionRepositoryProvider;

  private final Provider<SessionAttendanceRepository> attendanceRepositoryProvider;

  public MyStudentsViewModel_Factory(Provider<TeacherAssignmentsProvider> assignmentsProvider,
      Provider<AcademicSessionRepository> sessionRepositoryProvider,
      Provider<SessionAttendanceRepository> attendanceRepositoryProvider) {
    this.assignmentsProvider = assignmentsProvider;
    this.sessionRepositoryProvider = sessionRepositoryProvider;
    this.attendanceRepositoryProvider = attendanceRepositoryProvider;
  }

  @Override
  public MyStudentsViewModel get() {
    return newInstance(assignmentsProvider.get(), sessionRepositoryProvider.get(), attendanceRepositoryProvider.get());
  }

  public static MyStudentsViewModel_Factory create(
      Provider<TeacherAssignmentsProvider> assignmentsProvider,
      Provider<AcademicSessionRepository> sessionRepositoryProvider,
      Provider<SessionAttendanceRepository> attendanceRepositoryProvider) {
    return new MyStudentsViewModel_Factory(assignmentsProvider, sessionRepositoryProvider, attendanceRepositoryProvider);
  }

  public static MyStudentsViewModel newInstance(TeacherAssignmentsProvider assignmentsProvider,
      AcademicSessionRepository sessionRepository,
      SessionAttendanceRepository attendanceRepository) {
    return new MyStudentsViewModel(assignmentsProvider, sessionRepository, attendanceRepository);
  }
}
