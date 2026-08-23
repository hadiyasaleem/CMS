package com.mbd.cmsteacher.feature.exampaper;

import android.content.Context;
import com.mbd.cmscommon.auth.SessionManager;
import com.mbd.cmscommon.domain.repository.ExamPaperSubmissionRepository;
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class ExamPaperSubmissionViewModel_Factory implements Factory<ExamPaperSubmissionViewModel> {
  private final Provider<TeacherAssignmentsProvider> assignmentsProvider;

  private final Provider<ExamPaperSubmissionRepository> examPaperRepositoryProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  private final Provider<Context> contextProvider;

  public ExamPaperSubmissionViewModel_Factory(
      Provider<TeacherAssignmentsProvider> assignmentsProvider,
      Provider<ExamPaperSubmissionRepository> examPaperRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider, Provider<Context> contextProvider) {
    this.assignmentsProvider = assignmentsProvider;
    this.examPaperRepositoryProvider = examPaperRepositoryProvider;
    this.sessionManagerProvider = sessionManagerProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public ExamPaperSubmissionViewModel get() {
    return newInstance(assignmentsProvider.get(), examPaperRepositoryProvider.get(), sessionManagerProvider.get(), contextProvider.get());
  }

  public static ExamPaperSubmissionViewModel_Factory create(
      Provider<TeacherAssignmentsProvider> assignmentsProvider,
      Provider<ExamPaperSubmissionRepository> examPaperRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider, Provider<Context> contextProvider) {
    return new ExamPaperSubmissionViewModel_Factory(assignmentsProvider, examPaperRepositoryProvider, sessionManagerProvider, contextProvider);
  }

  public static ExamPaperSubmissionViewModel newInstance(
      TeacherAssignmentsProvider assignmentsProvider,
      ExamPaperSubmissionRepository examPaperRepository, SessionManager sessionManager,
      Context context) {
    return new ExamPaperSubmissionViewModel(assignmentsProvider, examPaperRepository, sessionManager, context);
  }
}
