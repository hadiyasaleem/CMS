package com.mbd.cmsteacher.feature.insights;

import com.mbd.cmscommon.domain.repository.AcademicSessionRepository;
import com.mbd.cmscommon.domain.repository.DepartmentRepository;
import com.mbd.cmscommon.domain.repository.InsightsRepository;
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
public final class InsightsViewModel_Factory implements Factory<InsightsViewModel> {
  private final Provider<InsightsRepository> repoProvider;

  private final Provider<AcademicSessionRepository> sessionRepositoryProvider;

  private final Provider<DepartmentRepository> departmentRepositoryProvider;

  private final Provider<TeacherAssignmentsProvider> assignmentsProvider;

  public InsightsViewModel_Factory(Provider<InsightsRepository> repoProvider,
      Provider<AcademicSessionRepository> sessionRepositoryProvider,
      Provider<DepartmentRepository> departmentRepositoryProvider,
      Provider<TeacherAssignmentsProvider> assignmentsProvider) {
    this.repoProvider = repoProvider;
    this.sessionRepositoryProvider = sessionRepositoryProvider;
    this.departmentRepositoryProvider = departmentRepositoryProvider;
    this.assignmentsProvider = assignmentsProvider;
  }

  @Override
  public InsightsViewModel get() {
    return newInstance(repoProvider.get(), sessionRepositoryProvider.get(), departmentRepositoryProvider.get(), assignmentsProvider.get());
  }

  public static InsightsViewModel_Factory create(Provider<InsightsRepository> repoProvider,
      Provider<AcademicSessionRepository> sessionRepositoryProvider,
      Provider<DepartmentRepository> departmentRepositoryProvider,
      Provider<TeacherAssignmentsProvider> assignmentsProvider) {
    return new InsightsViewModel_Factory(repoProvider, sessionRepositoryProvider, departmentRepositoryProvider, assignmentsProvider);
  }

  public static InsightsViewModel newInstance(InsightsRepository repo,
      AcademicSessionRepository sessionRepository, DepartmentRepository departmentRepository,
      TeacherAssignmentsProvider assignmentsProvider) {
    return new InsightsViewModel(repo, sessionRepository, departmentRepository, assignmentsProvider);
  }
}
