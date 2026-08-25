package com.mbd.cmsstudent.di

import androidx.room.Database
import androidx.room.TypeConverters
import com.mbd.cmscommon.data.local.CMS_DATABASE_VERSION
import com.mbd.cmscommon.data.local.CmsDatabase
import com.mbd.cmscommon.data.local.Converters
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
 * Student's concrete Room database — repeats the full shared entity list from CmsDatabase's
 * documented reference (a Room/annotation constraint: the @Database-annotated class itself must
 * list entities as a compile-time literal). Teacher/Student apps declare the same list but only
 * exercise a subset of the DAOs in practice.
 */
@Database(
    entities = [
        DepartmentEntity::class, TeacherEntity::class, AdministratorAccountEntity::class, UserEntity::class,
        StudentLinkRequestEntity::class, DocumentEntity::class,
        ExamPaperSubmissionEntity::class, NotificationEntity::class,
        SyncStateEntity::class, TableSyncStateEntity::class,
        AcademicSessionEntity::class, SemesterSubjectEntity::class, SessionStudentEntity::class,
        SessionPeriodEntity::class, SessionAttendanceTallyEntity::class, SessionAttendanceRowEntity::class,
        SessionMarkEntity::class, StudentSemesterGpaEntity::class,
        SessionFeeEntity::class, SessionFeeHeadEntity::class,
        FineEntity::class, CalendarEventEntity::class, MarkEditRequestEntity::class,
        InsightSessionOverviewEntity::class, InsightAtRiskStudentEntity::class, InsightExamStatEntity::class,
        DatesheetEntity::class, DatesheetSlotEntity::class,
    ],
    version = CMS_DATABASE_VERSION,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class StudentDatabase : CmsDatabase()
