package com.mbd.cmsstudent.feature.profile;

import com.mbd.cmscommon.auth.SessionManager;
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository;
import com.mbd.cmscommon.domain.repository.DepartmentRepository;
import com.mbd.cmscommon.domain.repository.FineRepository;
import com.mbd.cmscommon.domain.repository.UserRepository;
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
public final class ProfileViewModel_Factory implements Factory<ProfileViewModel> {
  private final Provider<SessionManager> sessionManagerProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<AcademicSessionRepository> sessionRepositoryProvider;

  private final Provider<FineRepository> fineRepositoryProvider;

  private final Provider<DepartmentRepository> departmentRepositoryProvider;

  private final Provider<CurrentStudentProvider> currentStudentProvider;

  public ProfileViewModel_Factory(Provider<SessionManager> sessionManagerProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<AcademicSessionRepository> sessionRepositoryProvider,
      Provider<FineRepository> fineRepositoryProvider,
      Provider<DepartmentRepository> departmentRepositoryProvider,
      Provider<CurrentStudentProvider> currentStudentProvider) {
    this.sessionManagerProvider = sessionManagerProvider;
    this.userRepositoryProvider = userRepositoryProvider;
    this.sessionRepositoryProvider = sessionRepositoryProvider;
    this.fineRepositoryProvider = fineRepositoryProvider;
    this.departmentRepositoryProvider = departmentRepositoryProvider;
    this.currentStudentProvider = currentStudentProvider;
  }

  @Override
  public ProfileViewModel get() {
    return newInstance(sessionManagerProvider.get(), userRepositoryProvider.get(), sessionRepositoryProvider.get(), fineRepositoryProvider.get(), departmentRepositoryProvider.get(), currentStudentProvider.get());
  }

  public static ProfileViewModel_Factory create(Provider<SessionManager> sessionManagerProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<AcademicSessionRepository> sessionRepositoryProvider,
      Provider<FineRepository> fineRepositoryProvider,
      Provider<DepartmentRepository> departmentRepositoryProvider,
      Provider<CurrentStudentProvider> currentStudentProvider) {
    return new ProfileViewModel_Factory(sessionManagerProvider, userRepositoryProvider, sessionRepositoryProvider, fineRepositoryProvider, departmentRepositoryProvider, currentStudentProvider);
  }

  public static ProfileViewModel newInstance(SessionManager sessionManager,
      UserRepository userRepository, AcademicSessionRepository sessionRepository,
      FineRepository fineRepository, DepartmentRepository departmentRepository,
      CurrentStudentProvider currentStudentProvider) {
    return new ProfileViewModel(sessionManager, userRepository, sessionRepository, fineRepository, departmentRepository, currentStudentProvider);
  }
}
