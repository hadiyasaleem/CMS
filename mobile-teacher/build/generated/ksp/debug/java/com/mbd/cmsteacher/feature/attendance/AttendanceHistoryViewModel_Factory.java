package com.mbd.cmsteacher.feature.attendance;

import androidx.lifecycle.SavedStateHandle;
import com.mbd.cmscommon.auth.SessionManager;
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository;
import com.mbd.cmscommon.domain.repository.CurriculumRepository;
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository;
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository;
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
public final class AttendanceHistoryViewModel_Factory implements Factory<AttendanceHistoryViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<SessionAttendanceRepository> attendanceRepositoryProvider;

  private final Provider<AcademicSessionRepository> sessionRepositoryProvider;

  private final Provider<CurriculumRepository> curriculumRepositoryProvider;

  private final Provider<SessionTimetableRepository> timetableRepositoryProvider;

  private final Provider<TeacherRepository> teacherRepositoryProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  public AttendanceHistoryViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<SessionAttendanceRepository> attendanceRepositoryProvider,
      Provider<AcademicSessionRepository> sessionRepositoryProvider,
      Provider<CurriculumRepository> curriculumRepositoryProvider,
      Provider<SessionTimetableRepository> timetableRepositoryProvider,
      Provider<TeacherRepository> teacherRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.attendanceRepositoryProvider = attendanceRepositoryProvider;
    this.sessionRepositoryProvider = sessionRepositoryProvider;
    this.curriculumRepositoryProvider = curriculumRepositoryProvider;
    this.timetableRepositoryProvider = timetableRepositoryProvider;
    this.teacherRepositoryProvider = teacherRepositoryProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public AttendanceHistoryViewModel get() {
    return newInstance(savedStateHandleProvider.get(), attendanceRepositoryProvider.get(), sessionRepositoryProvider.get(), curriculumRepositoryProvider.get(), timetableRepositoryProvider.get(), teacherRepositoryProvider.get(), sessionManagerProvider.get());
  }

  public static AttendanceHistoryViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<SessionAttendanceRepository> attendanceRepositoryProvider,
      Provider<AcademicSessionRepository> sessionRepositoryProvider,
      Provider<CurriculumRepository> curriculumRepositoryProvider,
      Provider<SessionTimetableRepository> timetableRepositoryProvider,
      Provider<TeacherRepository> teacherRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new AttendanceHistoryViewModel_Factory(savedStateHandleProvider, attendanceRepositoryProvider, sessionRepositoryProvider, curriculumRepositoryProvider, timetableRepositoryProvider, teacherRepositoryProvider, sessionManagerProvider);
  }

  public static AttendanceHistoryViewModel newInstance(SavedStateHandle savedStateHandle,
      SessionAttendanceRepository attendanceRepository, AcademicSessionRepository sessionRepository,
      CurriculumRepository curriculumRepository, SessionTimetableRepository timetableRepository,
      TeacherRepository teacherRepository, SessionManager sessionManager) {
    return new AttendanceHistoryViewModel(savedStateHandle, attendanceRepository, sessionRepository, curriculumRepository, timetableRepository, teacherRepository, sessionManager);
  }
}
