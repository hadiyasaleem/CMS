package com.mbd.cmsteacher.feature.marks;

import com.mbd.cmscommon.auth.SessionManager;
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository;
import com.mbd.cmscommon.domain.repository.MarkEditRequestRepository;
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
public final class MarksEntryViewModel_Factory implements Factory<MarksEntryViewModel> {
  private final Provider<TeacherAssignmentsProvider> assignmentsProvider;

  private final Provider<SessionMarksRepository> marksRepositoryProvider;

  private final Provider<AcademicSessionRepository> sessionRepositoryProvider;

  private final Provider<MarkEditRequestRepository> markEditRequestRepositoryProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  public MarksEntryViewModel_Factory(Provider<TeacherAssignmentsProvider> assignmentsProvider,
      Provider<SessionMarksRepository> marksRepositoryProvider,
      Provider<AcademicSessionRepository> sessionRepositoryProvider,
      Provider<MarkEditRequestRepository> markEditRequestRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.assignmentsProvider = assignmentsProvider;
    this.marksRepositoryProvider = marksRepositoryProvider;
    this.sessionRepositoryProvider = sessionRepositoryProvider;
    this.markEditRequestRepositoryProvider = markEditRequestRepositoryProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public MarksEntryViewModel get() {
    return newInstance(assignmentsProvider.get(), marksRepositoryProvider.get(), sessionRepositoryProvider.get(), markEditRequestRepositoryProvider.get(), sessionManagerProvider.get());
  }

  public static MarksEntryViewModel_Factory create(
      Provider<TeacherAssignmentsProvider> assignmentsProvider,
      Provider<SessionMarksRepository> marksRepositoryProvider,
      Provider<AcademicSessionRepository> sessionRepositoryProvider,
      Provider<MarkEditRequestRepository> markEditRequestRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new MarksEntryViewModel_Factory(assignmentsProvider, marksRepositoryProvider, sessionRepositoryProvider, markEditRequestRepositoryProvider, sessionManagerProvider);
  }

  public static MarksEntryViewModel newInstance(TeacherAssignmentsProvider assignmentsProvider,
      SessionMarksRepository marksRepository, AcademicSessionRepository sessionRepository,
      MarkEditRequestRepository markEditRequestRepository, SessionManager sessionManager) {
    return new MarksEntryViewModel(assignmentsProvider, marksRepository, sessionRepository, markEditRequestRepository, sessionManager);
  }
}
