package com.mbd.cmscommon.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
import com.mbd.cmscommon.data.local.entity.AcademicSessionEntity
import com.mbd.cmscommon.data.local.entity.AdministratorAccountEntity
import com.mbd.cmscommon.data.local.entity.CalendarEventEntity
import com.mbd.cmscommon.data.local.entity.DatesheetEntity
import com.mbd.cmscommon.data.local.entity.DatesheetSlotEntity
import com.mbd.cmscommon.data.local.entity.DepartmentEntity
import com.mbd.cmscommon.data.local.entity.DocumentEntity
import com.mbd.cmscommon.data.local.entity.ExamPaperSubmissionEntity
import com.mbd.cmscommon.data.local.entity.FineEntity
import com.mbd.cmscommon.data.local.entity.InsightAtRiskStudentEntity
import com.mbd.cmscommon.data.local.entity.InsightExamStatEntity
import com.mbd.cmscommon.data.local.entity.InsightSessionOverviewEntity
import com.mbd.cmscommon.data.local.entity.MarkEditRequestEntity
import com.mbd.cmscommon.data.local.entity.NotificationEntity
import com.mbd.cmscommon.data.local.entity.SemesterSubjectEntity
import com.mbd.cmscommon.data.local.entity.SessionAttendanceRowEntity
import com.mbd.cmscommon.data.local.entity.SessionAttendanceTallyEntity
import com.mbd.cmscommon.data.local.entity.SessionFeeEntity
import com.mbd.cmscommon.data.local.entity.SessionFeeHeadEntity
import com.mbd.cmscommon.data.local.entity.SessionMarkEntity
import com.mbd.cmscommon.data.local.entity.SessionPeriodEntity
import com.mbd.cmscommon.data.local.entity.SessionStudentEntity
import com.mbd.cmscommon.data.local.entity.StudentLinkRequestEntity
import com.mbd.cmscommon.data.local.entity.StudentSemesterGpaEntity
import com.mbd.cmscommon.data.local.entity.SyncStateEntity
import com.mbd.cmscommon.data.local.entity.TableSyncStateEntity
import com.mbd.cmscommon.data.local.entity.TeacherEntity
import com.mbd.cmscommon.data.local.entity.UserEntity

@Database(
    entities = [
        DepartmentEntity::class,
        AdministratorAccountEntity::class,
        TeacherEntity::class,
        UserEntity::class,
        StudentLinkRequestEntity::class,
        DocumentEntity::class,
        ExamPaperSubmissionEntity::class,
        NotificationEntity::class,
        FineEntity::class,
        CalendarEventEntity::class,
        MarkEditRequestEntity::class,
        InsightSessionOverviewEntity::class,
        InsightAtRiskStudentEntity::class,
        InsightExamStatEntity::class,
        DatesheetEntity::class,
        DatesheetSlotEntity::class,
        SyncStateEntity::class,
        TableSyncStateEntity::class,
        AcademicSessionEntity::class,
        SemesterSubjectEntity::class,
        SessionStudentEntity::class,
        SessionPeriodEntity::class,
        SessionAttendanceRowEntity::class,
        SessionAttendanceTallyEntity::class,
        SessionMarkEntity::class,
        StudentSemesterGpaEntity::class,
        SessionFeeEntity::class,
        SessionFeeHeadEntity::class,
    ],
    version = 30,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class CmsDatabase : RoomDatabase() {
    abstract fun departmentDao(): DepartmentDao
    abstract fun administratorAccountDao(): AdministratorAccountDao
    abstract fun teacherDao(): TeacherDao
    abstract fun userDao(): UserDao
    abstract fun studentLinkRequestDao(): StudentLinkRequestDao
    abstract fun documentDao(): DocumentDao
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

val CMS_DATABASE_MIGRATIONS = arrayOf(
    MIGRATION_18_19,
    MIGRATION_19_20,
    MIGRATION_20_21,
    MIGRATION_21_22,
    MIGRATION_22_23,
    MIGRATION_23_24,
    MIGRATION_24_25,
    MIGRATION_25_26,
    MIGRATION_26_27,
    MIGRATION_27_28,
    MIGRATION_28_29,
    MIGRATION_29_30,
)
