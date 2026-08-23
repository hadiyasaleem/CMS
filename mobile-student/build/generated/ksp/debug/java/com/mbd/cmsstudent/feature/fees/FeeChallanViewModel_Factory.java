package com.mbd.cmsstudent.feature.fees;

import com.mbd.cmscommon.domain.repository.SessionFeeRepository;
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
public final class FeeChallanViewModel_Factory implements Factory<FeeChallanViewModel> {
  private final Provider<CurrentStudentProvider> currentStudentProvider;

  private final Provider<SessionFeeRepository> sessionFeeRepositoryProvider;

  public FeeChallanViewModel_Factory(Provider<CurrentStudentProvider> currentStudentProvider,
      Provider<SessionFeeRepository> sessionFeeRepositoryProvider) {
    this.currentStudentProvider = currentStudentProvider;
    this.sessionFeeRepositoryProvider = sessionFeeRepositoryProvider;
  }

  @Override
  public FeeChallanViewModel get() {
    return newInstance(currentStudentProvider.get(), sessionFeeRepositoryProvider.get());
  }

  public static FeeChallanViewModel_Factory create(
      Provider<CurrentStudentProvider> currentStudentProvider,
      Provider<SessionFeeRepository> sessionFeeRepositoryProvider) {
    return new FeeChallanViewModel_Factory(currentStudentProvider, sessionFeeRepositoryProvider);
  }

  public static FeeChallanViewModel newInstance(CurrentStudentProvider currentStudentProvider,
      SessionFeeRepository sessionFeeRepository) {
    return new FeeChallanViewModel(currentStudentProvider, sessionFeeRepository);
  }
}
