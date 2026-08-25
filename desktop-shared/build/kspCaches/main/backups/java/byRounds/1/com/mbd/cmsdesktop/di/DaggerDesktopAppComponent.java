package com.mbd.cmsdesktop.di;

import androidx.datastore.core.DataStore;
import androidx.datastore.preferences.core.Preferences;
import com.mbd.cmscommon.auth.AdminUserProvisioner;
import com.mbd.cmscommon.auth.AdminUserProvisioner_Factory;
import com.mbd.cmscommon.auth.SessionManager;
import com.mbd.cmscommon.auth.SessionManager_Factory;
import com.mbd.cmscommon.data.repository.AdministratorRepositoryImpl;
import com.mbd.cmscommon.data.repository.AdministratorRepositoryImpl_Factory;
import com.mbd.cmscommon.data.repository.CalendarRepositoryImpl;
import com.mbd.cmscommon.data.repository.CalendarRepositoryImpl_Factory;
import com.mbd.cmscommon.data.repository.DatesheetRepositoryImpl;
import com.mbd.cmscommon.data.repository.DatesheetRepositoryImpl_Factory;
import com.mbd.cmscommon.data.repository.FineRepositoryImpl;
import com.mbd.cmscommon.data.repository.FineRepositoryImpl_Factory;
import com.mbd.cmscommon.data.repository.InsightsRepositoryImpl;
import com.mbd.cmscommon.data.repository.InsightsRepositoryImpl_Factory;
import com.mbd.cmscommon.data.repository.MarkEditRequestRepositoryImpl;
import com.mbd.cmscommon.data.repository.MarkEditRequestRepositoryImpl_Factory;
import com.mbd.cmscommon.data.sync.AdminDataBootstrapper;
import com.mbd.cmscommon.data.sync.AdminDataBootstrapper_Factory;
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository;
import com.mbd.cmscommon.domain.repository.AdministratorRepository;
import com.mbd.cmscommon.domain.repository.CalendarRepository;
import com.mbd.cmscommon.domain.repository.CurriculumRepository;
import com.mbd.cmscommon.domain.repository.DatesheetRepository;
import com.mbd.cmscommon.domain.repository.DepartmentRepository;
import com.mbd.cmscommon.domain.repository.DocumentRepository;
import com.mbd.cmscommon.domain.repository.ExamPaperSubmissionRepository;
import com.mbd.cmscommon.domain.repository.FineRepository;
import com.mbd.cmscommon.domain.repository.InsightsRepository;
import com.mbd.cmscommon.domain.repository.MarkEditRequestRepository;
import com.mbd.cmscommon.domain.repository.NotificationRepository;
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository;
import com.mbd.cmscommon.domain.repository.SessionFeeRepository;
import com.mbd.cmscommon.domain.repository.SessionMarksRepository;
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository;
import com.mbd.cmscommon.domain.repository.StudentLinkRequestRepository;
import com.mbd.cmscommon.domain.repository.TeacherRepository;
import com.mbd.cmscommon.domain.repository.UserRepository;
import com.mbd.cmsdesktop.auth.DesktopRoleResolver;
import com.mbd.cmsdesktop.auth.DesktopRoleResolver_Factory;
import com.mbd.cmsdesktop.data.cache.DesktopBootstrapSnapshotStore;
import com.mbd.cmsdesktop.data.repository.DesktopAcademicSessionRepositoryImpl;
import com.mbd.cmsdesktop.data.repository.DesktopAcademicSessionRepositoryImpl_Factory;
import com.mbd.cmsdesktop.data.repository.DesktopCurriculumRepositoryImpl;
import com.mbd.cmsdesktop.data.repository.DesktopCurriculumRepositoryImpl_Factory;
import com.mbd.cmsdesktop.data.repository.DesktopDepartmentRepositoryImpl;
import com.mbd.cmsdesktop.data.repository.DesktopDepartmentRepositoryImpl_Factory;
import com.mbd.cmsdesktop.data.repository.DesktopDocumentRepository;
import com.mbd.cmsdesktop.data.repository.DesktopDocumentRepository_Factory;
import com.mbd.cmsdesktop.data.repository.DesktopExamPaperSubmissionRepository;
import com.mbd.cmsdesktop.data.repository.DesktopExamPaperSubmissionRepository_Factory;
import com.mbd.cmsdesktop.data.repository.DesktopNotificationRepositoryImpl;
import com.mbd.cmsdesktop.data.repository.DesktopNotificationRepositoryImpl_Factory;
import com.mbd.cmsdesktop.data.repository.DesktopSessionAttendanceRepositoryImpl;
import com.mbd.cmsdesktop.data.repository.DesktopSessionAttendanceRepositoryImpl_Factory;
import com.mbd.cmsdesktop.data.repository.DesktopSessionFeeRepositoryImpl;
import com.mbd.cmsdesktop.data.repository.DesktopSessionFeeRepositoryImpl_Factory;
import com.mbd.cmsdesktop.data.repository.DesktopSessionMarksRepositoryImpl;
import com.mbd.cmsdesktop.data.repository.DesktopSessionMarksRepositoryImpl_Factory;
import com.mbd.cmsdesktop.data.repository.DesktopSessionTimetableRepositoryImpl;
import com.mbd.cmsdesktop.data.repository.DesktopSessionTimetableRepositoryImpl_Factory;
import com.mbd.cmsdesktop.data.repository.DesktopStudentLinkRequestRepositoryImpl;
import com.mbd.cmsdesktop.data.repository.DesktopStudentLinkRequestRepositoryImpl_Factory;
import com.mbd.cmsdesktop.data.repository.DesktopTeacherRepositoryImpl;
import com.mbd.cmsdesktop.data.repository.DesktopTeacherRepositoryImpl_Factory;
import com.mbd.cmsdesktop.data.repository.DesktopUserRepositoryImpl;
import com.mbd.cmsdesktop.data.repository.DesktopUserRepositoryImpl_Factory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.Provider;
import io.github.jan.supabase.SupabaseClient;
import io.github.jan.supabase.auth.Auth;
import io.github.jan.supabase.functions.Functions;
import io.github.jan.supabase.postgrest.Postgrest;
import io.github.jan.supabase.storage.Storage;
import javax.annotation.processing.Generated;

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
public final class DaggerDesktopAppComponent {
  private DaggerDesktopAppComponent() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static DesktopAppComponent create() {
    return new Builder().build();
  }

