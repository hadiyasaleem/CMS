package com.mbd.cmsteacher.feature.schedule;

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
public final class ScheduleViewModel_Factory implements Factory<ScheduleViewModel> {
  private final Provider<DepartmentRepository> departmentRepositoryProvider;

  private final Provider<AcademicSessionRepository> sessionRepositoryProvider;

  private final Provider<SessionTimetableRepository> timetableRepositoryProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  public ScheduleViewModel_Factory(Provider<DepartmentRepository> departmentRepositoryProvider,
      Provider<AcademicSessionRepository> sessionRepositoryProvider,
      Provider<SessionTimetableRepository> timetableRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.departmentRepositoryProvider = departmentRepositoryProvider;
    this.sessionRepositoryProvider = sessionRepositoryProvider;
    this.timetableRepositoryProvider = timetableRepositoryProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public ScheduleViewModel get() {
    return newInstance(departmentRepositoryProvider.get(), sessionRepositoryProvider.get(), timetableRepositoryProvider.get(), sessionManagerProvider.get());
  }

  public static ScheduleViewModel_Factory create(
      Provider<DepartmentRepository> departmentRepositoryProvider,
      Provider<AcademicSessionRepository> sessionRepositoryProvider,
      Provider<SessionTimetableRepository> timetableRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new ScheduleViewModel_Factory(departmentRepositoryProvider, sessionRepositoryProvider, timetableRepositoryProvider, sessionManagerProvider);
  }

  public static ScheduleViewModel newInstance(DepartmentRepository departmentRepository,
      AcademicSessionRepository sessionRepository, SessionTimetableRepository timetableRepository,
      SessionManager sessionManager) {
    return new ScheduleViewModel(departmentRepository, sessionRepository, timetableRepository, sessionManager);
  }
}
