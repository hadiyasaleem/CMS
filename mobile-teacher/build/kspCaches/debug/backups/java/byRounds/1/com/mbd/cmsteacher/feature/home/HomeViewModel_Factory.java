package com.mbd.cmsteacher.feature.home;

import com.mbd.cmscommon.auth.SessionManager;
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<TeacherAssignmentsProvider> assignmentsProvider;

  private final Provider<SessionTimetableRepository> timetableRepositoryProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  public HomeViewModel_Factory(Provider<TeacherAssignmentsProvider> assignmentsProvider,
      Provider<SessionTimetableRepository> timetableRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.assignmentsProvider = assignmentsProvider;
    this.timetableRepositoryProvider = timetableRepositoryProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(assignmentsProvider.get(), timetableRepositoryProvider.get(), sessionManagerProvider.get());
  }

  public static HomeViewModel_Factory create(
      Provider<TeacherAssignmentsProvider> assignmentsProvider,
      Provider<SessionTimetableRepository> timetableRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new HomeViewModel_Factory(assignmentsProvider, timetableRepositoryProvider, sessionManagerProvider);
  }

  public static HomeViewModel newInstance(TeacherAssignmentsProvider assignmentsProvider,
      SessionTimetableRepository timetableRepository, SessionManager sessionManager) {
    return new HomeViewModel(assignmentsProvider, timetableRepository, sessionManager);
  }
}
