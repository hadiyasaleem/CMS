package com.mbd.cmsstudent.feature.common;

import com.mbd.cmscommon.domain.repository.AcademicSessionRepository;
import com.mbd.cmscommon.domain.repository.CurriculumRepository;
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository;
import com.mbd.cmscommon.domain.repository.SessionMarksRepository;
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository;
import com.mbd.cmscommon.domain.repository.UserRepository;
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
public final class CurrentStudentProvider_Factory implements Factory<CurrentStudentProvider> {
  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<AcademicSessionRepository> sessionRepositoryProvider;

  private final Provider<CurriculumRepository> curriculumRepositoryProvider;

  private final Provider<SessionTimetableRepository> timetableRepositoryProvider;

  private final Provider<SessionAttendanceRepository> attendanceRepositoryProvider;

  private final Provider<SessionMarksRepository> marksRepositoryProvider;

  public CurrentStudentProvider_Factory(Provider<UserRepository> userRepositoryProvider,
      Provider<AcademicSessionRepository> sessionRepositoryProvider,
      Provider<CurriculumRepository> curriculumRepositoryProvider,
      Provider<SessionTimetableRepository> timetableRepositoryProvider,
      Provider<SessionAttendanceRepository> attendanceRepositoryProvider,
      Provider<SessionMarksRepository> marksRepositoryProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
    this.sessionRepositoryProvider = sessionRepositoryProvider;
    this.curriculumRepositoryProvider = curriculumRepositoryProvider;
    this.timetableRepositoryProvider = timetableRepositoryProvider;
    this.attendanceRepositoryProvider = attendanceRepositoryProvider;
    this.marksRepositoryProvider = marksRepositoryProvider;
  }

  @Override
  public CurrentStudentProvider get() {
    return newInstance(userRepositoryProvider.get(), sessionRepositoryProvider.get(), curriculumRepositoryProvider.get(), timetableRepositoryProvider.get(), attendanceRepositoryProvider.get(), marksRepositoryProvider.get());
  }

  public static CurrentStudentProvider_Factory create(
      Provider<UserRepository> userRepositoryProvider,
      Provider<AcademicSessionRepository> sessionRepositoryProvider,
      Provider<CurriculumRepository> curriculumRepositoryProvider,
      Provider<SessionTimetableRepository> timetableRepositoryProvider,
      Provider<SessionAttendanceRepository> attendanceRepositoryProvider,
      Provider<SessionMarksRepository> marksRepositoryProvider) {
    return new CurrentStudentProvider_Factory(userRepositoryProvider, sessionRepositoryProvider, curriculumRepositoryProvider, timetableRepositoryProvider, attendanceRepositoryProvider, marksRepositoryProvider);
  }

  public static CurrentStudentProvider newInstance(UserRepository userRepository,
      AcademicSessionRepository sessionRepository, CurriculumRepository curriculumRepository,
      SessionTimetableRepository timetableRepository,
      SessionAttendanceRepository attendanceRepository, SessionMarksRepository marksRepository) {
    return new CurrentStudentProvider(userRepository, sessionRepository, curriculumRepository, timetableRepository, attendanceRepository, marksRepository);
  }
}