  public static final class Builder {
    private Builder() {
    }

    public DesktopAppComponent build() {
      return new DesktopAppComponentImpl();
    }
  }

  private static final class DesktopAppComponentImpl implements DesktopAppComponent {
    private final DesktopAppComponentImpl desktopAppComponentImpl = this;

    private Provider<SupabaseClient> provideSupabaseClientProvider;

    private Provider<Postgrest> providePostgrestProvider;

    private Provider<Functions> provideFunctionsProvider;

    private Provider<AdminUserProvisioner> adminUserProvisionerProvider;

    private Provider<AdministratorRepositoryImpl> administratorRepositoryImplProvider;

    private Provider<DesktopBootstrapSnapshotStore> provideBootstrapSnapshotStoreProvider;

    private Provider<DesktopDepartmentRepositoryImpl> desktopDepartmentRepositoryImplProvider;

    private Provider<DesktopTeacherRepositoryImpl> desktopTeacherRepositoryImplProvider;

    private Provider<DesktopAcademicSessionRepositoryImpl> desktopAcademicSessionRepositoryImplProvider;

    private Provider<DesktopCurriculumRepositoryImpl> desktopCurriculumRepositoryImplProvider;

    private Provider<DesktopSessionTimetableRepositoryImpl> desktopSessionTimetableRepositoryImplProvider;

