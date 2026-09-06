package com.mbd.cmscommon.di

import androidx.room.Database
import androidx.room.TypeConverters
import com.mbd.cmscommon.data.local.CMS_DATABASE_VERSION
import com.mbd.cmscommon.data.local.CmsDatabase
import com.mbd.cmscommon.data.local.Converters
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
import com.mbd.cmscommon.data.local.entity.SessionAttendanceTallyEntity
import com.mbd.cmscommon.data.local.entity.SessionAttendanceRowEntity
import com.mbd.cmscommon.data.local.entity.SessionFeeEntity
import com.mbd.cmscommon.data.local.entity.SessionFeeHeadEntity
import com.mbd.cmscommon.data.local.entity.SessionMarkEntity
import com.mbd.cmscommon.data.local.entity.SessionPeriodEntity
import com.mbd.cmscommon.data.local.entity.SessionStudentEntity
import com.mbd.cmscommon.data.local.entity.StudentSemesterGpaEntity
import com.mbd.cmscommon.data.local.entity.StudentLinkRequestEntity
import com.mbd.cmscommon.data.local.entity.SyncStateEntity
import com.mbd.cmscommon.data.local.entity.TableSyncStateEntity
import com.mbd.cmscommon.data.local.entity.TeacherEntity
import com.mbd.cmscommon.data.local.entity.UserEntity

/**
 * The single concrete Room database for all mobile apps (admin/student/teacher). Room requires
 * the entity list to be a compile-time literal on the @Database-annotated class, but since that
 * list and the version are identical across apps, one shared class replaces what used to be three
 * near-duplicate ones (AdminDatabase/StudentDatabase/TeacherDatabase). Each app still opens its own
 * file (see per-app DatabaseModule) so their data stays isolated.
 *
 * Lives under `di/`, not `data/local/`, on purpose: desktop-shared's build.gradle.kts reuses the
 * `data/local` directory as a source set for its own (unrelated) DesktopDatabase, and Room's KSP
 * would still try to process a @Database class placed there even if compileKotlin excludes it.
 */
@Database(
    entities = [
        DepartmentEntity::class, TeacherEntity::class, AdministratorAccountEntity::class, UserEntity::class,
        StudentLinkRequestEntity::class,
        ExamPaperSubmissionEntity::class, NotificationEntity::class,
        SyncStateEntity::class, TableSyncStateEntity::class,
        AcademicSessionEntity::class, SemesterSubjectEntity::class, SessionStudentEntity::class,
        SessionPeriodEntity::class, SessionAttendanceTallyEntity::class, SessionAttendanceRowEntity::class,
        SessionMarkEntity::class, StudentSemesterGpaEntity::class,
        SessionFeeEntity::class, SessionFeeHeadEntity::class,
        FineEntity::class, CalendarEventEntity::class, MarkEditRequestEntity::class,
        InsightSessionOverviewEntity::class, InsightAtRiskStudentEntity::class, InsightExamStatEntity::class,
        DatesheetEntity::class, DatesheetSlotEntity::class,
        BuildingEntity::class, RoomEntity::class,
        AppLogEntity::class,
    ],
    version = CMS_DATABASE_VERSION,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class MobileCmsDatabase : CmsDatabase()
