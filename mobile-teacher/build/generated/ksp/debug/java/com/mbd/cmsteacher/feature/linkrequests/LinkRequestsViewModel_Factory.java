package com.mbd.cmsteacher.feature.linkrequests;

import com.mbd.cmscommon.auth.SessionManager;
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository;
import com.mbd.cmscommon.domain.repository.DepartmentRepository;
import com.mbd.cmscommon.domain.repository.StudentLinkRequestRepository;
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
public final class LinkRequestsViewModel_Factory implements Factory<LinkRequestsViewModel> {
  private final Provider<StudentLinkRequestRepository> repositoryProvider;

  private final Provider<AcademicSessionRepository> sessionRepositoryProvider;

  private final Provider<DepartmentRepository> departmentRepositoryProvider;

  private final Provider<TeacherRepository> teacherRepositoryProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  public LinkRequestsViewModel_Factory(Provider<StudentLinkRequestRepository> repositoryProvider,
      Provider<AcademicSessionRepository> sessionRepositoryProvider,
      Provider<DepartmentRepository> departmentRepositoryProvider,
      Provider<TeacherRepository> teacherRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.sessionRepositoryProvider = sessionRepositoryProvider;
    this.departmentRepositoryProvider = departmentRepositoryProvider;
    this.teacherRepositoryProvider = teacherRepositoryProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public LinkRequestsViewModel get() {
    return newInstance(repositoryProvider.get(), sessionRepositoryProvider.get(), departmentRepositoryProvider.get(), teacherRepositoryProvider.get(), sessionManagerProvider.get());
  }

  public static LinkRequestsViewModel_Factory create(
      Provider<StudentLinkRequestRepository> repositoryProvider,
      Provider<AcademicSessionRepository> sessionRepositoryProvider,
      Provider<DepartmentRepository> departmentRepositoryProvider,
      Provider<TeacherRepository> teacherRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new LinkRequestsViewModel_Factory(repositoryProvider, sessionRepositoryProvider, departmentRepositoryProvider, teacherRepositoryProvider, sessionManagerProvider);
  }

  public static LinkRequestsViewModel newInstance(StudentLinkRequestRepository repository,
      AcademicSessionRepository sessionRepository, DepartmentRepository departmentRepository,
      TeacherRepository teacherRepository, SessionManager sessionManager) {
    return new LinkRequestsViewModel(repository, sessionRepository, departmentRepository, teacherRepository, sessionManager);
  }
}
