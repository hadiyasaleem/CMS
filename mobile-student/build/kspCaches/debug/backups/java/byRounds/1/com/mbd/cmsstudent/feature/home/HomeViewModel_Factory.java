package com.mbd.cmsstudent.feature.home;

import com.mbd.cmscommon.domain.repository.AcademicSessionRepository;
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository;
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<CurrentStudentProvider> currentStudentProvider;

  private final Provider<AcademicSessionRepository> sessionRepositoryProvider;

  private final Provider<SessionAttendanceRepository> attendanceRepositoryProvider;

  private final Provider<SessionTimetableRepository> timetableRepositoryProvider;

  public HomeViewModel_Factory(Provider<CurrentStudentProvider> currentStudentProvider,
      Provider<AcademicSessionRepository> sessionRepositoryProvider,
      Provider<SessionAttendanceRepository> attendanceRepositoryProvider,
      Provider<SessionTimetableRepository> timetableRepositoryProvider) {
    this.currentStudentProvider = currentStudentProvider;
    this.sessionRepositoryProvider = sessionRepositoryProvider;
    this.attendanceRepositoryProvider = attendanceRepositoryProvider;
    this.timetableRepositoryProvider = timetableRepositoryProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(currentStudentProvider.get(), sessionRepositoryProvider.get(), attendanceRepositoryProvider.get(), timetableRepositoryProvider.get());
  }

  public static HomeViewModel_Factory create(
      Provider<CurrentStudentProvider> currentStudentProvider,
      Provider<AcademicSessionRepository> sessionRepositoryProvider,
      Provider<SessionAttendanceRepository> attendanceRepositoryProvider,
      Provider<SessionTimetableRepository> timetableRepositoryProvider) {
    return new HomeViewModel_Factory(currentStudentProvider, sessionRepositoryProvider, attendanceRepositoryProvider, timetableRepositoryProvider);
  }

  public static HomeViewModel newInstance(CurrentStudentProvider currentStudentProvider,
      AcademicSessionRepository sessionRepository, SessionAttendanceRepository attendanceRepository,
      SessionTimetableRepository timetableRepository) {
    return new HomeViewModel(currentStudentProvider, sessionRepository, attendanceRepository, timetableRepository);
  }
}
