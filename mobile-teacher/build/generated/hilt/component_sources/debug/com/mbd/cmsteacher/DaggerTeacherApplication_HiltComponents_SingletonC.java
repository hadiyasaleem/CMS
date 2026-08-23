package com.mbd.cmsteacher;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.datastore.core.DataStore;
import androidx.datastore.preferences.core.Preferences;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.mbd.cmscommon.auth.AdminUserProvisioner;
import com.mbd.cmscommon.auth.RoleResolver;
import com.mbd.cmscommon.auth.SessionManager;
import com.mbd.cmscommon.data.local.CmsDatabase;
import com.mbd.cmscommon.data.local.dao.AcademicSessionDao;
import com.mbd.cmscommon.data.local.dao.CalendarEventDao;
import com.mbd.cmscommon.data.local.dao.DatesheetDao;
import com.mbd.cmscommon.data.local.dao.DepartmentDao;
import com.mbd.cmscommon.data.local.dao.DocumentDao;
import com.mbd.cmscommon.data.local.dao.ExamPaperSubmissionDao;
import com.mbd.cmscommon.data.local.dao.InsightsDao;
import com.mbd.cmscommon.data.local.dao.MarkEditRequestDao;
import com.mbd.cmscommon.data.local.dao.NotificationDao;
import com.mbd.cmscommon.data.local.dao.SemesterSubjectDao;
import com.mbd.cmscommon.data.local.dao.SessionAttendanceDao;
import com.mbd.cmscommon.data.local.dao.SessionMarkDao;
import com.mbd.cmscommon.data.local.dao.SessionPeriodDao;
import com.mbd.cmscommon.data.local.dao.SessionStudentDao;
import com.mbd.cmscommon.data.local.dao.StudentLinkRequestDao;
import com.mbd.cmscommon.data.local.dao.StudentSemesterGpaDao;
import com.mbd.cmscommon.data.local.dao.SyncStateDao;
import com.mbd.cmscommon.data.local.dao.TableSyncStateDao;
import com.mbd.cmscommon.data.local.dao.TeacherDao;
import com.mbd.cmscommon.data.local.dao.UserDao;
import com.mbd.cmscommon.data.repository.AcademicSessionRepositoryImpl;
import com.mbd.cmscommon.data.repository.CalendarRepositoryLocalImpl;
import com.mbd.cmscommon.data.repository.CurriculumRepositoryImpl;
import com.mbd.cmscommon.data.repository.DatesheetRepositoryLocalImpl;
import com.mbd.cmscommon.data.repository.DepartmentRepositoryImpl;
import com.mbd.cmscommon.data.repository.DocumentRepositoryImpl;
import com.mbd.cmscommon.data.repository.ExamPaperSubmissionRepositoryImpl;
import com.mbd.cmscommon.data.repository.InsightsRepositoryLocalImpl;
import com.mbd.cmscommon.data.repository.MarkEditRequestRepositoryLocalImpl;
import com.mbd.cmscommon.data.repository.NotificationRepositoryImpl;
import com.mbd.cmscommon.data.repository.SessionAttendanceRepositoryImpl;
import com.mbd.cmscommon.data.repository.SessionMarksRepositoryImpl;
import com.mbd.cmscommon.data.repository.SessionTimetableRepositoryImpl;
import com.mbd.cmscommon.data.repository.StudentLinkRequestRepositoryImpl;
import com.mbd.cmscommon.data.repository.TeacherRepositoryImpl;
import com.mbd.cmscommon.data.repository.UserRepositoryImpl;
import com.mbd.cmscommon.data.sync.RoomSyncCheckpointStore;
import com.mbd.cmscommon.data.sync.StartupBootstrapTracker;
import com.mbd.cmscommon.data.sync.SyncCheckpointStore;
import com.mbd.cmscommon.data.sync.SyncEngine;
import com.mbd.cmscommon.di.DaoModule_ProvideAcademicSessionDaoFactory;
import com.mbd.cmscommon.di.DaoModule_ProvideCalendarEventDaoFactory;
import com.mbd.cmscommon.di.DaoModule_ProvideDatesheetDaoFactory;
import com.mbd.cmscommon.di.DaoModule_ProvideDepartmentDaoFactory;
import com.mbd.cmscommon.di.DaoModule_ProvideDocumentDaoFactory;
import com.mbd.cmscommon.di.DaoModule_ProvideExamPaperSubmissionDaoFactory;
import com.mbd.cmscommon.di.DaoModule_ProvideInsightsDaoFactory;
import com.mbd.cmscommon.di.DaoModule_ProvideMarkEditRequestDaoFactory;
import com.mbd.cmscommon.di.DaoModule_ProvideNotificationDaoFactory;
import com.mbd.cmscommon.di.DaoModule_ProvideSemesterSubjectDaoFactory;
import com.mbd.cmscommon.di.DaoModule_ProvideSessionAttendanceDaoFactory;
import com.mbd.cmscommon.di.DaoModule_ProvideSessionMarkDaoFactory;
import com.mbd.cmscommon.di.DaoModule_ProvideSessionPeriodDaoFactory;
import com.mbd.cmscommon.di.DaoModule_ProvideSessionStudentDaoFactory;
import com.mbd.cmscommon.di.DaoModule_ProvideStudentLinkRequestDaoFactory;
import com.mbd.cmscommon.di.DaoModule_ProvideStudentSemesterGpaDaoFactory;
import com.mbd.cmscommon.di.DaoModule_ProvideSyncCheckpointStoreFactory;
import com.mbd.cmscommon.di.DaoModule_ProvideSyncStateDaoFactory;
import com.mbd.cmscommon.di.DaoModule_ProvideTableSyncStateDaoFactory;
import com.mbd.cmscommon.di.DaoModule_ProvideTeacherDaoFactory;
import com.mbd.cmscommon.di.DaoModule_ProvideUserDaoFactory;
import com.mbd.cmscommon.di.DataStoreModule_ProvideDataStoreFactory;
import com.mbd.cmscommon.di.SupabaseModule_ProvideAuthFactory;
import com.mbd.cmscommon.di.SupabaseModule_ProvideFunctionsFactory;
import com.mbd.cmscommon.di.SupabaseModule_ProvidePostgrestFactory;
import com.mbd.cmscommon.di.SupabaseModule_ProvideStorageFactory;
import com.mbd.cmscommon.di.SupabaseModule_ProvideSupabaseClientFactory;
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository;
import com.mbd.cmscommon.domain.repository.CalendarRepository;
import com.mbd.cmscommon.domain.repository.CurriculumRepository;
import com.mbd.cmscommon.domain.repository.DatesheetRepository;
import com.mbd.cmscommon.domain.repository.DepartmentRepository;
import com.mbd.cmscommon.domain.repository.DocumentRepository;
import com.mbd.cmscommon.domain.repository.ExamPaperSubmissionRepository;
import com.mbd.cmscommon.domain.repository.InsightsRepository;
import com.mbd.cmscommon.domain.repository.MarkEditRequestRepository;
import com.mbd.cmscommon.domain.repository.NotificationRepository;
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository;
import com.mbd.cmscommon.domain.repository.SessionMarksRepository;
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository;
import com.mbd.cmscommon.domain.repository.StudentLinkRequestRepository;
import com.mbd.cmscommon.domain.repository.TeacherRepository;
import com.mbd.cmscommon.domain.repository.UserRepository;
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider;
import com.mbd.cmscommon.ui.datesheets.DatesheetsViewModel;
import com.mbd.cmscommon.ui.datesheets.DatesheetsViewModel_HiltModules;
import com.mbd.cmscommon.ui.documents.DocumentsViewModel;
import com.mbd.cmscommon.ui.documents.DocumentsViewModel_HiltModules;
import com.mbd.cmscommon.ui.events.EventsViewModel;
import com.mbd.cmscommon.ui.events.EventsViewModel_HiltModules;
import com.mbd.cmscommon.ui.state.GlobalRefreshViewModel;
import com.mbd.cmscommon.ui.state.GlobalRefreshViewModel_HiltModules;
import com.mbd.cmsteacher.di.DatabaseModule_ProvideTeacherDatabaseFactory;
import com.mbd.cmsteacher.feature.attendance.AttendanceHistoryViewModel;
import com.mbd.cmsteacher.feature.attendance.AttendanceHistoryViewModel_HiltModules;
import com.mbd.cmsteacher.feature.attendance.MarkAttendanceViewModel;
import com.mbd.cmsteacher.feature.attendance.MarkAttendanceViewModel_HiltModules;
import com.mbd.cmsteacher.feature.auth.LoginViewModel;
import com.mbd.cmsteacher.feature.auth.LoginViewModel_HiltModules;
import com.mbd.cmsteacher.feature.exampaper.ExamPaperSubmissionViewModel;
import com.mbd.cmsteacher.feature.exampaper.ExamPaperSubmissionViewModel_HiltModules;
import com.mbd.cmsteacher.feature.home.HomeViewModel;
import com.mbd.cmsteacher.feature.home.HomeViewModel_HiltModules;
import com.mbd.cmsteacher.feature.hub.ExamsHubViewModel;
import com.mbd.cmsteacher.feature.hub.ExamsHubViewModel_HiltModules;
import com.mbd.cmsteacher.feature.hub.MenuViewModel;
import com.mbd.cmsteacher.feature.hub.MenuViewModel_HiltModules;
import com.mbd.cmsteacher.feature.insights.InsightsViewModel;
import com.mbd.cmsteacher.feature.insights.InsightsViewModel_HiltModules;
import com.mbd.cmsteacher.feature.linkrequests.LinkRequestsViewModel;
import com.mbd.cmsteacher.feature.linkrequests.LinkRequestsViewModel_HiltModules;
import com.mbd.cmsteacher.feature.marks.MarksEntryViewModel;
import com.mbd.cmsteacher.feature.marks.MarksEntryViewModel_HiltModules;
import com.mbd.cmsteacher.feature.notifications.NotificationsBadgeViewModel;
import com.mbd.cmsteacher.feature.notifications.NotificationsBadgeViewModel_HiltModules;
import com.mbd.cmsteacher.feature.notifications.NotificationsViewModel;
import com.mbd.cmsteacher.feature.notifications.NotificationsViewModel_HiltModules;
import com.mbd.cmsteacher.feature.profile.ProfileViewModel;
import com.mbd.cmsteacher.feature.profile.ProfileViewModel_HiltModules;
import com.mbd.cmsteacher.feature.results.SemesterResultsViewModel;
import com.mbd.cmsteacher.feature.results.SemesterResultsViewModel_HiltModules;
import com.mbd.cmsteacher.feature.root.AppRootViewModel;
import com.mbd.cmsteacher.feature.root.AppRootViewModel_HiltModules;
import com.mbd.cmsteacher.feature.schedule.ScheduleViewModel;
import com.mbd.cmsteacher.feature.schedule.ScheduleViewModel_HiltModules;
import com.mbd.cmsteacher.feature.students.MyStudentsViewModel;
import com.mbd.cmsteacher.feature.students.MyStudentsViewModel_HiltModules;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.IdentifierNameString;
import dagger.internal.KeepFieldType;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import io.github.jan.supabase.SupabaseClient;
import io.github.jan.supabase.auth.Auth;
import io.github.jan.supabase.functions.Functions;
import io.github.jan.supabase.postgrest.Postgrest;
import io.github.jan.supabase.storage.Storage;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
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
public final class DaggerTeacherApplication_HiltComponents_SingletonC {
  private DaggerTeacherApplication_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public TeacherApplication_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements TeacherApplication_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public TeacherApplication_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements TeacherApplication_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public TeacherApplication_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements TeacherApplication_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public TeacherApplication_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements TeacherApplication_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public TeacherApplication_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements TeacherApplication_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public TeacherApplication_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements TeacherApplication_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public TeacherApplication_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements TeacherApplication_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public TeacherApplication_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends TeacherApplication_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends TeacherApplication_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends TeacherApplication_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends TeacherApplication_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(21).put(LazyClassKeyProvider.com_mbd_cmsteacher_feature_root_AppRootViewModel, AppRootViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_mbd_cmsteacher_feature_attendance_AttendanceHistoryViewModel, AttendanceHistoryViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_mbd_cmscommon_ui_datesheets_DatesheetsViewModel, DatesheetsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_mbd_cmscommon_ui_documents_DocumentsViewModel, DocumentsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_mbd_cmscommon_ui_events_EventsViewModel, EventsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_mbd_cmsteacher_feature_exampaper_ExamPaperSubmissionViewModel, ExamPaperSubmissionViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_mbd_cmsteacher_feature_hub_ExamsHubViewModel, ExamsHubViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_mbd_cmscommon_ui_state_GlobalRefreshViewModel, GlobalRefreshViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_mbd_cmsteacher_feature_home_HomeViewModel, HomeViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_mbd_cmsteacher_feature_insights_InsightsViewModel, InsightsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_mbd_cmsteacher_feature_linkrequests_LinkRequestsViewModel, LinkRequestsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_mbd_cmsteacher_feature_auth_LoginViewModel, LoginViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_mbd_cmsteacher_feature_attendance_MarkAttendanceViewModel, MarkAttendanceViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_mbd_cmsteacher_feature_marks_MarksEntryViewModel, MarksEntryViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_mbd_cmsteacher_feature_hub_MenuViewModel, MenuViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_mbd_cmsteacher_feature_students_MyStudentsViewModel, MyStudentsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_mbd_cmsteacher_feature_notifications_NotificationsBadgeViewModel, NotificationsBadgeViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_mbd_cmsteacher_feature_notifications_NotificationsViewModel, NotificationsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_mbd_cmsteacher_feature_profile_ProfileViewModel, ProfileViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_mbd_cmsteacher_feature_schedule_ScheduleViewModel, ScheduleViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_mbd_cmsteacher_feature_results_SemesterResultsViewModel, SemesterResultsViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_mbd_cmsteacher_feature_notifications_NotificationsBadgeViewModel = "com.mbd.cmsteacher.feature.notifications.NotificationsBadgeViewModel";

      static String com_mbd_cmsteacher_feature_attendance_MarkAttendanceViewModel = "com.mbd.cmsteacher.feature.attendance.MarkAttendanceViewModel";

      static String com_mbd_cmsteacher_feature_root_AppRootViewModel = "com.mbd.cmsteacher.feature.root.AppRootViewModel";

      static String com_mbd_cmsteacher_feature_exampaper_ExamPaperSubmissionViewModel = "com.mbd.cmsteacher.feature.exampaper.ExamPaperSubmissionViewModel";

      static String com_mbd_cmsteacher_feature_hub_MenuViewModel = "com.mbd.cmsteacher.feature.hub.MenuViewModel";

      static String com_mbd_cmsteacher_feature_notifications_NotificationsViewModel = "com.mbd.cmsteacher.feature.notifications.NotificationsViewModel";

      static String com_mbd_cmsteacher_feature_results_SemesterResultsViewModel = "com.mbd.cmsteacher.feature.results.SemesterResultsViewModel";

      static String com_mbd_cmsteacher_feature_insights_InsightsViewModel = "com.mbd.cmsteacher.feature.insights.InsightsViewModel";

      static String com_mbd_cmsteacher_feature_hub_ExamsHubViewModel = "com.mbd.cmsteacher.feature.hub.ExamsHubViewModel";

      static String com_mbd_cmsteacher_feature_linkrequests_LinkRequestsViewModel = "com.mbd.cmsteacher.feature.linkrequests.LinkRequestsViewModel";

      static String com_mbd_cmscommon_ui_datesheets_DatesheetsViewModel = "com.mbd.cmscommon.ui.datesheets.DatesheetsViewModel";

      static String com_mbd_cmsteacher_feature_schedule_ScheduleViewModel = "com.mbd.cmsteacher.feature.schedule.ScheduleViewModel";

      static String com_mbd_cmscommon_ui_events_EventsViewModel = "com.mbd.cmscommon.ui.events.EventsViewModel";

      static String com_mbd_cmscommon_ui_state_GlobalRefreshViewModel = "com.mbd.cmscommon.ui.state.GlobalRefreshViewModel";

      static String com_mbd_cmsteacher_feature_profile_ProfileViewModel = "com.mbd.cmsteacher.feature.profile.ProfileViewModel";

      static String com_mbd_cmsteacher_feature_attendance_AttendanceHistoryViewModel = "com.mbd.cmsteacher.feature.attendance.AttendanceHistoryViewModel";

      static String com_mbd_cmsteacher_feature_marks_MarksEntryViewModel = "com.mbd.cmsteacher.feature.marks.MarksEntryViewModel";

      static String com_mbd_cmsteacher_feature_students_MyStudentsViewModel = "com.mbd.cmsteacher.feature.students.MyStudentsViewModel";

      static String com_mbd_cmscommon_ui_documents_DocumentsViewModel = "com.mbd.cmscommon.ui.documents.DocumentsViewModel";

      static String com_mbd_cmsteacher_feature_auth_LoginViewModel = "com.mbd.cmsteacher.feature.auth.LoginViewModel";

      static String com_mbd_cmsteacher_feature_home_HomeViewModel = "com.mbd.cmsteacher.feature.home.HomeViewModel";

      @KeepFieldType
      NotificationsBadgeViewModel com_mbd_cmsteacher_feature_notifications_NotificationsBadgeViewModel2;

      @KeepFieldType
      MarkAttendanceViewModel com_mbd_cmsteacher_feature_attendance_MarkAttendanceViewModel2;

      @KeepFieldType
      AppRootViewModel com_mbd_cmsteacher_feature_root_AppRootViewModel2;

      @KeepFieldType
      ExamPaperSubmissionViewModel com_mbd_cmsteacher_feature_exampaper_ExamPaperSubmissionViewModel2;

      @KeepFieldType
      MenuViewModel com_mbd_cmsteacher_feature_hub_MenuViewModel2;

      @KeepFieldType
      NotificationsViewModel com_mbd_cmsteacher_feature_notifications_NotificationsViewModel2;

      @KeepFieldType
      SemesterResultsViewModel com_mbd_cmsteacher_feature_results_SemesterResultsViewModel2;

      @KeepFieldType
      InsightsViewModel com_mbd_cmsteacher_feature_insights_InsightsViewModel2;

      @KeepFieldType
      ExamsHubViewModel com_mbd_cmsteacher_feature_hub_ExamsHubViewModel2;

      @KeepFieldType
      LinkRequestsViewModel com_mbd_cmsteacher_feature_linkrequests_LinkRequestsViewModel2;

      @KeepFieldType
      DatesheetsViewModel com_mbd_cmscommon_ui_datesheets_DatesheetsViewModel2;

      @KeepFieldType
      ScheduleViewModel com_mbd_cmsteacher_feature_schedule_ScheduleViewModel2;

      @KeepFieldType
      EventsViewModel com_mbd_cmscommon_ui_events_EventsViewModel2;

      @KeepFieldType
      GlobalRefreshViewModel com_mbd_cmscommon_ui_state_GlobalRefreshViewModel2;

      @KeepFieldType
      ProfileViewModel com_mbd_cmsteacher_feature_profile_ProfileViewModel2;

      @KeepFieldType
      AttendanceHistoryViewModel com_mbd_cmsteacher_feature_attendance_AttendanceHistoryViewModel2;

      @KeepFieldType
      MarksEntryViewModel com_mbd_cmsteacher_feature_marks_MarksEntryViewModel2;

      @KeepFieldType
      MyStudentsViewModel com_mbd_cmsteacher_feature_students_MyStudentsViewModel2;

      @KeepFieldType
      DocumentsViewModel com_mbd_cmscommon_ui_documents_DocumentsViewModel2;

      @KeepFieldType
      LoginViewModel com_mbd_cmsteacher_feature_auth_LoginViewModel2;

      @KeepFieldType
      HomeViewModel com_mbd_cmsteacher_feature_home_HomeViewModel2;
    }
  }

  private static final class ViewModelCImpl extends TeacherApplication_HiltComponents.ViewModelC {
    private final SavedStateHandle savedStateHandle;

    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<AppRootViewModel> appRootViewModelProvider;

    private Provider<AttendanceHistoryViewModel> attendanceHistoryViewModelProvider;

    private Provider<DatesheetsViewModel> datesheetsViewModelProvider;

    private Provider<DocumentsViewModel> documentsViewModelProvider;

    private Provider<EventsViewModel> eventsViewModelProvider;

    private Provider<ExamPaperSubmissionViewModel> examPaperSubmissionViewModelProvider;

    private Provider<ExamsHubViewModel> examsHubViewModelProvider;

    private Provider<GlobalRefreshViewModel> globalRefreshViewModelProvider;

    private Provider<HomeViewModel> homeViewModelProvider;

    private Provider<InsightsViewModel> insightsViewModelProvider;

    private Provider<LinkRequestsViewModel> linkRequestsViewModelProvider;

    private Provider<LoginViewModel> loginViewModelProvider;

    private Provider<MarkAttendanceViewModel> markAttendanceViewModelProvider;

    private Provider<MarksEntryViewModel> marksEntryViewModelProvider;

    private Provider<MenuViewModel> menuViewModelProvider;

    private Provider<MyStudentsViewModel> myStudentsViewModelProvider;

    private Provider<NotificationsBadgeViewModel> notificationsBadgeViewModelProvider;

    private Provider<NotificationsViewModel> notificationsViewModelProvider;

    private Provider<ProfileViewModel> profileViewModelProvider;

    private Provider<ScheduleViewModel> scheduleViewModelProvider;

    private Provider<SemesterResultsViewModel> semesterResultsViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.savedStateHandle = savedStateHandleParam;
      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.appRootViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.attendanceHistoryViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.datesheetsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.documentsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.eventsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.examPaperSubmissionViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.examsHubViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.globalRefreshViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.homeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
      this.insightsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 9);
      this.linkRequestsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 10);
      this.loginViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 11);
      this.markAttendanceViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 12);
      this.marksEntryViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 13);
      this.menuViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 14);
      this.myStudentsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 15);
      this.notificationsBadgeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 16);
      this.notificationsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 17);
      this.profileViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 18);
      this.scheduleViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 19);
      this.semesterResultsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 20);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(21).put(LazyClassKeyProvider.com_mbd_cmsteacher_feature_root_AppRootViewModel, ((Provider) appRootViewModelProvider)).put(LazyClassKeyProvider.com_mbd_cmsteacher_feature_attendance_AttendanceHistoryViewModel, ((Provider) attendanceHistoryViewModelProvider)).put(LazyClassKeyProvider.com_mbd_cmscommon_ui_datesheets_DatesheetsViewModel, ((Provider) datesheetsViewModelProvider)).put(LazyClassKeyProvider.com_mbd_cmscommon_ui_documents_DocumentsViewModel, ((Provider) documentsViewModelProvider)).put(LazyClassKeyProvider.com_mbd_cmscommon_ui_events_EventsViewModel, ((Provider) eventsViewModelProvider)).put(LazyClassKeyProvider.com_mbd_cmsteacher_feature_exampaper_ExamPaperSubmissionViewModel, ((Provider) examPaperSubmissionViewModelProvider)).put(LazyClassKeyProvider.com_mbd_cmsteacher_feature_hub_ExamsHubViewModel, ((Provider) examsHubViewModelProvider)).put(LazyClassKeyProvider.com_mbd_cmscommon_ui_state_GlobalRefreshViewModel, ((Provider) globalRefreshViewModelProvider)).put(LazyClassKeyProvider.com_mbd_cmsteacher_feature_home_HomeViewModel, ((Provider) homeViewModelProvider)).put(LazyClassKeyProvider.com_mbd_cmsteacher_feature_insights_InsightsViewModel, ((Provider) insightsViewModelProvider)).put(LazyClassKeyProvider.com_mbd_cmsteacher_feature_linkrequests_LinkRequestsViewModel, ((Provider) linkRequestsViewModelProvider)).put(LazyClassKeyProvider.com_mbd_cmsteacher_feature_auth_LoginViewModel, ((Provider) loginViewModelProvider)).put(LazyClassKeyProvider.com_mbd_cmsteacher_feature_attendance_MarkAttendanceViewModel, ((Provider) markAttendanceViewModelProvider)).put(LazyClassKeyProvider.com_mbd_cmsteacher_feature_marks_MarksEntryViewModel, ((Provider) marksEntryViewModelProvider)).put(LazyClassKeyProvider.com_mbd_cmsteacher_feature_hub_MenuViewModel, ((Provider) menuViewModelProvider)).put(LazyClassKeyProvider.com_mbd_cmsteacher_feature_students_MyStudentsViewModel, ((Provider) myStudentsViewModelProvider)).put(LazyClassKeyProvider.com_mbd_cmsteacher_feature_notifications_NotificationsBadgeViewModel, ((Provider) notificationsBadgeViewModelProvider)).put(LazyClassKeyProvider.com_mbd_cmsteacher_feature_notifications_NotificationsViewModel, ((Provider) notificationsViewModelProvider)).put(LazyClassKeyProvider.com_mbd_cmsteacher_feature_profile_ProfileViewModel, ((Provider) profileViewModelProvider)).put(LazyClassKeyProvider.com_mbd_cmsteacher_feature_schedule_ScheduleViewModel, ((Provider) scheduleViewModelProvider)).put(LazyClassKeyProvider.com_mbd_cmsteacher_feature_results_SemesterResultsViewModel, ((Provider) semesterResultsViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_mbd_cmsteacher_feature_home_HomeViewModel = "com.mbd.cmsteacher.feature.home.HomeViewModel";

      static String com_mbd_cmsteacher_feature_notifications_NotificationsViewModel = "com.mbd.cmsteacher.feature.notifications.NotificationsViewModel";

      static String com_mbd_cmsteacher_feature_hub_MenuViewModel = "com.mbd.cmsteacher.feature.hub.MenuViewModel";

      static String com_mbd_cmsteacher_feature_students_MyStudentsViewModel = "com.mbd.cmsteacher.feature.students.MyStudentsViewModel";

      static String com_mbd_cmsteacher_feature_linkrequests_LinkRequestsViewModel = "com.mbd.cmsteacher.feature.linkrequests.LinkRequestsViewModel";

      static String com_mbd_cmsteacher_feature_auth_LoginViewModel = "com.mbd.cmsteacher.feature.auth.LoginViewModel";

      static String com_mbd_cmscommon_ui_state_GlobalRefreshViewModel = "com.mbd.cmscommon.ui.state.GlobalRefreshViewModel";

      static String com_mbd_cmsteacher_feature_attendance_AttendanceHistoryViewModel = "com.mbd.cmsteacher.feature.attendance.AttendanceHistoryViewModel";

      static String com_mbd_cmscommon_ui_datesheets_DatesheetsViewModel = "com.mbd.cmscommon.ui.datesheets.DatesheetsViewModel";

      static String com_mbd_cmsteacher_feature_results_SemesterResultsViewModel = "com.mbd.cmsteacher.feature.results.SemesterResultsViewModel";

      static String com_mbd_cmsteacher_feature_root_AppRootViewModel = "com.mbd.cmsteacher.feature.root.AppRootViewModel";

      static String com_mbd_cmsteacher_feature_attendance_MarkAttendanceViewModel = "com.mbd.cmsteacher.feature.attendance.MarkAttendanceViewModel";

      static String com_mbd_cmscommon_ui_events_EventsViewModel = "com.mbd.cmscommon.ui.events.EventsViewModel";

      static String com_mbd_cmsteacher_feature_hub_ExamsHubViewModel = "com.mbd.cmsteacher.feature.hub.ExamsHubViewModel";

      static String com_mbd_cmsteacher_feature_notifications_NotificationsBadgeViewModel = "com.mbd.cmsteacher.feature.notifications.NotificationsBadgeViewModel";

      static String com_mbd_cmsteacher_feature_insights_InsightsViewModel = "com.mbd.cmsteacher.feature.insights.InsightsViewModel";

      static String com_mbd_cmscommon_ui_documents_DocumentsViewModel = "com.mbd.cmscommon.ui.documents.DocumentsViewModel";

      static String com_mbd_cmsteacher_feature_marks_MarksEntryViewModel = "com.mbd.cmsteacher.feature.marks.MarksEntryViewModel";

      static String com_mbd_cmsteacher_feature_exampaper_ExamPaperSubmissionViewModel = "com.mbd.cmsteacher.feature.exampaper.ExamPaperSubmissionViewModel";

      static String com_mbd_cmsteacher_feature_profile_ProfileViewModel = "com.mbd.cmsteacher.feature.profile.ProfileViewModel";

      static String com_mbd_cmsteacher_feature_schedule_ScheduleViewModel = "com.mbd.cmsteacher.feature.schedule.ScheduleViewModel";

      @KeepFieldType
      HomeViewModel com_mbd_cmsteacher_feature_home_HomeViewModel2;

      @KeepFieldType
      NotificationsViewModel com_mbd_cmsteacher_feature_notifications_NotificationsViewModel2;

      @KeepFieldType
      MenuViewModel com_mbd_cmsteacher_feature_hub_MenuViewModel2;

      @KeepFieldType
      MyStudentsViewModel com_mbd_cmsteacher_feature_students_MyStudentsViewModel2;

      @KeepFieldType
      LinkRequestsViewModel com_mbd_cmsteacher_feature_linkrequests_LinkRequestsViewModel2;

      @KeepFieldType
      LoginViewModel com_mbd_cmsteacher_feature_auth_LoginViewModel2;

      @KeepFieldType
      GlobalRefreshViewModel com_mbd_cmscommon_ui_state_GlobalRefreshViewModel2;

      @KeepFieldType
      AttendanceHistoryViewModel com_mbd_cmsteacher_feature_attendance_AttendanceHistoryViewModel2;

      @KeepFieldType
      DatesheetsViewModel com_mbd_cmscommon_ui_datesheets_DatesheetsViewModel2;

      @KeepFieldType
      SemesterResultsViewModel com_mbd_cmsteacher_feature_results_SemesterResultsViewModel2;

      @KeepFieldType
      AppRootViewModel com_mbd_cmsteacher_feature_root_AppRootViewModel2;

      @KeepFieldType
      MarkAttendanceViewModel com_mbd_cmsteacher_feature_attendance_MarkAttendanceViewModel2;

      @KeepFieldType
      EventsViewModel com_mbd_cmscommon_ui_events_EventsViewModel2;

      @KeepFieldType
      ExamsHubViewModel com_mbd_cmsteacher_feature_hub_ExamsHubViewModel2;

      @KeepFieldType
      NotificationsBadgeViewModel com_mbd_cmsteacher_feature_notifications_NotificationsBadgeViewModel2;

      @KeepFieldType
      InsightsViewModel com_mbd_cmsteacher_feature_insights_InsightsViewModel2;

      @KeepFieldType
      DocumentsViewModel com_mbd_cmscommon_ui_documents_DocumentsViewModel2;

      @KeepFieldType
      MarksEntryViewModel com_mbd_cmsteacher_feature_marks_MarksEntryViewModel2;

      @KeepFieldType
      ExamPaperSubmissionViewModel com_mbd_cmsteacher_feature_exampaper_ExamPaperSubmissionViewModel2;

      @KeepFieldType
      ProfileViewModel com_mbd_cmsteacher_feature_profile_ProfileViewModel2;

      @KeepFieldType
      ScheduleViewModel com_mbd_cmsteacher_feature_schedule_ScheduleViewModel2;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.mbd.cmsteacher.feature.root.AppRootViewModel 
          return (T) new AppRootViewModel(singletonCImpl.sessionManagerProvider.get(), singletonCImpl.bindUserRepositoryProvider.get(), singletonCImpl.bindTeacherRepositoryProvider.get(), singletonCImpl.syncEngineProvider.get(), singletonCImpl.startupBootstrapTrackerProvider.get());

          case 1: // com.mbd.cmsteacher.feature.attendance.AttendanceHistoryViewModel 
          return (T) new AttendanceHistoryViewModel(viewModelCImpl.savedStateHandle, singletonCImpl.bindSessionAttendanceRepositoryProvider.get(), singletonCImpl.bindAcademicSessionRepositoryProvider.get(), singletonCImpl.bindCurriculumRepositoryProvider.get(), singletonCImpl.bindSessionTimetableRepositoryProvider.get(), singletonCImpl.bindTeacherRepositoryProvider.get(), singletonCImpl.sessionManagerProvider.get());

          case 2: // com.mbd.cmscommon.ui.datesheets.DatesheetsViewModel 
          return (T) new DatesheetsViewModel(singletonCImpl.bindDatesheetRepositoryProvider.get(), singletonCImpl.bindUserRepositoryProvider.get(), singletonCImpl.bindAcademicSessionRepositoryProvider.get(), singletonCImpl.bindCurriculumRepositoryProvider.get(), singletonCImpl.bindTeacherRepositoryProvider.get(), singletonCImpl.sessionManagerProvider.get());

          case 3: // com.mbd.cmscommon.ui.documents.DocumentsViewModel 
          return (T) new DocumentsViewModel(singletonCImpl.bindDocumentRepositoryProvider.get(), singletonCImpl.bindUserRepositoryProvider.get(), singletonCImpl.bindTeacherRepositoryProvider.get(), singletonCImpl.bindDepartmentRepositoryProvider.get(), singletonCImpl.sessionManagerProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 4: // com.mbd.cmscommon.ui.events.EventsViewModel 
          return (T) new EventsViewModel(singletonCImpl.bindCalendarRepositoryProvider.get(), singletonCImpl.bindUserRepositoryProvider.get(), singletonCImpl.bindTeacherRepositoryProvider.get(), singletonCImpl.teacherAssignmentsProvider.get(), singletonCImpl.bindDepartmentRepositoryProvider.get(), singletonCImpl.bindAcademicSessionRepositoryProvider.get(), singletonCImpl.sessionManagerProvider.get());

          case 5: // com.mbd.cmsteacher.feature.exampaper.ExamPaperSubmissionViewModel 
          return (T) new ExamPaperSubmissionViewModel(singletonCImpl.teacherAssignmentsProvider.get(), singletonCImpl.bindExamPaperSubmissionRepositoryProvider.get(), singletonCImpl.sessionManagerProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 6: // com.mbd.cmsteacher.feature.hub.ExamsHubViewModel 
          return (T) new ExamsHubViewModel(singletonCImpl.sessionManagerProvider.get(), singletonCImpl.teacherAssignmentsProvider.get(), singletonCImpl.bindExamPaperSubmissionRepositoryProvider.get(), singletonCImpl.bindDatesheetRepositoryProvider.get());

          case 7: // com.mbd.cmscommon.ui.state.GlobalRefreshViewModel 
          return (T) new GlobalRefreshViewModel(singletonCImpl.syncEngineProvider.get());

          case 8: // com.mbd.cmsteacher.feature.home.HomeViewModel 
          return (T) new HomeViewModel(singletonCImpl.teacherAssignmentsProvider.get(), singletonCImpl.bindSessionTimetableRepositoryProvider.get(), singletonCImpl.sessionManagerProvider.get());

          case 9: // com.mbd.cmsteacher.feature.insights.InsightsViewModel 
          return (T) new InsightsViewModel(singletonCImpl.bindInsightsRepositoryProvider.get(), singletonCImpl.bindAcademicSessionRepositoryProvider.get(), singletonCImpl.bindDepartmentRepositoryProvider.get(), singletonCImpl.teacherAssignmentsProvider.get());

          case 10: // com.mbd.cmsteacher.feature.linkrequests.LinkRequestsViewModel 
          return (T) new LinkRequestsViewModel(singletonCImpl.bindStudentLinkRequestRepositoryProvider.get(), singletonCImpl.bindAcademicSessionRepositoryProvider.get(), singletonCImpl.bindDepartmentRepositoryProvider.get(), singletonCImpl.bindTeacherRepositoryProvider.get(), singletonCImpl.sessionManagerProvider.get());

          case 11: // com.mbd.cmsteacher.feature.auth.LoginViewModel 
          return (T) new LoginViewModel(singletonCImpl.sessionManagerProvider.get(), singletonCImpl.bindUserRepositoryProvider.get());

          case 12: // com.mbd.cmsteacher.feature.attendance.MarkAttendanceViewModel 
          return (T) new MarkAttendanceViewModel(singletonCImpl.teacherAssignmentsProvider.get(), singletonCImpl.bindSessionAttendanceRepositoryProvider.get(), singletonCImpl.bindAcademicSessionRepositoryProvider.get(), singletonCImpl.bindNotificationRepositoryProvider.get(), singletonCImpl.sessionManagerProvider.get());

          case 13: // com.mbd.cmsteacher.feature.marks.MarksEntryViewModel 
          return (T) new MarksEntryViewModel(singletonCImpl.teacherAssignmentsProvider.get(), singletonCImpl.bindSessionMarksRepositoryProvider.get(), singletonCImpl.bindAcademicSessionRepositoryProvider.get(), singletonCImpl.bindMarkEditRequestRepositoryProvider.get(), singletonCImpl.sessionManagerProvider.get());

          case 14: // com.mbd.cmsteacher.feature.hub.MenuViewModel 
          return (T) new MenuViewModel(singletonCImpl.sessionManagerProvider.get(), singletonCImpl.bindUserRepositoryProvider.get(), singletonCImpl.bindTeacherRepositoryProvider.get(), singletonCImpl.teacherAssignmentsProvider.get(), singletonCImpl.bindNotificationRepositoryProvider.get(), singletonCImpl.bindStudentLinkRequestRepositoryProvider.get());

          case 15: // com.mbd.cmsteacher.feature.students.MyStudentsViewModel 
          return (T) new MyStudentsViewModel(singletonCImpl.teacherAssignmentsProvider.get(), singletonCImpl.bindAcademicSessionRepositoryProvider.get(), singletonCImpl.bindSessionAttendanceRepositoryProvider.get());

          case 16: // com.mbd.cmsteacher.feature.notifications.NotificationsBadgeViewModel 
          return (T) new NotificationsBadgeViewModel(singletonCImpl.bindNotificationRepositoryProvider.get(), singletonCImpl.bindTeacherRepositoryProvider.get(), singletonCImpl.sessionManagerProvider.get());

          case 17: // com.mbd.cmsteacher.feature.notifications.NotificationsViewModel 
          return (T) new NotificationsViewModel(singletonCImpl.bindNotificationRepositoryProvider.get(), singletonCImpl.bindAcademicSessionRepositoryProvider.get(), singletonCImpl.bindDepartmentRepositoryProvider.get(), singletonCImpl.bindTeacherRepositoryProvider.get(), singletonCImpl.teacherAssignmentsProvider.get(), singletonCImpl.sessionManagerProvider.get());

          case 18: // com.mbd.cmsteacher.feature.profile.ProfileViewModel 
          return (T) new ProfileViewModel(singletonCImpl.sessionManagerProvider.get(), singletonCImpl.bindTeacherRepositoryProvider.get(), singletonCImpl.bindDepartmentRepositoryProvider.get(), singletonCImpl.teacherAssignmentsProvider.get());

          case 19: // com.mbd.cmsteacher.feature.schedule.ScheduleViewModel 
          return (T) new ScheduleViewModel(singletonCImpl.bindDepartmentRepositoryProvider.get(), singletonCImpl.bindAcademicSessionRepositoryProvider.get(), singletonCImpl.bindSessionTimetableRepositoryProvider.get(), singletonCImpl.sessionManagerProvider.get());

          case 20: // com.mbd.cmsteacher.feature.results.SemesterResultsViewModel 
          return (T) new SemesterResultsViewModel(singletonCImpl.teacherAssignmentsProvider.get(), singletonCImpl.bindSessionMarksRepositoryProvider.get(), singletonCImpl.bindAcademicSessionRepositoryProvider.get(), singletonCImpl.bindCurriculumRepositoryProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends TeacherApplication_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends TeacherApplication_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends TeacherApplication_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<SupabaseClient> provideSupabaseClientProvider;

    private Provider<Auth> provideAuthProvider;

    private Provider<SessionManager> sessionManagerProvider;

    private Provider<Postgrest> providePostgrestProvider;

    private Provider<CmsDatabase> provideTeacherDatabaseProvider;

    private Provider<RoleResolver> roleResolverProvider;

    private Provider<UserRepositoryImpl> userRepositoryImplProvider;

    private Provider<UserRepository> bindUserRepositoryProvider;

    private Provider<Functions> provideFunctionsProvider;

    private Provider<AdminUserProvisioner> adminUserProvisionerProvider;

    private Provider<RoomSyncCheckpointStore> roomSyncCheckpointStoreProvider;

    private Provider<TeacherRepositoryImpl> teacherRepositoryImplProvider;

    private Provider<TeacherRepository> bindTeacherRepositoryProvider;

    private Provider<DepartmentRepositoryImpl> departmentRepositoryImplProvider;

    private Provider<DepartmentRepository> bindDepartmentRepositoryProvider;

    private Provider<CurriculumRepositoryImpl> curriculumRepositoryImplProvider;

    private Provider<CurriculumRepository> bindCurriculumRepositoryProvider;

    private Provider<AcademicSessionRepositoryImpl> academicSessionRepositoryImplProvider;

    private Provider<AcademicSessionRepository> bindAcademicSessionRepositoryProvider;

    private Provider<SessionTimetableRepositoryImpl> sessionTimetableRepositoryImplProvider;

    private Provider<SessionTimetableRepository> bindSessionTimetableRepositoryProvider;

    private Provider<StudentLinkRequestRepositoryImpl> studentLinkRequestRepositoryImplProvider;

    private Provider<StudentLinkRequestRepository> bindStudentLinkRequestRepositoryProvider;

    private Provider<SyncEngine> syncEngineProvider;

    private Provider<StartupBootstrapTracker> startupBootstrapTrackerProvider;

    private Provider<SessionAttendanceRepositoryImpl> sessionAttendanceRepositoryImplProvider;

    private Provider<SessionAttendanceRepository> bindSessionAttendanceRepositoryProvider;

    private Provider<DatesheetRepositoryLocalImpl> datesheetRepositoryLocalImplProvider;

    private Provider<DatesheetRepository> bindDatesheetRepositoryProvider;

    private Provider<Storage> provideStorageProvider;

    private Provider<DocumentRepositoryImpl> documentRepositoryImplProvider;

    private Provider<DocumentRepository> bindDocumentRepositoryProvider;

    private Provider<CalendarRepositoryLocalImpl> calendarRepositoryLocalImplProvider;

    private Provider<CalendarRepository> bindCalendarRepositoryProvider;

    private Provider<TeacherAssignmentsProvider> teacherAssignmentsProvider;

    private Provider<ExamPaperSubmissionRepositoryImpl> examPaperSubmissionRepositoryImplProvider;

    private Provider<ExamPaperSubmissionRepository> bindExamPaperSubmissionRepositoryProvider;

    private Provider<InsightsRepositoryLocalImpl> insightsRepositoryLocalImplProvider;

    private Provider<InsightsRepository> bindInsightsRepositoryProvider;

    private Provider<DataStore<Preferences>> provideDataStoreProvider;

    private Provider<NotificationRepositoryImpl> notificationRepositoryImplProvider;

    private Provider<NotificationRepository> bindNotificationRepositoryProvider;

    private Provider<SessionMarksRepositoryImpl> sessionMarksRepositoryImplProvider;

    private Provider<SessionMarksRepository> bindSessionMarksRepositoryProvider;

    private Provider<MarkEditRequestRepositoryLocalImpl> markEditRequestRepositoryLocalImplProvider;

    private Provider<MarkEditRequestRepository> bindMarkEditRequestRepositoryProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);
      initialize2(applicationContextModuleParam);

    }

    private UserDao userDao() {
      return DaoModule_ProvideUserDaoFactory.provideUserDao(provideTeacherDatabaseProvider.get());
    }

    private TeacherDao teacherDao() {
      return DaoModule_ProvideTeacherDaoFactory.provideTeacherDao(provideTeacherDatabaseProvider.get());
    }

    private TableSyncStateDao tableSyncStateDao() {
      return DaoModule_ProvideTableSyncStateDaoFactory.provideTableSyncStateDao(provideTeacherDatabaseProvider.get());
    }

    private SyncCheckpointStore syncCheckpointStore() {
      return DaoModule_ProvideSyncCheckpointStoreFactory.provideSyncCheckpointStore(roomSyncCheckpointStoreProvider.get());
    }

    private DepartmentDao departmentDao() {
      return DaoModule_ProvideDepartmentDaoFactory.provideDepartmentDao(provideTeacherDatabaseProvider.get());
    }

    private SemesterSubjectDao semesterSubjectDao() {
      return DaoModule_ProvideSemesterSubjectDaoFactory.provideSemesterSubjectDao(provideTeacherDatabaseProvider.get());
    }

    private AcademicSessionDao academicSessionDao() {
      return DaoModule_ProvideAcademicSessionDaoFactory.provideAcademicSessionDao(provideTeacherDatabaseProvider.get());
    }

    private SessionStudentDao sessionStudentDao() {
      return DaoModule_ProvideSessionStudentDaoFactory.provideSessionStudentDao(provideTeacherDatabaseProvider.get());
    }

    private SessionPeriodDao sessionPeriodDao() {
      return DaoModule_ProvideSessionPeriodDaoFactory.provideSessionPeriodDao(provideTeacherDatabaseProvider.get());
    }

    private StudentLinkRequestDao studentLinkRequestDao() {
      return DaoModule_ProvideStudentLinkRequestDaoFactory.provideStudentLinkRequestDao(provideTeacherDatabaseProvider.get());
    }

    private SyncStateDao syncStateDao() {
      return DaoModule_ProvideSyncStateDaoFactory.provideSyncStateDao(provideTeacherDatabaseProvider.get());
    }

    private SessionAttendanceDao sessionAttendanceDao() {
      return DaoModule_ProvideSessionAttendanceDaoFactory.provideSessionAttendanceDao(provideTeacherDatabaseProvider.get());
    }

    private DatesheetDao datesheetDao() {
      return DaoModule_ProvideDatesheetDaoFactory.provideDatesheetDao(provideTeacherDatabaseProvider.get());
    }

    private DocumentDao documentDao() {
      return DaoModule_ProvideDocumentDaoFactory.provideDocumentDao(provideTeacherDatabaseProvider.get());
    }

    private CalendarEventDao calendarEventDao() {
      return DaoModule_ProvideCalendarEventDaoFactory.provideCalendarEventDao(provideTeacherDatabaseProvider.get());
    }

    private ExamPaperSubmissionDao examPaperSubmissionDao() {
      return DaoModule_ProvideExamPaperSubmissionDaoFactory.provideExamPaperSubmissionDao(provideTeacherDatabaseProvider.get());
    }

    private InsightsDao insightsDao() {
      return DaoModule_ProvideInsightsDaoFactory.provideInsightsDao(provideTeacherDatabaseProvider.get());
    }

    private NotificationDao notificationDao() {
      return DaoModule_ProvideNotificationDaoFactory.provideNotificationDao(provideTeacherDatabaseProvider.get());
    }

    private SessionMarkDao sessionMarkDao() {
      return DaoModule_ProvideSessionMarkDaoFactory.provideSessionMarkDao(provideTeacherDatabaseProvider.get());
    }

    private StudentSemesterGpaDao studentSemesterGpaDao() {
      return DaoModule_ProvideStudentSemesterGpaDaoFactory.provideStudentSemesterGpaDao(provideTeacherDatabaseProvider.get());
    }

    private MarkEditRequestDao markEditRequestDao() {
      return DaoModule_ProvideMarkEditRequestDaoFactory.provideMarkEditRequestDao(provideTeacherDatabaseProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideSupabaseClientProvider = DoubleCheck.provider(new SwitchingProvider<SupabaseClient>(singletonCImpl, 2));
      this.provideAuthProvider = DoubleCheck.provider(new SwitchingProvider<Auth>(singletonCImpl, 1));
      this.sessionManagerProvider = DoubleCheck.provider(new SwitchingProvider<SessionManager>(singletonCImpl, 0));
      this.providePostgrestProvider = DoubleCheck.provider(new SwitchingProvider<Postgrest>(singletonCImpl, 4));
      this.provideTeacherDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<CmsDatabase>(singletonCImpl, 5));
      this.roleResolverProvider = DoubleCheck.provider(new SwitchingProvider<RoleResolver>(singletonCImpl, 6));
      this.userRepositoryImplProvider = new SwitchingProvider<>(singletonCImpl, 3);
      this.bindUserRepositoryProvider = DoubleCheck.provider((Provider) userRepositoryImplProvider);
      this.provideFunctionsProvider = DoubleCheck.provider(new SwitchingProvider<Functions>(singletonCImpl, 9));
      this.adminUserProvisionerProvider = DoubleCheck.provider(new SwitchingProvider<AdminUserProvisioner>(singletonCImpl, 8));
      this.roomSyncCheckpointStoreProvider = DoubleCheck.provider(new SwitchingProvider<RoomSyncCheckpointStore>(singletonCImpl, 10));
      this.teacherRepositoryImplProvider = new SwitchingProvider<>(singletonCImpl, 7);
      this.bindTeacherRepositoryProvider = DoubleCheck.provider((Provider) teacherRepositoryImplProvider);
      this.departmentRepositoryImplProvider = new SwitchingProvider<>(singletonCImpl, 12);
      this.bindDepartmentRepositoryProvider = DoubleCheck.provider((Provider) departmentRepositoryImplProvider);
      this.curriculumRepositoryImplProvider = new SwitchingProvider<>(singletonCImpl, 13);
      this.bindCurriculumRepositoryProvider = DoubleCheck.provider((Provider) curriculumRepositoryImplProvider);
      this.academicSessionRepositoryImplProvider = new SwitchingProvider<>(singletonCImpl, 14);
      this.bindAcademicSessionRepositoryProvider = DoubleCheck.provider((Provider) academicSessionRepositoryImplProvider);
      this.sessionTimetableRepositoryImplProvider = new SwitchingProvider<>(singletonCImpl, 15);
      this.bindSessionTimetableRepositoryProvider = DoubleCheck.provider((Provider) sessionTimetableRepositoryImplProvider);
      this.studentLinkRequestRepositoryImplProvider = new SwitchingProvider<>(singletonCImpl, 16);
      this.bindStudentLinkRequestRepositoryProvider = DoubleCheck.provider((Provider) studentLinkRequestRepositoryImplProvider);
      this.syncEngineProvider = DoubleCheck.provider(new SwitchingProvider<SyncEngine>(singletonCImpl, 11));
      this.startupBootstrapTrackerProvider = DoubleCheck.provider(new SwitchingProvider<StartupBootstrapTracker>(singletonCImpl, 17));
    }

    @SuppressWarnings("unchecked")
    private void initialize2(final ApplicationContextModule applicationContextModuleParam) {
      this.sessionAttendanceRepositoryImplProvider = new SwitchingProvider<>(singletonCImpl, 18);
      this.bindSessionAttendanceRepositoryProvider = DoubleCheck.provider((Provider) sessionAttendanceRepositoryImplProvider);
      this.datesheetRepositoryLocalImplProvider = new SwitchingProvider<>(singletonCImpl, 19);
      this.bindDatesheetRepositoryProvider = DoubleCheck.provider((Provider) datesheetRepositoryLocalImplProvider);
      this.provideStorageProvider = DoubleCheck.provider(new SwitchingProvider<Storage>(singletonCImpl, 21));
      this.documentRepositoryImplProvider = new SwitchingProvider<>(singletonCImpl, 20);
      this.bindDocumentRepositoryProvider = DoubleCheck.provider((Provider) documentRepositoryImplProvider);
      this.calendarRepositoryLocalImplProvider = new SwitchingProvider<>(singletonCImpl, 22);
      this.bindCalendarRepositoryProvider = DoubleCheck.provider((Provider) calendarRepositoryLocalImplProvider);
      this.teacherAssignmentsProvider = DoubleCheck.provider(new SwitchingProvider<TeacherAssignmentsProvider>(singletonCImpl, 23));
      this.examPaperSubmissionRepositoryImplProvider = new SwitchingProvider<>(singletonCImpl, 24);
      this.bindExamPaperSubmissionRepositoryProvider = DoubleCheck.provider((Provider) examPaperSubmissionRepositoryImplProvider);
      this.insightsRepositoryLocalImplProvider = new SwitchingProvider<>(singletonCImpl, 25);
      this.bindInsightsRepositoryProvider = DoubleCheck.provider((Provider) insightsRepositoryLocalImplProvider);
      this.provideDataStoreProvider = DoubleCheck.provider(new SwitchingProvider<DataStore<Preferences>>(singletonCImpl, 27));
      this.notificationRepositoryImplProvider = new SwitchingProvider<>(singletonCImpl, 26);
      this.bindNotificationRepositoryProvider = DoubleCheck.provider((Provider) notificationRepositoryImplProvider);
      this.sessionMarksRepositoryImplProvider = new SwitchingProvider<>(singletonCImpl, 28);
      this.bindSessionMarksRepositoryProvider = DoubleCheck.provider((Provider) sessionMarksRepositoryImplProvider);
      this.markEditRequestRepositoryLocalImplProvider = new SwitchingProvider<>(singletonCImpl, 29);
      this.bindMarkEditRequestRepositoryProvider = DoubleCheck.provider((Provider) markEditRequestRepositoryLocalImplProvider);
    }

    @Override
    public void injectTeacherApplication(TeacherApplication teacherApplication) {
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.mbd.cmscommon.auth.SessionManager 
          return (T) new SessionManager(singletonCImpl.provideAuthProvider.get());

          case 1: // io.github.jan.supabase.auth.Auth 
          return (T) SupabaseModule_ProvideAuthFactory.provideAuth(singletonCImpl.provideSupabaseClientProvider.get());

          case 2: // io.github.jan.supabase.SupabaseClient 
          return (T) SupabaseModule_ProvideSupabaseClientFactory.provideSupabaseClient();

          case 3: // com.mbd.cmscommon.data.repository.UserRepositoryImpl 
          return (T) new UserRepositoryImpl(singletonCImpl.providePostgrestProvider.get(), singletonCImpl.userDao(), singletonCImpl.roleResolverProvider.get());

          case 4: // io.github.jan.supabase.postgrest.Postgrest 
          return (T) SupabaseModule_ProvidePostgrestFactory.providePostgrest(singletonCImpl.provideSupabaseClientProvider.get());

          case 5: // com.mbd.cmscommon.data.local.CmsDatabase 
          return (T) DatabaseModule_ProvideTeacherDatabaseFactory.provideTeacherDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 6: // com.mbd.cmscommon.auth.RoleResolver 
          return (T) new RoleResolver(singletonCImpl.userDao(), singletonCImpl.teacherDao());

          case 7: // com.mbd.cmscommon.data.repository.TeacherRepositoryImpl 
          return (T) new TeacherRepositoryImpl(singletonCImpl.providePostgrestProvider.get(), singletonCImpl.teacherDao(), singletonCImpl.adminUserProvisionerProvider.get(), singletonCImpl.syncCheckpointStore(), singletonCImpl.sessionManagerProvider.get());

          case 8: // com.mbd.cmscommon.auth.AdminUserProvisioner 
          return (T) new AdminUserProvisioner(singletonCImpl.provideFunctionsProvider.get());

          case 9: // io.github.jan.supabase.functions.Functions 
          return (T) SupabaseModule_ProvideFunctionsFactory.provideFunctions(singletonCImpl.provideSupabaseClientProvider.get());

          case 10: // com.mbd.cmscommon.data.sync.RoomSyncCheckpointStore 
          return (T) new RoomSyncCheckpointStore(singletonCImpl.tableSyncStateDao());

          case 11: // com.mbd.cmscommon.data.sync.SyncEngine 
          return (T) new SyncEngine(singletonCImpl.bindDepartmentRepositoryProvider.get(), singletonCImpl.bindCurriculumRepositoryProvider.get(), singletonCImpl.bindAcademicSessionRepositoryProvider.get(), singletonCImpl.bindSessionTimetableRepositoryProvider.get(), singletonCImpl.bindTeacherRepositoryProvider.get(), singletonCImpl.bindStudentLinkRequestRepositoryProvider.get());

          case 12: // com.mbd.cmscommon.data.repository.DepartmentRepositoryImpl 
          return (T) new DepartmentRepositoryImpl(singletonCImpl.providePostgrestProvider.get(), singletonCImpl.departmentDao(), singletonCImpl.syncCheckpointStore(), singletonCImpl.sessionManagerProvider.get());

          case 13: // com.mbd.cmscommon.data.repository.CurriculumRepositoryImpl 
          return (T) new CurriculumRepositoryImpl(singletonCImpl.providePostgrestProvider.get(), singletonCImpl.semesterSubjectDao(), singletonCImpl.syncCheckpointStore(), singletonCImpl.sessionManagerProvider.get());

          case 14: // com.mbd.cmscommon.data.repository.AcademicSessionRepositoryImpl 
          return (T) new AcademicSessionRepositoryImpl(singletonCImpl.providePostgrestProvider.get(), singletonCImpl.academicSessionDao(), singletonCImpl.sessionStudentDao(), singletonCImpl.sessionPeriodDao(), singletonCImpl.syncCheckpointStore(), singletonCImpl.sessionManagerProvider.get());

          case 15: // com.mbd.cmscommon.data.repository.SessionTimetableRepositoryImpl 
          return (T) new SessionTimetableRepositoryImpl(singletonCImpl.providePostgrestProvider.get(), singletonCImpl.sessionPeriodDao(), singletonCImpl.academicSessionDao(), singletonCImpl.syncCheckpointStore(), singletonCImpl.sessionManagerProvider.get());

          case 16: // com.mbd.cmscommon.data.repository.StudentLinkRequestRepositoryImpl 
          return (T) new StudentLinkRequestRepositoryImpl(singletonCImpl.providePostgrestProvider.get(), singletonCImpl.studentLinkRequestDao(), singletonCImpl.sessionStudentDao(), singletonCImpl.syncCheckpointStore(), singletonCImpl.sessionManagerProvider.get());

          case 17: // com.mbd.cmscommon.data.sync.StartupBootstrapTracker 
          return (T) new StartupBootstrapTracker(singletonCImpl.syncStateDao());

          case 18: // com.mbd.cmscommon.data.repository.SessionAttendanceRepositoryImpl 
          return (T) new SessionAttendanceRepositoryImpl(singletonCImpl.providePostgrestProvider.get(), singletonCImpl.sessionAttendanceDao(), singletonCImpl.academicSessionDao(), singletonCImpl.syncCheckpointStore(), singletonCImpl.sessionManagerProvider.get());

          case 19: // com.mbd.cmscommon.data.repository.DatesheetRepositoryLocalImpl 
          return (T) new DatesheetRepositoryLocalImpl(singletonCImpl.providePostgrestProvider.get(), singletonCImpl.datesheetDao(), singletonCImpl.syncCheckpointStore(), singletonCImpl.sessionManagerProvider.get());

          case 20: // com.mbd.cmscommon.data.repository.DocumentRepositoryImpl 
          return (T) new DocumentRepositoryImpl(singletonCImpl.providePostgrestProvider.get(), singletonCImpl.provideStorageProvider.get(), singletonCImpl.documentDao(), singletonCImpl.syncCheckpointStore(), singletonCImpl.sessionManagerProvider.get());

          case 21: // io.github.jan.supabase.storage.Storage 
          return (T) SupabaseModule_ProvideStorageFactory.provideStorage(singletonCImpl.provideSupabaseClientProvider.get());

          case 22: // com.mbd.cmscommon.data.repository.CalendarRepositoryLocalImpl 
          return (T) new CalendarRepositoryLocalImpl(singletonCImpl.providePostgrestProvider.get(), singletonCImpl.calendarEventDao(), singletonCImpl.syncCheckpointStore(), singletonCImpl.sessionManagerProvider.get());

          case 23: // com.mbd.cmscommon.teacher.TeacherAssignmentsProvider 
          return (T) new TeacherAssignmentsProvider(singletonCImpl.sessionManagerProvider.get(), singletonCImpl.bindSessionTimetableRepositoryProvider.get(), singletonCImpl.bindAcademicSessionRepositoryProvider.get(), singletonCImpl.bindDepartmentRepositoryProvider.get());

          case 24: // com.mbd.cmscommon.data.repository.ExamPaperSubmissionRepositoryImpl 
          return (T) new ExamPaperSubmissionRepositoryImpl(singletonCImpl.providePostgrestProvider.get(), singletonCImpl.provideStorageProvider.get(), singletonCImpl.examPaperSubmissionDao(), singletonCImpl.academicSessionDao(), singletonCImpl.syncCheckpointStore(), singletonCImpl.sessionManagerProvider.get());

          case 25: // com.mbd.cmscommon.data.repository.InsightsRepositoryLocalImpl 
          return (T) new InsightsRepositoryLocalImpl(singletonCImpl.providePostgrestProvider.get(), singletonCImpl.insightsDao());

          case 26: // com.mbd.cmscommon.data.repository.NotificationRepositoryImpl 
          return (T) new NotificationRepositoryImpl(singletonCImpl.providePostgrestProvider.get(), singletonCImpl.notificationDao(), singletonCImpl.provideDataStoreProvider.get(), singletonCImpl.syncCheckpointStore(), singletonCImpl.sessionManagerProvider.get());

          case 27: // androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences> 
          return (T) DataStoreModule_ProvideDataStoreFactory.provideDataStore(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 28: // com.mbd.cmscommon.data.repository.SessionMarksRepositoryImpl 
          return (T) new SessionMarksRepositoryImpl(singletonCImpl.providePostgrestProvider.get(), singletonCImpl.sessionMarkDao(), singletonCImpl.studentSemesterGpaDao(), singletonCImpl.academicSessionDao(), singletonCImpl.syncCheckpointStore(), singletonCImpl.sessionManagerProvider.get());

          case 29: // com.mbd.cmscommon.data.repository.MarkEditRequestRepositoryLocalImpl 
          return (T) new MarkEditRequestRepositoryLocalImpl(singletonCImpl.providePostgrestProvider.get(), singletonCImpl.markEditRequestDao(), singletonCImpl.syncCheckpointStore(), singletonCImpl.sessionManagerProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
