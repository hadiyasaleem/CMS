package com.mbd.cmsstudent.feature.results;

import com.mbd.cmscommon.domain.repository.SessionMarksRepository;
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
public final class ResultsViewModel_Factory implements Factory<ResultsViewModel> {
  private final Provider<SessionMarksRepository> marksRepositoryProvider;

  private final Provider<CurrentStudentProvider> currentStudentProvider;

  public ResultsViewModel_Factory(Provider<SessionMarksRepository> marksRepositoryProvider,
      Provider<CurrentStudentProvider> currentStudentProvider) {
    this.marksRepositoryProvider = marksRepositoryProvider;
    this.currentStudentProvider = currentStudentProvider;
  }

  @Override
  public ResultsViewModel get() {
    return newInstance(marksRepositoryProvider.get(), currentStudentProvider.get());
  }

  public static ResultsViewModel_Factory create(
      Provider<SessionMarksRepository> marksRepositoryProvider,
      Provider<CurrentStudentProvider> currentStudentProvider) {
    return new ResultsViewModel_Factory(marksRepositoryProvider, currentStudentProvider);
  }

  public static ResultsViewModel newInstance(SessionMarksRepository marksRepository,
      CurrentStudentProvider currentStudentProvider) {
    return new ResultsViewModel(marksRepository, currentStudentProvider);
  }
}