    private Provider<DesktopSessionAttendanceRepositoryImpl> desktopSessionAttendanceRepositoryImplProvider;

    private Provider<DesktopSessionMarksRepositoryImpl> desktopSessionMarksRepositoryImplProvider;

    private Provider<DesktopStudentLinkRequestRepositoryImpl> desktopStudentLinkRequestRepositoryImplProvider;

    private Provider<DataStore<Preferences>> provideDataStoreProvider;

    private Provider<DesktopNotificationRepositoryImpl> desktopNotificationRepositoryImplProvider;

    private Provider<AdminDataBootstrapper> adminDataBootstrapperProvider;

    private Provider<Auth> provideAuthProvider;

    private Provider<SessionManager> sessionManagerProvider;

    private Provider<DesktopRoleResolver> desktopRoleResolverProvider;

    private Provider<DatesheetRepositoryImpl> datesheetRepositoryImplProvider;

    private Provider<DatesheetRepository> bindDatesheetRepositoryProvider;

    private Provider<DesktopUserRepositoryImpl> desktopUserRepositoryImplProvider;

    private Provider<InsightsRepositoryImpl> insightsRepositoryImplProvider;

    private Provider<InsightsRepository> bindInsightsRepositoryProvider;

    private Provider<Storage> provideStorageProvider;

    private Provider<DesktopDocumentRepository> desktopDocumentRepositoryProvider;

    private Provider<DesktopExamPaperSubmissionRepository> desktopExamPaperSubmissionRepositoryProvider;

    private Provider<MarkEditRequestRepositoryImpl> markEditRequestRepositoryImplProvider;

    private Provider<MarkEditRequestRepository> bindMarkEditRequestRepositoryProvider;

    private Provider<CalendarRepositoryImpl> calendarRepositoryImplProvider;

    private Provider<CalendarRepository> bindCalendarRepositoryProvider;

    private Provider<DesktopSessionFeeRepositoryImpl> desktopSessionFeeRepositoryImplProvider;

    private Provider<FineRepositoryImpl> fineRepositoryImplProvider;

    private Provider<FineRepository> bindFineRepositoryProvider;

    private DesktopAppComponentImpl() {

      initialize();
      initialize2();

    }

