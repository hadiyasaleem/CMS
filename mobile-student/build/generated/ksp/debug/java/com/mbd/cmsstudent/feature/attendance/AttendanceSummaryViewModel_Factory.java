package com.mbd.cmsstudent.feature.attendance;

import com.mbd.cmscommon.domain.repository.CurriculumRepository;
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository;
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
public final class AttendanceSummaryViewModel_Factory implements Factory<AttendanceSummaryViewModel> {
  private final Provider<CurrentStudentProvider> currentStudentProvider;

  private final Provider<SessionAttendanceRepository> attendanceRepositoryProvider;

  private final Provider<CurriculumRepository> curriculumRepositoryProvider;

  public AttendanceSummaryViewModel_Factory(Provider<CurrentStudentProvider> currentStudentProvider,
      Provider<SessionAttendanceRepository> attendanceRepositoryProvider,
      Provider<CurriculumRepository> curriculumRepositoryProvider) {
    this.currentStudentProvider = currentStudentProvider;
    this.attendanceRepositoryProvider = attendanceRepositoryProvider;
    this.curriculumRepositoryProvider = curriculumRepositoryProvider;
  }

  @Override
  public AttendanceSummaryViewModel get() {
    return newInstance(currentStudentProvider.get(), attendanceRepositoryProvider.get(), curriculumRepositoryProvider.get());
  }

  public static AttendanceSummaryViewModel_Factory create(
      Provider<CurrentStudentProvider> currentStudentProvider,
      Provider<SessionAttendanceRepository> attendanceRepositoryProvider,
      Provider<CurriculumRepository> curriculumRepositoryProvider) {
    return new AttendanceSummaryViewModel_Factory(currentStudentProvider, attendanceRepositoryProvider, curriculumRepositoryProvider);
  }

  public static AttendanceSummaryViewModel newInstance(
      CurrentStudentProvider currentStudentProvider,
      SessionAttendanceRepository attendanceRepository, CurriculumRepository curriculumRepository) {
    return new AttendanceSummaryViewModel(currentStudentProvider, attendanceRepository, curriculumRepository);
  }
}
