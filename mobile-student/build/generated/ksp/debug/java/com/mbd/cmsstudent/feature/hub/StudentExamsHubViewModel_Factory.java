package com.mbd.cmsstudent.feature.hub;

import com.mbd.cmscommon.domain.repository.DatesheetRepository;
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
public final class StudentExamsHubViewModel_Factory implements Factory<StudentExamsHubViewModel> {
  private final Provider<CurrentStudentProvider> currentStudentProvider;

  private final Provider<SessionMarksRepository> marksRepositoryProvider;

  private final Provider<DatesheetRepository> datesheetRepositoryProvider;

  public StudentExamsHubViewModel_Factory(Provider<CurrentStudentProvider> currentStudentProvider,
      Provider<SessionMarksRepository> marksRepositoryProvider,
      Provider<DatesheetRepository> datesheetRepositoryProvider) {
    this.currentStudentProvider = currentStudentProvider;
    this.marksRepositoryProvider = marksRepositoryProvider;
    this.datesheetRepositoryProvider = datesheetRepositoryProvider;
  }

  @Override
  public StudentExamsHubViewModel get() {
    return newInstance(currentStudentProvider.get(), marksRepositoryProvider.get(), datesheetRepositoryProvider.get());
  }

  public static StudentExamsHubViewModel_Factory create(
      Provider<CurrentStudentProvider> currentStudentProvider,
      Provider<SessionMarksRepository> marksRepositoryProvider,
      Provider<DatesheetRepository> datesheetRepositoryProvider) {
    return new StudentExamsHubViewModel_Factory(currentStudentProvider, marksRepositoryProvider, datesheetRepositoryProvider);
  }

  public static StudentExamsHubViewModel newInstance(CurrentStudentProvider currentStudentProvider,
      SessionMarksRepository marksRepository, DatesheetRepository datesheetRepository) {
    return new StudentExamsHubViewModel(currentStudentProvider, marksRepository, datesheetRepository);
  }
}
