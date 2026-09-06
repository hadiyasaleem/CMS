package com.mbd.cmsdesktop.data.local

import androidx.room.Database
import com.mbd.cmscommon.data.local.CmsDatabase
import com.mbd.cmsdesktop.data.local.dao.NotificationViewStateDao
import com.mbd.cmsdesktop.data.local.dao.DesktopAuthCodeVerifierDao
import com.mbd.cmsdesktop.data.local.dao.DesktopAuthSessionDao
import com.mbd.cmscommon.data.local.entity.AcademicSessionEntity
import com.mbd.cmscommon.data.local.entity.AdministratorAccountEntity
import com.mbd.cmscommon.data.local.entity.AppLogEntity
import com.mbd.cmscommon.data.local.entity.BuildingEntity
import com.mbd.cmscommon.data.local.entity.CalendarEventEntity
import com.mbd.cmscommon.data.local.entity.DatesheetEntity
import com.mbd.cmscommon.data.local.entity.DatesheetSlotEntity
import com.mbd.cmscommon.data.local.entity.DepartmentEntity
import com.mbd.cmscommon.data.local.entity.ExamPaperSubmissionEntity
import com.mbd.cmscommon.data.local.entity.FineEntity
import com.mbd.cmscommon.data.local.entity.InsightAtRiskStudentEntity
import com.mbd.cmscommon.data.local.entity.InsightExamStatEntity
import com.mbd.cmscommon.data.local.entity.InsightSessionOverviewEntity
import com.mbd.cmscommon.data.local.entity.MarkEditRequestEntity
import com.mbd.cmscommon.data.local.entity.NotificationEntity
import com.mbd.cmscommon.data.local.entity.RoomEntity
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
import com.mbd.cmsdesktop.data.local.entity.NotificationViewStateEntity
import com.mbd.cmsdesktop.data.local.entity.DesktopAuthCodeVerifierEntity
import com.mbd.cmsdesktop.data.local.entity.DesktopAuthSessionEntity

/** The structured Room database shared by the desktop admin, teacher, and student apps. */
@Database(
    entities = [
        DepartmentEntity::class, TeacherEntity::class, AdministratorAccountEntity::class, UserEntity::class,
        StudentLinkRequestEntity::class, ExamPaperSubmissionEntity::class, NotificationEntity::class,
        NotificationViewStateEntity::class,
        DesktopAuthSessionEntity::class, DesktopAuthCodeVerifierEntity::class,
        SyncStateEntity::class, TableSyncStateEntity::class, AcademicSessionEntity::class,
        SemesterSubjectEntity::class, SessionStudentEntity::class, SessionPeriodEntity::class,
        SessionAttendanceTallyEntity::class, SessionAttendanceRowEntity::class, SessionMarkEntity::class,
        StudentSemesterGpaEntity::class, SessionFeeEntity::class, SessionFeeHeadEntity::class,
        FineEntity::class, CalendarEventEntity::class, MarkEditRequestEntity::class,
        InsightSessionOverviewEntity::class, InsightAtRiskStudentEntity::class, InsightExamStatEntity::class,
        DatesheetEntity::class, DatesheetSlotEntity::class,
        BuildingEntity::class, RoomEntity::class,
        AppLogEntity::class,
    ],
    version = 6,
    // Schema export is disabled: Room 2.8.4's schema-bundle serializers are incompatible with the
    // project's kotlinx-serialization 1.8.0 (KSP AbstractMethodError in SchemaBundle.deserialize).
    // The desktop DB is a local cache with no Room migration tests, so exported schemas aren't needed.
    // Version bumps rely on DesktopRoomModule's fallbackToDestructiveMigration() instead — see there.
    exportSchema = false,
)
abstract class DesktopDatabase : CmsDatabase() {
    abstract fun notificationViewStateDao(): NotificationViewStateDao
    abstract fun desktopAuthSessionDao(): DesktopAuthSessionDao
    abstract fun desktopAuthCodeVerifierDao(): DesktopAuthCodeVerifierDao
}
