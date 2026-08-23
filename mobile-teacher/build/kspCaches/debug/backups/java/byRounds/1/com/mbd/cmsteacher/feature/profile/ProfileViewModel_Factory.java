package com.mbd.cmsteacher.feature.profile;

import com.mbd.cmscommon.auth.SessionManager;
import com.mbd.cmscommon.domain.repository.DepartmentRepository;
import com.mbd.cmscommon.domain.repository.TeacherRepository;
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider;
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

  private final Provider<TeacherRepository> teacherRepositoryProvider;

  private final Provider<DepartmentRepository> departmentRepositoryProvider;

  private final Provider<TeacherAssignmentsProvider> assignmentsProvider;

  public ProfileViewModel_Factory(Provider<SessionManager> sessionManagerProvider,
      Provider<TeacherRepository> teacherRepositoryProvider,
      Provider<DepartmentRepository> departmentRepositoryProvider,
      Provider<TeacherAssignmentsProvider> assignmentsProvider) {
    this.sessionManagerProvider = sessionManagerProvider;
    this.teacherRepositoryProvider = teacherRepositoryProvider;
    this.departmentRepositoryProvider = departmentRepositoryProvider;
    this.assignmentsProvider = assignmentsProvider;
  }

  @Override
  public ProfileViewModel get() {
    return newInstance(sessionManagerProvider.get(), teacherRepositoryProvider.get(), departmentRepositoryProvider.get(), assignmentsProvider.get());
  }

  public static ProfileViewModel_Factory create(Provider<SessionManager> sessionManagerProvider,
      Provider<TeacherRepository> teacherRepositoryProvider,
      Provider<DepartmentRepository> departmentRepositoryProvider,
      Provider<TeacherAssignmentsProvider> assignmentsProvider) {
    return new ProfileViewModel_Factory(sessionManagerProvider, teacherRepositoryProvider, departmentRepositoryProvider, assignmentsProvider);
  }

  public static ProfileViewModel newInstance(SessionManager sessionManager,
      TeacherRepository teacherRepository, DepartmentRepository departmentRepository,
      TeacherAssignmentsProvider assignmentsProvider) {
    return new ProfileViewModel(sessionManager, teacherRepository, departmentRepository, assignmentsProvider);
  }
}
