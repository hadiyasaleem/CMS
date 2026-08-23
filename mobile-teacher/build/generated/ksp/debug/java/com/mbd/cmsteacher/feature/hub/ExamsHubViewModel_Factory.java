package com.mbd.cmsteacher.feature.hub;

import com.mbd.cmscommon.auth.SessionManager;
import com.mbd.cmscommon.domain.repository.DatesheetRepository;
import com.mbd.cmscommon.domain.repository.ExamPaperSubmissionRepository;
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
public final class ExamsHubViewModel_Factory implements Factory<ExamsHubViewModel> {
  private final Provider<SessionManager> sessionManagerProvider;

  private final Provider<TeacherAssignmentsProvider> assignmentsProvider;

  private final Provider<ExamPaperSubmissionRepository> examPaperRepositoryProvider;

  private final Provider<DatesheetRepository> datesheetRepositoryProvider;

  public ExamsHubViewModel_Factory(Provider<SessionManager> sessionManagerProvider,
      Provider<TeacherAssignmentsProvider> assignmentsProvider,
      Provider<ExamPaperSubmissionRepository> examPaperRepositoryProvider,
      Provider<DatesheetRepository> datesheetRepositoryProvider) {
    this.sessionManagerProvider = sessionManagerProvider;
    this.assignmentsProvider = assignmentsProvider;
    this.examPaperRepositoryProvider = examPaperRepositoryProvider;
    this.datesheetRepositoryProvider = datesheetRepositoryProvider;
  }

  @Override
  public ExamsHubViewModel get() {
    return newInstance(sessionManagerProvider.get(), assignmentsProvider.get(), examPaperRepositoryProvider.get(), datesheetRepositoryProvider.get());
  }

  public static ExamsHubViewModel_Factory create(Provider<SessionManager> sessionManagerProvider,
      Provider<TeacherAssignmentsProvider> assignmentsProvider,
      Provider<ExamPaperSubmissionRepository> examPaperRepositoryProvider,
      Provider<DatesheetRepository> datesheetRepositoryProvider) {
    return new ExamsHubViewModel_Factory(sessionManagerProvider, assignmentsProvider, examPaperRepositoryProvider, datesheetRepositoryProvider);
  }

  public static ExamsHubViewModel newInstance(SessionManager sessionManager,
      TeacherAssignmentsProvider assignmentsProvider,
      ExamPaperSubmissionRepository examPaperRepository, DatesheetRepository datesheetRepository) {
    return new ExamsHubViewModel(sessionManager, assignmentsProvider, examPaperRepository, datesheetRepository);
  }
}
