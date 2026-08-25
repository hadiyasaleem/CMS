package com.mbd.cmscommon.di

import com.mbd.cmscommon.data.local.CmsDatabase
import com.mbd.cmscommon.data.local.dao.AcademicSessionDao
import com.mbd.cmscommon.data.local.dao.AdministratorAccountDao
import com.mbd.cmscommon.data.local.dao.CalendarEventDao
import com.mbd.cmscommon.data.local.dao.DatesheetDao
import com.mbd.cmscommon.data.local.dao.DepartmentDao
import com.mbd.cmscommon.data.local.dao.DocumentDao
import com.mbd.cmscommon.data.local.dao.ExamPaperSubmissionDao
import com.mbd.cmscommon.data.local.dao.FineDao
import com.mbd.cmscommon.data.local.dao.InsightsDao
import com.mbd.cmscommon.data.local.dao.MarkEditRequestDao
import com.mbd.cmscommon.data.local.dao.NotificationDao
import com.mbd.cmscommon.data.local.dao.SemesterSubjectDao
import com.mbd.cmscommon.data.local.dao.SessionAttendanceDao
import com.mbd.cmscommon.data.local.dao.SessionFeeDao
import com.mbd.cmscommon.data.local.dao.SessionMarkDao
import com.mbd.cmscommon.data.local.dao.SessionPeriodDao
import com.mbd.cmscommon.data.local.dao.SessionStudentDao
import com.mbd.cmscommon.data.local.dao.StudentLinkRequestDao
import com.mbd.cmscommon.data.local.dao.StudentSemesterGpaDao
import com.mbd.cmscommon.data.local.dao.SyncStateDao
import com.mbd.cmscommon.data.local.dao.TableSyncStateDao
import com.mbd.cmscommon.data.local.dao.TeacherDao
import com.mbd.cmscommon.data.local.dao.UserDao
import com.mbd.cmscommon.data.sync.RoomSyncCheckpointStore
import com.mbd.cmscommon.data.sync.SyncCheckpointStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DaoModule {
    @Provides
    fun provideDepartmentDao(db: CmsDatabase): DepartmentDao = db.departmentDao()

    @Provides
    fun provideAdministratorAccountDao(db: CmsDatabase): AdministratorAccountDao = db.administratorAccountDao()

    @Provides
    fun provideTeacherDao(db: CmsDatabase): TeacherDao = db.teacherDao()

    @Provides
    fun provideUserDao(db: CmsDatabase): UserDao = db.userDao()

    @Provides
    fun provideStudentLinkRequestDao(db: CmsDatabase): StudentLinkRequestDao = db.studentLinkRequestDao()

    @Provides
    fun provideDocumentDao(db: CmsDatabase): DocumentDao = db.documentDao()

    @Provides
    fun provideExamPaperSubmissionDao(db: CmsDatabase): ExamPaperSubmissionDao = db.examPaperSubmissionDao()

    @Provides
    fun provideNotificationDao(db: CmsDatabase): NotificationDao = db.notificationDao()

    @Provides
    fun provideFineDao(db: CmsDatabase): FineDao = db.fineDao()

    @Provides
    fun provideCalendarEventDao(db: CmsDatabase): CalendarEventDao = db.calendarEventDao()

    @Provides
    fun provideMarkEditRequestDao(db: CmsDatabase): MarkEditRequestDao = db.markEditRequestDao()

    @Provides
    fun provideInsightsDao(db: CmsDatabase): InsightsDao = db.insightsDao()

    @Provides
    fun provideDatesheetDao(db: CmsDatabase): DatesheetDao = db.datesheetDao()

    @Provides
    fun provideSyncStateDao(db: CmsDatabase): SyncStateDao = db.syncStateDao()

    @Provides
    fun provideTableSyncStateDao(db: CmsDatabase): TableSyncStateDao = db.tableSyncStateDao()

    @Provides
    fun provideAcademicSessionDao(db: CmsDatabase): AcademicSessionDao = db.academicSessionDao()

    @Provides
    fun provideSemesterSubjectDao(db: CmsDatabase): SemesterSubjectDao = db.semesterSubjectDao()

    @Provides
    fun provideSessionStudentDao(db: CmsDatabase): SessionStudentDao = db.sessionStudentDao()

    @Provides
    fun provideSessionPeriodDao(db: CmsDatabase): SessionPeriodDao = db.sessionPeriodDao()

    @Provides
    fun provideSessionAttendanceDao(db: CmsDatabase): SessionAttendanceDao = db.sessionAttendanceDao()

    @Provides
    fun provideSessionMarkDao(db: CmsDatabase): SessionMarkDao = db.sessionMarkDao()

    @Provides
    fun provideStudentSemesterGpaDao(db: CmsDatabase): StudentSemesterGpaDao = db.studentSemesterGpaDao()

    @Provides
    fun provideSessionFeeDao(db: CmsDatabase): SessionFeeDao = db.sessionFeeDao()

    @Provides
    fun provideSyncCheckpointStore(store: RoomSyncCheckpointStore): SyncCheckpointStore = store
}