    @SuppressWarnings("unchecked")
    private void initialize() {
      this.provideSupabaseClientProvider = DoubleCheck.provider(SupabaseModule_ProvideSupabaseClientFactory.create());
      this.providePostgrestProvider = DoubleCheck.provider(SupabaseModule_ProvidePostgrestFactory.create(provideSupabaseClientProvider));
      this.provideFunctionsProvider = DoubleCheck.provider(SupabaseModule_ProvideFunctionsFactory.create(provideSupabaseClientProvider));
      this.adminUserProvisionerProvider = AdminUserProvisioner_Factory.create(provideFunctionsProvider);
      this.administratorRepositoryImplProvider = DoubleCheck.provider(AdministratorRepositoryImpl_Factory.create(providePostgrestProvider, adminUserProvisionerProvider));
      this.provideBootstrapSnapshotStoreProvider = DoubleCheck.provider(PreferencesModule_ProvideBootstrapSnapshotStoreFactory.create());
      this.desktopDepartmentRepositoryImplProvider = DoubleCheck.provider(DesktopDepartmentRepositoryImpl_Factory.create(providePostgrestProvider, provideBootstrapSnapshotStoreProvider));
      this.desktopTeacherRepositoryImplProvider = DoubleCheck.provider(DesktopTeacherRepositoryImpl_Factory.create(providePostgrestProvider, adminUserProvisionerProvider, provideBootstrapSnapshotStoreProvider));
      this.desktopAcademicSessionRepositoryImplProvider = DoubleCheck.provider(DesktopAcademicSessionRepositoryImpl_Factory.create(providePostgrestProvider, provideBootstrapSnapshotStoreProvider));
      this.desktopCurriculumRepositoryImplProvider = DoubleCheck.provider(DesktopCurriculumRepositoryImpl_Factory.create(providePostgrestProvider));
      this.desktopSessionTimetableRepositoryImplProvider = DoubleCheck.provider(DesktopSessionTimetableRepositoryImpl_Factory.create(providePostgrestProvider));
      this.desktopSessionAttendanceRepositoryImplProvider = DoubleCheck.provider(DesktopSessionAttendanceRepositoryImpl_Factory.create(providePostgrestProvider));
      this.desktopSessionMarksRepositoryImplProvider = DoubleCheck.provider(DesktopSessionMarksRepositoryImpl_Factory.create(providePostgrestProvider));
      this.desktopStudentLinkRequestRepositoryImplProvider = DoubleCheck.provider(DesktopStudentLinkRequestRepositoryImpl_Factory.create(providePostgrestProvider));
      this.provideDataStoreProvider = DoubleCheck.provider(PreferencesModule_ProvideDataStoreFactory.create());
      this.desktopNotificationRepositoryImplProvider = DoubleCheck.provider(DesktopNotificationRepositoryImpl_Factory.create(providePostgrestProvider, provideDataStoreProvider));
      this.adminDataBootstrapperProvider = DoubleCheck.provider(AdminDataBootstrapper_Factory.create(((Provider) administratorRepositoryImplProvider), ((Provider) desktopDepartmentRepositoryImplProvider), ((Provider) desktopTeacherRepositoryImplProvider), ((Provider) desktopAcademicSessionRepositoryImplProvider), ((Provider) desktopCurriculumRepositoryImplProvider), ((Provider) desktopSessionTimetableRepositoryImplProvider), ((Provider) desktopSessionAttendanceRepositoryImplProvider), ((Provider) desktopSessionMarksRepositoryImplProvider), ((Provider) desktopStudentLinkRequestRepositoryImplProvider), ((Provider) desktopNotificationRepositoryImplProvider)));
      this.provideAuthProvider = DoubleCheck.provider(SupabaseModule_ProvideAuthFactory.create(provideSupabaseClientProvider));
      this.sessionManagerProvider = DoubleCheck.provider(SessionManager_Factory.create(provideAuthProvider));
      this.desktopRoleResolverProvider = DoubleCheck.provider(DesktopRoleResolver_Factory.create(providePostgrestProvider));
      this.datesheetRepositoryImplProvider = DatesheetRepositoryImpl_Factory.create(providePostgrestProvider);
      this.bindDatesheetRepositoryProvider = DoubleCheck.provider((Provider) datesheetRepositoryImplProvider);
      this.desktopUserRepositoryImplProvider = DoubleCheck.provider(DesktopUserRepositoryImpl_Factory.create(providePostgrestProvider, desktopRoleResolverProvider, provideBootstrapSnapshotStoreProvider));
      this.insightsRepositoryImplProvider = InsightsRepositoryImpl_Factory.create(providePostgrestProvider);
      this.bindInsightsRepositoryProvider = DoubleCheck.provider((Provider) insightsRepositoryImplProvider);
    }

