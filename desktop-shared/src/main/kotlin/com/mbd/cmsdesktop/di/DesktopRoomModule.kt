package com.mbd.cmsdesktop.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.mbd.cmscommon.data.local.dao.AcademicSessionDao
import com.mbd.cmscommon.data.local.dao.AdministratorAccountDao
import com.mbd.cmscommon.data.local.dao.CalendarEventDao
import com.mbd.cmscommon.data.local.dao.DatesheetDao
import com.mbd.cmscommon.data.local.dao.DepartmentDao
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
import com.mbd.cmsdesktop.data.local.DesktopDatabase
import com.mbd.cmsdesktop.data.local.dao.NotificationViewStateDao
import com.mbd.cmsdesktop.data.local.dao.DesktopAuthCodeVerifierDao
import com.mbd.cmsdesktop.data.local.dao.DesktopAuthSessionDao
import java.io.File
import javax.inject.Singleton
import dagger.Module
import dagger.Provides

/** Supplies the one durable Room database used by each desktop application identity. */
@Module
object DesktopRoomModule {
    private fun databaseFile(): File {
        val appId = System.getProperty("cms.desktop.appId").orEmpty().ifBlank { "shared" }
        val root = System.getenv("APPDATA") ?: System.getProperty("user.home")
        return File(root, "CMSDesktop/$appId/cms.db").also { it.parentFile.mkdirs() }
    }

    @Provides
    @Singleton
    fun provideDatabase(): DesktopDatabase =
        Room.databaseBuilder<DesktopDatabase>(name = databaseFile().absolutePath)
            .setDriver(BundledSQLiteDriver())
            .build()

    @Provides fun departmentDao(db: DesktopDatabase): DepartmentDao = db.departmentDao()
    @Provides fun administratorAccountDao(db: DesktopDatabase): AdministratorAccountDao = db.administratorAccountDao()
    @Provides fun teacherDao(db: DesktopDatabase): TeacherDao = db.teacherDao()
    @Provides fun userDao(db: DesktopDatabase): UserDao = db.userDao()
    @Provides fun studentLinkRequestDao(db: DesktopDatabase): StudentLinkRequestDao = db.studentLinkRequestDao()
    @Provides fun examPaperSubmissionDao(db: DesktopDatabase): ExamPaperSubmissionDao = db.examPaperSubmissionDao()
    @Provides fun notificationDao(db: DesktopDatabase): NotificationDao = db.notificationDao()
    @Provides fun notificationViewStateDao(db: DesktopDatabase): NotificationViewStateDao = db.notificationViewStateDao()
    @Provides fun desktopAuthSessionDao(db: DesktopDatabase): DesktopAuthSessionDao = db.desktopAuthSessionDao()
    @Provides fun desktopAuthCodeVerifierDao(db: DesktopDatabase): DesktopAuthCodeVerifierDao = db.desktopAuthCodeVerifierDao()
    @Provides fun fineDao(db: DesktopDatabase): FineDao = db.fineDao()
    @Provides fun calendarEventDao(db: DesktopDatabase): CalendarEventDao = db.calendarEventDao()
    @Provides fun markEditRequestDao(db: DesktopDatabase): MarkEditRequestDao = db.markEditRequestDao()
    @Provides fun insightsDao(db: DesktopDatabase): InsightsDao = db.insightsDao()
    @Provides fun datesheetDao(db: DesktopDatabase): DatesheetDao = db.datesheetDao()
    @Provides fun syncStateDao(db: DesktopDatabase): SyncStateDao = db.syncStateDao()
    @Provides fun tableSyncStateDao(db: DesktopDatabase): TableSyncStateDao = db.tableSyncStateDao()
    @Provides fun academicSessionDao(db: DesktopDatabase): AcademicSessionDao = db.academicSessionDao()
    @Provides fun semesterSubjectDao(db: DesktopDatabase): SemesterSubjectDao = db.semesterSubjectDao()
    @Provides fun sessionStudentDao(db: DesktopDatabase): SessionStudentDao = db.sessionStudentDao()
    @Provides fun sessionPeriodDao(db: DesktopDatabase): SessionPeriodDao = db.sessionPeriodDao()
    @Provides fun sessionAttendanceDao(db: DesktopDatabase): SessionAttendanceDao = db.sessionAttendanceDao()
    @Provides fun sessionMarkDao(db: DesktopDatabase): SessionMarkDao = db.sessionMarkDao()
    @Provides fun studentSemesterGpaDao(db: DesktopDatabase): StudentSemesterGpaDao = db.studentSemesterGpaDao()
    @Provides fun sessionFeeDao(db: DesktopDatabase): SessionFeeDao = db.sessionFeeDao()

    @Provides
    @Singleton
    fun syncCheckpointStore(dao: TableSyncStateDao): SyncCheckpointStore = RoomSyncCheckpointStore(dao)
}
