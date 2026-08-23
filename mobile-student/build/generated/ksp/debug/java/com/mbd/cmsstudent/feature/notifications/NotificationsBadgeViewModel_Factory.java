package com.mbd.cmsstudent.feature.notifications;

import com.mbd.cmscommon.domain.repository.NotificationRepository;
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
public final class NotificationsBadgeViewModel_Factory implements Factory<NotificationsBadgeViewModel> {
  private final Provider<NotificationRepository> repositoryProvider;

  private final Provider<CurrentStudentProvider> currentStudentProvider;

  public NotificationsBadgeViewModel_Factory(Provider<NotificationRepository> repositoryProvider,
      Provider<CurrentStudentProvider> currentStudentProvider) {
    this.repositoryProvider = repositoryProvider;
    this.currentStudentProvider = currentStudentProvider;
  }

  @Override
  public NotificationsBadgeViewModel get() {
    return newInstance(repositoryProvider.get(), currentStudentProvider.get());
  }

  public static NotificationsBadgeViewModel_Factory create(
      Provider<NotificationRepository> repositoryProvider,
      Provider<CurrentStudentProvider> currentStudentProvider) {
    return new NotificationsBadgeViewModel_Factory(repositoryProvider, currentStudentProvider);
  }

  public static NotificationsBadgeViewModel newInstance(NotificationRepository repository,
      CurrentStudentProvider currentStudentProvider) {
    return new NotificationsBadgeViewModel(repository, currentStudentProvider);
  }
}