    @SuppressWarnings("unchecked")
    private void initialize2() {
      this.provideStorageProvider = DoubleCheck.provider(SupabaseModule_ProvideStorageFactory.create(provideSupabaseClientProvider));
      this.desktopDocumentRepositoryProvider = DoubleCheck.provider(DesktopDocumentRepository_Factory.create(providePostgrestProvider, provideStorageProvider));
      this.desktopExamPaperSubmissionRepositoryProvider = DoubleCheck.provider(DesktopExamPaperSubmissionRepository_Factory.create(providePostgrestProvider, provideStorageProvider));
      this.markEditRequestRepositoryImplProvider = MarkEditRequestRepositoryImpl_Factory.create(providePostgrestProvider);
      this.bindMarkEditRequestRepositoryProvider = DoubleCheck.provider((Provider) markEditRequestRepositoryImplProvider);
      this.calendarRepositoryImplProvider = CalendarRepositoryImpl_Factory.create(providePostgrestProvider);
      this.bindCalendarRepositoryProvider = DoubleCheck.provider((Provider) calendarRepositoryImplProvider);
      this.desktopSessionFeeRepositoryImplProvider = DoubleCheck.provider(DesktopSessionFeeRepositoryImpl_Factory.create(providePostgrestProvider));
      this.fineRepositoryImplProvider = FineRepositoryImpl_Factory.create(providePostgrestProvider);
      this.bindFineRepositoryProvider = DoubleCheck.provider((Provider) fineRepositoryImplProvider);
    }

    @Override
    public AdminDataBootstrapper adminDataBootstrapper() {
      return adminDataBootstrapperProvider.get();
    }

    @Override
    public DesktopBootstrapSnapshotStore bootstrapSnapshotStore() {
      return provideBootstrapSnapshotStoreProvider.get();
    }

    @Override
    public Auth auth() {
      return provideAuthProvider.get();
    }

    @Override
    public SessionManager sessionManager() {
      return sessionManagerProvider.get();
    }

    @Override
    public DesktopRoleResolver roleResolver() {
      return desktopRoleResolverProvider.get();
    }

    @Override
    public DatesheetRepository datesheetRepository() {
      return bindDatesheetRepositoryProvider.get();
    }

    @Override
    public UserRepository userRepository() {
      return desktopUserRepositoryImplProvider.get();
    }

    @Override
    public AdministratorRepository administratorRepository() {
      return administratorRepositoryImplProvider.get();
    }

    @Override
    public NotificationRepository notificationRepository() {
      return desktopNotificationRepositoryImplProvider.get();
    }

    @Override
    public InsightsRepository insightsRepository() {
      return bindInsightsRepositoryProvider.get();
    }

    @Override
    public DocumentRepository documentRepository() {
      return desktopDocumentRepositoryProvider.get();
    }

    @Override
    public ExamPaperSubmissionRepository examPaperRepository() {
      return desktopExamPaperSubmissionRepositoryProvider.get();
    }

    @Override
    public TeacherRepository teacherRepository() {
      return desktopTeacherRepositoryImplProvider.get();
    }

    @Override
    public DepartmentRepository departmentRepository() {
      return desktopDepartmentRepositoryImplProvider.get();
    }

    @Override
    public MarkEditRequestRepository markEditRequestRepository() {
      return bindMarkEditRequestRepositoryProvider.get();
    }

    @Override
    public CalendarRepository calendarRepository() {
      return bindCalendarRepositoryProvider.get();
    }

    @Override
    public AcademicSessionRepository academicSessionRepository() {
      return desktopAcademicSessionRepositoryImplProvider.get();
    }

    @Override
    public CurriculumRepository curriculumRepository() {
      return desktopCurriculumRepositoryImplProvider.get();
    }

    @Override
    public SessionFeeRepository sessionFeeRepository() {
      return desktopSessionFeeRepositoryImplProvider.get();
    }

    @Override
    public SessionTimetableRepository sessionTimetableRepository() {
      return desktopSessionTimetableRepositoryImplProvider.get();
    }

    @Override
    public StudentLinkRequestRepository studentLinkRequestRepository() {
      return desktopStudentLinkRequestRepositoryImplProvider.get();
    }

    @Override
    public SessionAttendanceRepository sessionAttendanceRepository() {
      return desktopSessionAttendanceRepositoryImplProvider.get();
    }

    @Override
    public SessionMarksRepository sessionMarksRepository() {
      return desktopSessionMarksRepositoryImplProvider.get();
    }

    @Override
    public FineRepository fineRepository() {
      return bindFineRepositoryProvider.get();
    }
  }
}
