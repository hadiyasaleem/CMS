package com.mbd.cmsteacher.feature.results;

import com.mbd.cmscommon.domain.repository.AcademicSessionRepository;
import com.mbd.cmscommon.domain.repository.CurriculumRepository;
import com.mbd.cmscommon.domain.repository.SessionMarksRepository;
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
public final class SemesterResultsViewModel_Factory implements Factory<SemesterResultsViewModel> {
  private final Provider<TeacherAssignmentsProvider> assignmentsProvider;

  private final Provider<SessionMarksRepository> marksRepositoryProvider;

  private final Provider<AcademicSessionRepository> sessionRepositoryProvider;

  private final Provider<CurriculumRepository> curriculumRepositoryProvider;

  public SemesterResultsViewModel_Factory(Provider<TeacherAssignmentsProvider> assignmentsProvider,
      Provider<SessionMarksRepository> marksRepositoryProvider,
      Provider<AcademicSessionRepository> sessionRepositoryProvider,
      Provider<CurriculumRepository> curriculumRepositoryProvider) {
    this.assignmentsProvider = assignmentsProvider;
    this.marksRepositoryProvider = marksRepositoryProvider;
    this.sessionRepositoryProvider = sessionRepositoryProvider;
    this.curriculumRepositoryProvider = curriculumRepositoryProvider;
  }

  @Override
  public SemesterResultsViewModel get() {
    return newInstance(assignmentsProvider.get(), marksRepositoryProvider.get(), sessionRepositoryProvider.get(), curriculumRepositoryProvider.get());
  }

  public static SemesterResultsViewModel_Factory create(
      Provider<TeacherAssignmentsProvider> assignmentsProvider,
      Provider<SessionMarksRepository> marksRepositoryProvider,
      Provider<AcademicSessionRepository> sessionRepositoryProvider,
      Provider<CurriculumRepository> curriculumRepositoryProvider) {
    return new SemesterResultsViewModel_Factory(assignmentsProvider, marksRepositoryProvider, sessionRepositoryProvider, curriculumRepositoryProvider);
  }

  public static SemesterResultsViewModel newInstance(TeacherAssignmentsProvider assignmentsProvider,
      SessionMarksRepository marksRepository, AcademicSessionRepository sessionRepository,
      CurriculumRepository curriculumRepository) {
    return new SemesterResultsViewModel(assignmentsProvider, marksRepository, sessionRepository, curriculumRepository);
  }
}
