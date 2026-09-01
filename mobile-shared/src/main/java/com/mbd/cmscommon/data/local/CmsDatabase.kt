package com.mbd.cmscommon.data.local

import androidx.room.RoomDatabase
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

const val CMS_DATABASE_VERSION = 33

abstract class CmsDatabase : RoomDatabase() {
    abstract fun departmentDao(): DepartmentDao
    abstract fun administratorAccountDao(): AdministratorAccountDao
    abstract fun teacherDao(): TeacherDao
    abstract fun userDao(): UserDao
    abstract fun studentLinkRequestDao(): StudentLinkRequestDao
    abstract fun examPaperSubmissionDao(): ExamPaperSubmissionDao
    abstract fun notificationDao(): NotificationDao
    abstract fun fineDao(): FineDao
    abstract fun calendarEventDao(): CalendarEventDao
    abstract fun markEditRequestDao(): MarkEditRequestDao
    abstract fun insightsDao(): InsightsDao
    abstract fun datesheetDao(): DatesheetDao
    abstract fun syncStateDao(): SyncStateDao
    abstract fun tableSyncStateDao(): TableSyncStateDao
    abstract fun academicSessionDao(): AcademicSessionDao
    abstract fun semesterSubjectDao(): SemesterSubjectDao
    abstract fun sessionStudentDao(): SessionStudentDao
    abstract fun sessionPeriodDao(): SessionPeriodDao
    abstract fun sessionAttendanceDao(): SessionAttendanceDao
    abstract fun sessionMarkDao(): SessionMarkDao
    abstract fun studentSemesterGpaDao(): StudentSemesterGpaDao
    abstract fun sessionFeeDao(): SessionFeeDao
}
