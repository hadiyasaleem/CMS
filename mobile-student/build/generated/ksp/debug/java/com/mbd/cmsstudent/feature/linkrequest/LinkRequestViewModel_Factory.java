package com.mbd.cmsstudent.feature.linkrequest;

import com.mbd.cmscommon.auth.SessionManager;
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository;
import com.mbd.cmscommon.domain.repository.DepartmentRepository;
import com.mbd.cmscommon.domain.repository.StudentLinkRequestRepository;
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
public final class LinkRequestViewModel_Factory implements Factory<LinkRequestViewModel> {
  private final Provider<StudentLinkRequestRepository> repositoryProvider;

  private final Provider<DepartmentRepository> departmentRepositoryProvider;

  private final Provider<AcademicSessionRepository> sessionRepositoryProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  public LinkRequestViewModel_Factory(Provider<StudentLinkRequestRepository> repositoryProvider,
      Provider<DepartmentRepository> departmentRepositoryProvider,
      Provider<AcademicSessionRepository> sessionRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.departmentRepositoryProvider = departmentRepositoryProvider;
    this.sessionRepositoryProvider = sessionRepositoryProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public LinkRequestViewModel get() {
    return newInstance(repositoryProvider.get(), departmentRepositoryProvider.get(), sessionRepositoryProvider.get(), sessionManagerProvider.get());
  }

  public static LinkRequestViewModel_Factory create(
      Provider<StudentLinkRequestRepository> repositoryProvider,
      Provider<DepartmentRepository> departmentRepositoryProvider,
      Provider<AcademicSessionRepository> sessionRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new LinkRequestViewModel_Factory(repositoryProvider, departmentRepositoryProvider, sessionRepositoryProvider, sessionManagerProvider);
  }

  public static LinkRequestViewModel newInstance(StudentLinkRequestRepository repository,
      DepartmentRepository departmentRepository, AcademicSessionRepository sessionRepository,
      SessionManager sessionManager) {
    return new LinkRequestViewModel(repository, departmentRepository, sessionRepository, sessionManager);
  }
}
