package com.mbd.cmsstudent.feature.timetable;

import com.mbd.cmscommon.domain.repository.SessionTimetableRepository;
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
public final class MyTimetableViewModel_Factory implements Factory<MyTimetableViewModel> {
  private final Provider<SessionTimetableRepository> timetableRepositoryProvider;

  private final Provider<CurrentStudentProvider> currentStudentProvider;

  public MyTimetableViewModel_Factory(
      Provider<SessionTimetableRepository> timetableRepositoryProvider,
      Provider<CurrentStudentProvider> currentStudentProvider) {
    this.timetableRepositoryProvider = timetableRepositoryProvider;
    this.currentStudentProvider = currentStudentProvider;
  }

  @Override
  public MyTimetableViewModel get() {
    return newInstance(timetableRepositoryProvider.get(), currentStudentProvider.get());
  }

  public static MyTimetableViewModel_Factory create(
      Provider<SessionTimetableRepository> timetableRepositoryProvider,
      Provider<CurrentStudentProvider> currentStudentProvider) {
    return new MyTimetableViewModel_Factory(timetableRepositoryProvider, currentStudentProvider);
  }

  public static MyTimetableViewModel newInstance(SessionTimetableRepository timetableRepository,
      CurrentStudentProvider currentStudentProvider) {
    return new MyTimetableViewModel(timetableRepository, currentStudentProvider);
  }
}
