package com.mbd.cmscommon.teacher;

import com.mbd.cmscommon.auth.SessionManager;
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository;
import com.mbd.cmscommon.domain.repository.DepartmentRepository;
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository;
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
public final class TeacherAssignmentsProvider_Factory implements Factory<TeacherAssignmentsProvider> {
  private final Provider<SessionManager> sessionManagerProvider;

  private final Provider<SessionTimetableRepository> timetableRepositoryProvider;

  private final Provider<AcademicSessionRepository> sessionRepositoryProvider;

  private final Provider<DepartmentRepository> departmentRepositoryProvider;

  public TeacherAssignmentsProvider_Factory(Provider<SessionManager> sessionManagerProvider,
      Provider<SessionTimetableRepository> timetableRepositoryProvider,
      Provider<AcademicSessionRepository> sessionRepositoryProvider,
      Provider<DepartmentRepository> departmentRepositoryProvider) {
    this.sessionManagerProvider = sessionManagerProvider;
    this.timetableRepositoryProvider = timetableRepositoryProvider;
    this.sessionRepositoryProvider = sessionRepositoryProvider;
    this.departmentRepositoryProvider = departmentRepositoryProvider;
  }

  @Override
  public TeacherAssignmentsProvider get() {
    return newInstance(sessionManagerProvider.get(), timetableRepositoryProvider.get(), sessionRepositoryProvider.get(), departmentRepositoryProvider.get());
  }

  public static TeacherAssignmentsProvider_Factory create(
      Provider<SessionManager> sessionManagerProvider,
      Provider<SessionTimetableRepository> timetableRepositoryProvider,
      Provider<AcademicSessionRepository> sessionRepositoryProvider,
      Provider<DepartmentRepository> departmentRepositoryProvider) {
    return new TeacherAssignmentsProvider_Factory(sessionManagerProvider, timetableRepositoryProvider, sessionRepositoryProvider, departmentRepositoryProvider);
  }

  public static TeacherAssignmentsProvider newInstance(SessionManager sessionManager,
      SessionTimetableRepository timetableRepository, AcademicSessionRepository sessionRepository,
      DepartmentRepository departmentRepository) {
    return new TeacherAssignmentsProvider(sessionManager, timetableRepository, sessionRepository, departmentRepository);
  }
}
