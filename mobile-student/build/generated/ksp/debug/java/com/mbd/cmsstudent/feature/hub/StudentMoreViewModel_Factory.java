package com.mbd.cmsstudent.feature.hub;

import com.mbd.cmscommon.domain.repository.AcademicSessionRepository;
import com.mbd.cmscommon.domain.repository.CalendarRepository;
import com.mbd.cmscommon.domain.repository.DocumentRepository;
import com.mbd.cmscommon.domain.repository.NotificationRepository;
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
public final class StudentMoreViewModel_Factory implements Factory<StudentMoreViewModel> {
  private final Provider<CurrentStudentProvider> currentStudentProvider;

  private final Provider<CalendarRepository> calendarRepositoryProvider;

  private final Provider<DocumentRepository> documentRepositoryProvider;

  private final Provider<SessionFeeRepository> feeRepositoryProvider;

  private final Provider<NotificationRepository> notificationRepositoryProvider;

  private final Provider<AcademicSessionRepository> sessionRepositoryProvider;

  public StudentMoreViewModel_Factory(Provider<CurrentStudentProvider> currentStudentProvider,
      Provider<CalendarRepository> calendarRepositoryProvider,
      Provider<DocumentRepository> documentRepositoryProvider,
      Provider<SessionFeeRepository> feeRepositoryProvider,
      Provider<NotificationRepository> notificationRepositoryProvider,
      Provider<AcademicSessionRepository> sessionRepositoryProvider) {
    this.currentStudentProvider = currentStudentProvider;
    this.calendarRepositoryProvider = calendarRepositoryProvider;
    this.documentRepositoryProvider = documentRepositoryProvider;
    this.feeRepositoryProvider = feeRepositoryProvider;
    this.notificationRepositoryProvider = notificationRepositoryProvider;
    this.sessionRepositoryProvider = sessionRepositoryProvider;
  }

  @Override
  public StudentMoreViewModel get() {
    return newInstance(currentStudentProvider.get(), calendarRepositoryProvider.get(), documentRepositoryProvider.get(), feeRepositoryProvider.get(), notificationRepositoryProvider.get(), sessionRepositoryProvider.get());
  }

  public static StudentMoreViewModel_Factory create(
      Provider<CurrentStudentProvider> currentStudentProvider,
      Provider<CalendarRepository> calendarRepositoryProvider,
      Provider<DocumentRepository> documentRepositoryProvider,
      Provider<SessionFeeRepository> feeRepositoryProvider,
      Provider<NotificationRepository> notificationRepositoryProvider,
      Provider<AcademicSessionRepository> sessionRepositoryProvider) {
    return new StudentMoreViewModel_Factory(currentStudentProvider, calendarRepositoryProvider, documentRepositoryProvider, feeRepositoryProvider, notificationRepositoryProvider, sessionRepositoryProvider);
  }

  public static StudentMoreViewModel newInstance(CurrentStudentProvider currentStudentProvider,
      CalendarRepository calendarRepository, DocumentRepository documentRepository,
      SessionFeeRepository feeRepository, NotificationRepository notificationRepository,
      AcademicSessionRepository sessionRepository) {
    return new StudentMoreViewModel(currentStudentProvider, calendarRepository, documentRepository, feeRepository, notificationRepository, sessionRepository);
  }
}
