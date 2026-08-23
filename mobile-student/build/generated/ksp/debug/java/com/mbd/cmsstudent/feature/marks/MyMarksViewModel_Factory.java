package com.mbd.cmsstudent.feature.marks;

import com.mbd.cmscommon.domain.repository.CurriculumRepository;
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
public final class MyMarksViewModel_Factory implements Factory<MyMarksViewModel> {
  private final Provider<CurrentStudentProvider> currentStudentProvider;

  private final Provider<SessionMarksRepository> marksRepositoryProvider;

  private final Provider<CurriculumRepository> curriculumRepositoryProvider;

  public MyMarksViewModel_Factory(Provider<CurrentStudentProvider> currentStudentProvider,
      Provider<SessionMarksRepository> marksRepositoryProvider,
      Provider<CurriculumRepository> curriculumRepositoryProvider) {
    this.currentStudentProvider = currentStudentProvider;
    this.marksRepositoryProvider = marksRepositoryProvider;
    this.curriculumRepositoryProvider = curriculumRepositoryProvider;
  }

  @Override
  public MyMarksViewModel get() {
    return newInstance(currentStudentProvider.get(), marksRepositoryProvider.get(), curriculumRepositoryProvider.get());
  }

  public static MyMarksViewModel_Factory create(
      Provider<CurrentStudentProvider> currentStudentProvider,
      Provider<SessionMarksRepository> marksRepositoryProvider,
      Provider<CurriculumRepository> curriculumRepositoryProvider) {
    return new MyMarksViewModel_Factory(currentStudentProvider, marksRepositoryProvider, curriculumRepositoryProvider);
  }

  public static MyMarksViewModel newInstance(CurrentStudentProvider currentStudentProvider,
      SessionMarksRepository marksRepository, CurriculumRepository curriculumRepository) {
    return new MyMarksViewModel(currentStudentProvider, marksRepository, curriculumRepository);
  }
}
