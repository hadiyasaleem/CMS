package com.mbd.cmscommon.data.sync;

import com.mbd.cmscommon.domain.repository.AcademicSessionRepository;
import com.mbd.cmscommon.domain.repository.AdministratorRepository;
import com.mbd.cmscommon.domain.repository.CurriculumRepository;
import com.mbd.cmscommon.domain.repository.DepartmentRepository;
import com.mbd.cmscommon.domain.repository.NotificationRepository;
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository;
import com.mbd.cmscommon.domain.repository.SessionMarksRepository;
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository;
import com.mbd.cmscommon.domain.repository.StudentLinkRequestRepository;
import com.mbd.cmscommon.domain.repository.TeacherRepository;
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
public final class AdminDataBootstrapper_Factory implements Factory<AdminDataBootstrapper> {
  private final Provider<AdministratorRepository> administratorRepositoryProvider;

  private final Provider<DepartmentRepository> departmentRepositoryProvider;

  private final Provider<TeacherRepository> teacherRepositoryProvider;

  private final Provider<AcademicSessionRepository> sessionRepositoryProvider;

  private final Provider<CurriculumRepository> curriculumRepositoryProvider;

  private final Provider<SessionTimetableRepository> timetableRepositoryProvider;

  private final Provider<SessionAttendanceRepository> attendanceRepositoryProvider;

  private final Provider<SessionMarksRepository> marksRepositoryProvider;

  private final Provider<StudentLinkRequestRepository> linkRequestRepositoryProvider;

  private final Provider<NotificationRepository> notificationRepositoryProvider;

  public AdminDataBootstrapper_Factory(
      Provider<AdministratorRepository> administratorRepositoryProvider,
      Provider<DepartmentRepository> departmentRepositoryProvider,
      Provider<TeacherRepository> teacherRepositoryProvider,
      Provider<AcademicSessionRepository> sessionRepositoryProvider,
      Provider<CurriculumRepository> curriculumRepositoryProvider,
      Provider<SessionTimetableRepository> timetableRepositoryProvider,
      Provider<SessionAttendanceRepository> attendanceRepositoryProvider,
      Provider<SessionMarksRepository> marksRepositoryProvider,
      Provider<StudentLinkRequestRepository> linkRequestRepositoryProvider,
      Provider<NotificationRepository> notificationRepositoryProvider) {
    this.administratorRepositoryProvider = administratorRepositoryProvider;
    this.departmentRepositoryProvider = departmentRepositoryProvider;
    this.teacherRepositoryProvider = teacherRepositoryProvider;
    this.sessionRepositoryProvider = sessionRepositoryProvider;
    this.curriculumRepositoryProvider = curriculumRepositoryProvider;
    this.timetableRepositoryProvider = timetableRepositoryProvider;
    this.attendanceRepositoryProvider = attendanceRepositoryProvider;
    this.marksRepositoryProvider = marksRepositoryProvider;
    this.linkRequestRepositoryProvider = linkRequestRepositoryProvider;
    this.notificationRepositoryProvider = notificationRepositoryProvider;
  }

  @Override
  public AdminDataBootstrapper get() {
    return newInstance(administratorRepositoryProvider.get(), departmentRepositoryProvider.get(), teacherRepositoryProvider.get(), sessionRepositoryProvider.get(), curriculumRepositoryProvider.get(), timetableRepositoryProvider.get(), attendanceRepositoryProvider.get(), marksRepositoryProvider.get(), linkRequestRepositoryProvider.get(), notificationRepositoryProvider.get());
  }

  public static AdminDataBootstrapper_Factory create(
      Provider<AdministratorRepository> administratorRepositoryProvider,
      Provider<DepartmentRepository> departmentRepositoryProvider,
      Provider<TeacherRepository> teacherRepositoryProvider,
      Provider<AcademicSessionRepository> sessionRepositoryProvider,
      Provider<CurriculumRepository> curriculumRepositoryProvider,
      Provider<SessionTimetableRepository> timetableRepositoryProvider,
      Provider<SessionAttendanceRepository> attendanceRepositoryProvider,
      Provider<SessionMarksRepository> marksRepositoryProvider,
      Provider<StudentLinkRequestRepository> linkRequestRepositoryProvider,
      Provider<NotificationRepository> notificationRepositoryProvider) {
    return new AdminDataBootstrapper_Factory(administratorRepositoryProvider, departmentRepositoryProvider, teacherRepositoryProvider, sessionRepositoryProvider, curriculumRepositoryProvider, timetableRepositoryProvider, attendanceRepositoryProvider, marksRepositoryProvider, linkRequestRepositoryProvider, notificationRepositoryProvider);
  }

  public static AdminDataBootstrapper newInstance(AdministratorRepository administratorRepository,
      DepartmentRepository departmentRepository, TeacherRepository teacherRepository,
      AcademicSessionRepository sessionRepository, CurriculumRepository curriculumRepository,
      SessionTimetableRepository timetableRepository,
      SessionAttendanceRepository attendanceRepository, SessionMarksRepository marksRepository,
      StudentLinkRequestRepository linkRequestRepository,
      NotificationRepository notificationRepository) {
    return new AdminDataBootstrapper(administratorRepository, departmentRepository, teacherRepository, sessionRepository, curriculumRepository, timetableRepository, attendanceRepository, marksRepository, linkRequestRepository, notificationRepository);
  }
}
