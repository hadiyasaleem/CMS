package com.mbd.cmscommon.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mbd.cmscommon.data.remote.SupabaseTables

val MIGRATION_18_19: Migration = object : Migration(18, 19) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `table_sync_state` (
                `owner_key` TEXT NOT NULL,
                `table_name` TEXT NOT NULL,
                `scope_key` TEXT NOT NULL,
                `last_updated_at` TEXT NOT NULL,
                `last_successful_sync_at` TEXT NOT NULL,
                `created_at` INTEGER NOT NULL,
                `created_by` TEXT,
                `updated_at` INTEGER NOT NULL,
                `updated_by` TEXT,
                PRIMARY KEY(`owner_key`, `table_name`, `scope_key`)
            )
            """.trimIndent(),
        )
    }
}

val MIGRATION_19_20: Migration = object : Migration(19, 20) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `documents` (
                `documentId` TEXT NOT NULL,
                `kind` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `storagePath` TEXT,
                `body` TEXT,
                `deptId` TEXT,
                `audience` TEXT NOT NULL,
                `tagsJson` TEXT NOT NULL,
                `published` INTEGER NOT NULL,
                `publishedBy` TEXT,
                `entityId` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL,
                `updatedBy` TEXT,
                PRIMARY KEY(`documentId`)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_documents_kind_deptId_published` ON `documents` (`kind`, `deptId`, `published`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_documents_updatedAt_entityId` ON `documents` (`updatedAt`, `entityId`)")
    }
}

val MIGRATION_20_21: Migration = object : Migration(20, 21) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `session_marks` ADD COLUMN `entityId` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `session_marks` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `session_marks` ADD COLUMN `createdBy` TEXT")
        db.execSQL("ALTER TABLE `session_marks` ADD COLUMN `updatedAt` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `session_marks` ADD COLUMN `updatedBy` TEXT")
    }
}

val MIGRATION_21_22: Migration = object : Migration(21, 22) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `session_fees` (
                `sessionId` TEXT NOT NULL,
                `cadence` TEXT NOT NULL,
                `academicYear` TEXT,
                `dueDate` TEXT,
                `lateFineNote` TEXT,
                `paymentNote` TEXT,
                `entityId` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL,
                `updatedBy` TEXT,
                PRIMARY KEY(`sessionId`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `session_fee_heads` (
                `id` TEXT NOT NULL,
                `sessionId` TEXT NOT NULL,
                `label` TEXT NOT NULL,
                `amount` REAL NOT NULL,
                `position` INTEGER NOT NULL,
                `entityId` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL,
                `updatedBy` TEXT,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_fee_heads_sessionId_position` ON `session_fee_heads` (`sessionId`, `position`)")
    }
}

val MIGRATION_22_23: Migration = object : Migration(22, 23) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `fines` (
                `fineId` TEXT NOT NULL,
                `sessionId` TEXT NOT NULL,
                `rollNumber` TEXT NOT NULL,
                `category` TEXT NOT NULL,
                `amount` REAL NOT NULL,
                `reason` TEXT,
                `issuedBy` TEXT,
                `issuedAt` INTEGER NOT NULL,
                `entityId` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL,
                `updatedBy` TEXT,
                PRIMARY KEY(`fineId`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `calendar_events` (
                `eventId` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `eventType` TEXT NOT NULL,
                `startDate` TEXT NOT NULL,
                `endDate` TEXT,
                `startTime` TEXT,
                `endTime` TEXT,
                `description` TEXT,
                `venue` TEXT,
                `audience` TEXT NOT NULL,
                `deptId` TEXT,
                `sessionId` TEXT,
                `entityId` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL,
                `updatedBy` TEXT,
                PRIMARY KEY(`eventId`)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_fines_sessionId_rollNumber` ON `fines` (`sessionId`, `rollNumber`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_calendar_events_startDate` ON `calendar_events` (`startDate`)")
    }
}

val MIGRATION_23_24: Migration = object : Migration(23, 24) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `datesheets` (
                `datesheetId` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `examType` TEXT NOT NULL,
                `sessionId` TEXT,
                `published` INTEGER NOT NULL,
                `instructions` TEXT,
                `entityId` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL,
                `updatedBy` TEXT,
                PRIMARY KEY(`datesheetId`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `datesheet_slots` (
                `slotId` TEXT NOT NULL,
                `datesheetId` TEXT NOT NULL,
                `examDate` TEXT NOT NULL,
                `startTime` TEXT,
                `endTime` TEXT,
                `durationMinutes` INTEGER,
                `courseCode` TEXT,
                `subjectName` TEXT,
                `roomNo` TEXT,
                `building` TEXT,
                `invigilatorEmail` TEXT,
                `entityId` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL,
                `updatedBy` TEXT,
                PRIMARY KEY(`slotId`)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_datesheet_slots_datesheetId_examDate` ON `datesheet_slots` (`datesheetId`, `examDate`)")
    }
}

val MIGRATION_24_25: Migration = object : Migration(24, 25) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `student_semester_gpa` (
                `id` TEXT NOT NULL,
                `sessionId` TEXT NOT NULL,
                `rollNumber` TEXT NOT NULL,
                `semester` INTEGER NOT NULL,
                `gpa` REAL NOT NULL,
                `cgpa` REAL NOT NULL,
                `termLabel` TEXT,
                `resultStatus` TEXT NOT NULL,
                `classPosition` INTEGER,
                `remarks` TEXT,
                `supplyCoursesJson` TEXT NOT NULL,
                `entityId` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL,
                `updatedBy` TEXT,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_student_semester_gpa_sessionId_rollNumber_semester` ON `student_semester_gpa` (`sessionId`, `rollNumber`, `semester`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_student_semester_gpa_sessionId_semester_rollNumber` ON `student_semester_gpa` (`sessionId`, `semester`, `rollNumber`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_student_semester_gpa_sessionId_rollNumber_updatedAt_entityId` ON `student_semester_gpa` (`sessionId`, `rollNumber`, `updatedAt`, `entityId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_student_semester_gpa_sessionId_semester_updatedAt_entityId` ON `student_semester_gpa` (`sessionId`, `semester`, `updatedAt`, `entityId`)")
    }
}

val MIGRATION_25_26: Migration = object : Migration(25, 26) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `session_attendance_rows` (
                `id` TEXT NOT NULL,
                `sessionId` TEXT NOT NULL,
                `semester` INTEGER NOT NULL,
                `courseCode` TEXT NOT NULL,
                `date` TEXT NOT NULL,
                `rollNumber` TEXT NOT NULL,
                `status` TEXT NOT NULL,
                `teacherEmail` TEXT NOT NULL,
                `isLate` INTEGER NOT NULL,
                `remark` TEXT,
                `lectureTopic` TEXT,
                `recordedAt` INTEGER NOT NULL,
                `entityId` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL,
                `updatedBy` TEXT,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_attendance_rows_sessionId_courseCode_date_rollNumber` ON `session_attendance_rows` (`sessionId`, `courseCode`, `date`, `rollNumber`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_attendance_rows_sessionId_courseCode_updatedAt_entityId` ON `session_attendance_rows` (`sessionId`, `courseCode`, `updatedAt`, `entityId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_attendance_rows_sessionId_updatedAt_entityId` ON `session_attendance_rows` (`sessionId`, `updatedAt`, `entityId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_attendance_rows_sessionId_semester` ON `session_attendance_rows` (`sessionId`, `semester`)")
    }
}

val MIGRATION_26_27: Migration = object : Migration(26, 27) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `mark_edit_requests` (
                `requestId` TEXT NOT NULL,
                `sessionId` TEXT NOT NULL,
                `semester` INTEGER NOT NULL,
                `courseCode` TEXT NOT NULL,
                `examType` TEXT NOT NULL,
                `rollNumber` TEXT NOT NULL,
                `currentScore` INTEGER,
                `requestedScore` INTEGER NOT NULL,
                `reason` TEXT,
                `status` TEXT NOT NULL,
                `requestedBy` TEXT NOT NULL,
                `reviewedBy` TEXT,
                `requestedAt` INTEGER NOT NULL,
                `reviewedAt` INTEGER,
                `entityId` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL,
                `updatedBy` TEXT,
                PRIMARY KEY(`requestId`)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_mark_edit_requests_sessionId_courseCode_examType_status_rollNumber` ON `mark_edit_requests` (`sessionId`, `courseCode`, `examType`, `status`, `rollNumber`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_mark_edit_requests_sessionId_courseCode_examType_status_updatedAt_entityId` ON `mark_edit_requests` (`sessionId`, `courseCode`, `examType`, `status`, `updatedAt`, `entityId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_mark_edit_requests_status_requestedAt` ON `mark_edit_requests` (`status`, `requestedAt`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_mark_edit_requests_status_updatedAt_entityId` ON `mark_edit_requests` (`status`, `updatedAt`, `entityId`)")
    }
}

val MIGRATION_27_28: Migration = object : Migration(27, 28) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `insight_session_overviews` (
                `sessionId` TEXT NOT NULL,
                `deptId` TEXT NOT NULL,
                `shift` TEXT NOT NULL,
                `currentSemester` INTEGER NOT NULL,
                `students` INTEGER NOT NULL,
                `avgCgpa` REAL,
                `avgAttendance` REAL,
                `cachedAt` INTEGER NOT NULL,
                PRIMARY KEY(`sessionId`)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_insight_session_overviews_deptId_sessionId` ON `insight_session_overviews` (`deptId`, `sessionId`)")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `insight_at_risk_students` (
                `id` TEXT NOT NULL,
                `sessionId` TEXT NOT NULL,
                `rollNumber` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `cgpa` REAL,
                `attendance` REAL,
                `cachedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_insight_at_risk_students_sessionId_rollNumber` ON `insight_at_risk_students` (`sessionId`, `rollNumber`)")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `insight_exam_stats` (
                `id` TEXT NOT NULL,
                `sessionId` TEXT NOT NULL,
                `semester` INTEGER NOT NULL,
                `courseCode` TEXT NOT NULL,
                `examType` TEXT NOT NULL,
                `entered` INTEGER NOT NULL,
                `avgScore` REAL,
                `minScore` INTEGER,
                `maxScore` INTEGER,
                `stddev` REAL,
                `outOf` INTEGER NOT NULL,
                `passRate` REAL,
                `cachedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_insight_exam_stats_sessionId_semester_courseCode_examType` ON `insight_exam_stats` (`sessionId`, `semester`, `courseCode`, `examType`)")
    }
}

val MIGRATION_28_29: Migration = object : Migration(28, 29) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `administrator_accounts` (
                `id` TEXT NOT NULL,
                `email` TEXT NOT NULL,
                `status` TEXT NOT NULL,
                `lastLoginAt` INTEGER,
                `entityId` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL,
                `updatedBy` TEXT,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_administrator_accounts_email` ON `administrator_accounts` (`email`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_administrator_accounts_updatedAt_entityId` ON `administrator_accounts` (`updatedAt`, `entityId`)")
    }
}

private val SOFT_DELETE_TABLES = listOf(
    SupabaseTables.ACADEMIC_SESSIONS,
    "administrator_accounts",
    SupabaseTables.CALENDAR_EVENTS,
    SupabaseTables.DATESHEETS,
    SupabaseTables.DATESHEET_SLOTS,
    SupabaseTables.DEPARTMENTS,
    "documents",
    SupabaseTables.EXAM_PAPER_SUBMISSIONS,
    SupabaseTables.FINES,
    SupabaseTables.MARK_EDIT_REQUESTS,
    SupabaseTables.NOTIFICATIONS,
    "semester_subjects",
    "session_attendance_rows",
    SupabaseTables.SESSION_FEE_HEADS,
    SupabaseTables.SESSION_FEES,
    SupabaseTables.SESSION_MARKS,
    "session_periods",
    SupabaseTables.SESSION_STUDENTS,
    SupabaseTables.STUDENT_LINK_REQUESTS,
    SupabaseTables.STUDENT_SEMESTER_GPA,
    SupabaseTables.TEACHERS,
)

val MIGRATION_29_30: Migration = object : Migration(29, 30) {
    override fun migrate(db: SupportSQLiteDatabase) {
        SOFT_DELETE_TABLES.forEach { table ->
            db.execSQL("ALTER TABLE `$table` ADD COLUMN `isDeleted` INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE `$table` ADD COLUMN `deletedAt` INTEGER")
            db.execSQL("ALTER TABLE `$table` ADD COLUMN `deletedBy` TEXT")
        }
    }
}

// Documents feature removed: drop the local cache table (and its indices, dropped implicitly).
val MIGRATION_30_31: Migration = object : Migration(30, 31) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS `documents`")
    }
}

/**
 * Persists an independent high-water mark for each Supabase table and query scope. The
 * startup-bootstrap marker deliberately remains in sync_state: completing one bootstrap must
 * never advance a table that failed part-way through an incremental refresh.
 */
val MIGRATION_31_32: Migration = object : Migration(31, 32) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `table_sync_state` (
                `owner_key` TEXT NOT NULL,
                `table_name` TEXT NOT NULL,
                `scope_key` TEXT NOT NULL,
                `last_updated_at` TEXT NOT NULL,
                `last_successful_sync_at` TEXT NOT NULL,
                `created_at` INTEGER NOT NULL,
                `created_by` TEXT,
                `updated_at` INTEGER NOT NULL,
                `updated_by` TEXT,
                PRIMARY KEY(`owner_key`, `table_name`, `scope_key`)
            )
            """.trimIndent(),
        )
    }
}

/** Adds a compact durable copy of the full student profile returned by session_students. */
val MIGRATION_32_33: Migration = object : Migration(32, 33) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `session_students` ADD COLUMN `profileJson` TEXT")
    }
}

/** Adds the teachers columns (auth_uid/is_admin/is_hod/photo_path) that already existed on the
 * Postgres table but had no local Room representation. */
val MIGRATION_33_34: Migration = object : Migration(33, 34) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `teachers` ADD COLUMN `authUid` TEXT")
        db.execSQL("ALTER TABLE `teachers` ADD COLUMN `isAdmin` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `teachers` ADD COLUMN `isHod` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `teachers` ADD COLUMN `photoPath` TEXT")
    }
}

/** Adds the exam-paper review columns (review_status/reviewed_by/reviewed_at/teacher_notes/
 * key_storage_path/mime_type) that back the admin review workflow. */
val MIGRATION_34_35: Migration = object : Migration(34, 35) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `exam_paper_submissions` ADD COLUMN `mimeType` TEXT")
        db.execSQL("ALTER TABLE `exam_paper_submissions` ADD COLUMN `keyStoragePath` TEXT")
        db.execSQL("ALTER TABLE `exam_paper_submissions` ADD COLUMN `teacherNotes` TEXT")
        db.execSQL("ALTER TABLE `exam_paper_submissions` ADD COLUMN `reviewStatus` TEXT NOT NULL DEFAULT 'SUBMITTED'")
        db.execSQL("ALTER TABLE `exam_paper_submissions` ADD COLUMN `reviewedBy` TEXT")
        db.execSQL("ALTER TABLE `exam_paper_submissions` ADD COLUMN `reviewedAt` INTEGER")
    }
}

/** Drops archived_at from academic_sessions and departments — never written by any code path,
 * so the check-the-column-then-fall-back-to-isActive logic it backed was always vacuous. Kept on
 * teachers, where set-teacher-status actually writes it as a soft-delete timestamp. SQLite on
 * pre-3.35 devices has no DROP COLUMN, so rebuild each table without it. */
val MIGRATION_35_36: Migration = object : Migration(35, 36) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE `academic_sessions_new` (
                `sessionId` TEXT NOT NULL,
                `deptId` TEXT NOT NULL,
                `startYear` INTEGER NOT NULL,
                `endYear` INTEGER NOT NULL,
                `shift` TEXT NOT NULL,
                `currentSemester` INTEGER NOT NULL,
                `isActive` INTEGER NOT NULL DEFAULT 1,
                `programName` TEXT,
                `inchargeEmail` TEXT,
                `maxStudents` INTEGER NOT NULL,
                `entityId` INTEGER NOT NULL DEFAULT 0,
                `createdAt` INTEGER NOT NULL DEFAULT 0,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL DEFAULT 0,
                `updatedBy` TEXT,
                `isDeleted` INTEGER NOT NULL DEFAULT 0,
                `deletedAt` INTEGER,
                `deletedBy` TEXT,
                PRIMARY KEY(`sessionId`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `academic_sessions_new`
            (`sessionId`,`deptId`,`startYear`,`endYear`,`shift`,`currentSemester`,`isActive`,
             `programName`,`inchargeEmail`,`maxStudents`,`entityId`,`createdAt`,`createdBy`,
             `updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`)
            SELECT
             `sessionId`,`deptId`,`startYear`,`endYear`,`shift`,`currentSemester`,`isActive`,
             `programName`,`inchargeEmail`,`maxStudents`,`entityId`,`createdAt`,`createdBy`,
             `updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`
            FROM `academic_sessions`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `academic_sessions`")
        db.execSQL("ALTER TABLE `academic_sessions_new` RENAME TO `academic_sessions`")

        db.execSQL(
            """
            CREATE TABLE `departments_new` (
                `deptId` TEXT NOT NULL,
                `entityId` INTEGER NOT NULL DEFAULT 0,
                `name` TEXT NOT NULL,
                `code` TEXT NOT NULL,
                `hodEmail` TEXT,
                `description` TEXT,
                `isActive` INTEGER NOT NULL DEFAULT 1,
                `createdAt` INTEGER NOT NULL DEFAULT 0,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL DEFAULT 0,
                `updatedBy` TEXT,
                `isDeleted` INTEGER NOT NULL DEFAULT 0,
                `deletedAt` INTEGER,
                `deletedBy` TEXT,
                PRIMARY KEY(`deptId`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `departments_new`
            (`deptId`,`entityId`,`name`,`code`,`hodEmail`,`description`,`isActive`,
             `createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`)
            SELECT
             `deptId`,`entityId`,`name`,`code`,`hodEmail`,`description`,`isActive`,
             `createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`
            FROM `departments`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `departments`")
        db.execSQL("ALTER TABLE `departments_new` RENAME TO `departments`")
    }
}

/** Drops archived_at from teachers too — it was write-only (set on delete, never read anywhere)
 * and, per the same audit, is_active/status already do the actual lifecycle work. */
val MIGRATION_36_37: Migration = object : Migration(36, 37) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE `teachers_new` (
                `teacherId` TEXT NOT NULL,
                `entityId` INTEGER NOT NULL DEFAULT 0,
                `name` TEXT NOT NULL,
                `email` TEXT NOT NULL,
                `phone` TEXT,
                `deptId` TEXT,
                `designation` TEXT,
                `qualification` TEXT,
                `specialization` TEXT,
                `officeRoom` TEXT,
                `gender` TEXT,
                `authUid` TEXT,
                `isAdmin` INTEGER NOT NULL DEFAULT 0,
                `isHod` INTEGER NOT NULL DEFAULT 0,
                `photoPath` TEXT,
                `canApproveLinkRequests` INTEGER NOT NULL DEFAULT 0,
                `canEditTimetable` INTEGER NOT NULL DEFAULT 0,
                `canSendNotifications` INTEGER NOT NULL DEFAULT 0,
                `canManageDatesheets` INTEGER NOT NULL DEFAULT 0,
                `status` TEXT NOT NULL,
                `isActive` INTEGER NOT NULL DEFAULT 1,
                `createdAt` INTEGER NOT NULL DEFAULT 0,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL DEFAULT 0,
                `updatedBy` TEXT,
                `isDeleted` INTEGER NOT NULL DEFAULT 0,
                `deletedAt` INTEGER,
                `deletedBy` TEXT,
                PRIMARY KEY(`teacherId`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `teachers_new`
            (`teacherId`,`entityId`,`name`,`email`,`phone`,`deptId`,`designation`,`qualification`,
             `specialization`,`officeRoom`,`gender`,`authUid`,`isAdmin`,`isHod`,`photoPath`,
             `canApproveLinkRequests`,`canEditTimetable`,`canSendNotifications`,`canManageDatesheets`,
             `status`,`isActive`,`createdAt`,`createdBy`,`updatedAt`,`updatedBy`,
             `isDeleted`,`deletedAt`,`deletedBy`)
            SELECT
             `teacherId`,`entityId`,`name`,`email`,`phone`,`deptId`,`designation`,`qualification`,
             `specialization`,`officeRoom`,`gender`,`authUid`,`isAdmin`,`isHod`,`photoPath`,
             `canApproveLinkRequests`,`canEditTimetable`,`canSendNotifications`,`canManageDatesheets`,
             `status`,`isActive`,`createdAt`,`createdBy`,`updatedAt`,`updatedBy`,
             `isDeleted`,`deletedAt`,`deletedBy`
            FROM `teachers`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `teachers`")
        db.execSQL("ALTER TABLE `teachers_new` RENAME TO `teachers`")
    }
}

/** Drops departments.is_active — the local query filtered on it, but nothing ever writes it
 * false (no "deactivate department" feature exists); it was a permanent tautology. Also drops
 * session_students' is_active from the profileJson blob (pure JSON, no schema migration needed
 * for that half) — enrollment_status is the real student lifecycle field. */
val MIGRATION_37_38: Migration = object : Migration(37, 38) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE `departments_new` (
                `deptId` TEXT NOT NULL,
                `entityId` INTEGER NOT NULL DEFAULT 0,
                `name` TEXT NOT NULL,
                `code` TEXT NOT NULL,
                `hodEmail` TEXT,
                `description` TEXT,
                `createdAt` INTEGER NOT NULL DEFAULT 0,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL DEFAULT 0,
                `updatedBy` TEXT,
                `isDeleted` INTEGER NOT NULL DEFAULT 0,
                `deletedAt` INTEGER,
                `deletedBy` TEXT,
                PRIMARY KEY(`deptId`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `departments_new`
            (`deptId`,`entityId`,`name`,`code`,`hodEmail`,`description`,
             `createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`)
            SELECT
             `deptId`,`entityId`,`name`,`code`,`hodEmail`,`description`,
             `createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`
            FROM `departments`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `departments`")
        db.execSQL("ALTER TABLE `departments_new` RENAME TO `departments`")
    }
}

/** Drops entityId (the `bigint generated always as identity` surrogate key added by an earlier
 * migration) from every local table except session_attendance_rows, matching the Supabase side
 * where the column was dropped from all tables except session_attendance — there it's genuinely
 * used as a sync-page tie-breaker (see SessionAttendanceRepositoryImpl). SQLite has no DROP COLUMN
 * pre-3.35, so each affected table is rebuilt without the column. */
val MIGRATION_38_39: Migration = object : Migration(38, 39) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE `academic_sessions_new` (
                `sessionId` TEXT NOT NULL,
                `deptId` TEXT NOT NULL,
                `startYear` INTEGER NOT NULL,
                `endYear` INTEGER NOT NULL,
                `shift` TEXT NOT NULL,
                `currentSemester` INTEGER NOT NULL,
                `isActive` INTEGER NOT NULL DEFAULT 1,
                `programName` TEXT,
                `inchargeEmail` TEXT,
                `maxStudents` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL DEFAULT 0,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL DEFAULT 0,
                `updatedBy` TEXT,
                `isDeleted` INTEGER NOT NULL DEFAULT 0,
                `deletedAt` INTEGER,
                `deletedBy` TEXT,
                PRIMARY KEY(`sessionId`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `academic_sessions_new`
            (`sessionId`,`deptId`,`startYear`,`endYear`,`shift`,`currentSemester`,`isActive`,
             `programName`,`inchargeEmail`,`maxStudents`,`createdAt`,`createdBy`,
             `updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`)
            SELECT
             `sessionId`,`deptId`,`startYear`,`endYear`,`shift`,`currentSemester`,`isActive`,
             `programName`,`inchargeEmail`,`maxStudents`,`createdAt`,`createdBy`,
             `updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`
            FROM `academic_sessions`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `academic_sessions`")
        db.execSQL("ALTER TABLE `academic_sessions_new` RENAME TO `academic_sessions`")

        db.execSQL(
            """
            CREATE TABLE `semester_subjects_new` (
                `id` TEXT NOT NULL,
                `sessionId` TEXT NOT NULL,
                `semester` INTEGER NOT NULL,
                `courseCode` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `creditHours` INTEGER NOT NULL,
                `subjectType` TEXT NOT NULL,
                `isElective` INTEGER NOT NULL DEFAULT 0,
                `outline` TEXT,
                `createdAt` INTEGER NOT NULL DEFAULT 0,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL DEFAULT 0,
                `updatedBy` TEXT,
                `isDeleted` INTEGER NOT NULL DEFAULT 0,
                `deletedAt` INTEGER,
                `deletedBy` TEXT,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `semester_subjects_new`
            (`id`,`sessionId`,`semester`,`courseCode`,`name`,`creditHours`,`subjectType`,`isElective`,
             `outline`,`createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`)
            SELECT
             `id`,`sessionId`,`semester`,`courseCode`,`name`,`creditHours`,`subjectType`,`isElective`,
             `outline`,`createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`
            FROM `semester_subjects`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `semester_subjects`")
        db.execSQL("ALTER TABLE `semester_subjects_new` RENAME TO `semester_subjects`")

        db.execSQL(
            """
            CREATE TABLE `session_marks_new` (
                `id` TEXT NOT NULL,
                `sessionId` TEXT NOT NULL,
                `courseCode` TEXT NOT NULL,
                `examType` TEXT NOT NULL,
                `rollNumber` TEXT NOT NULL,
                `score` INTEGER NOT NULL,
                `maxMarks` INTEGER NOT NULL,
                `wasAbsent` INTEGER NOT NULL DEFAULT 0,
                `remarks` TEXT,
                `createdAt` INTEGER NOT NULL DEFAULT 0,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL DEFAULT 0,
                `updatedBy` TEXT,
                `isDeleted` INTEGER NOT NULL DEFAULT 0,
                `deletedAt` INTEGER,
                `deletedBy` TEXT,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `session_marks_new`
            (`id`,`sessionId`,`courseCode`,`examType`,`rollNumber`,`score`,`maxMarks`,`wasAbsent`,
             `remarks`,`createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`)
            SELECT
             `id`,`sessionId`,`courseCode`,`examType`,`rollNumber`,`score`,`maxMarks`,`wasAbsent`,
             `remarks`,`createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`
            FROM `session_marks`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `session_marks`")
        db.execSQL("ALTER TABLE `session_marks_new` RENAME TO `session_marks`")

        db.execSQL(
            """
            CREATE TABLE `session_periods_new` (
                `id` TEXT NOT NULL,
                `sessionId` TEXT NOT NULL,
                `deptId` TEXT NOT NULL,
                `day` TEXT NOT NULL,
                `startTime` TEXT,
                `endTime` TEXT,
                `courseCode` TEXT,
                `subjectName` TEXT,
                `teacherId` TEXT,
                `teacherName` TEXT,
                `periodType` TEXT NOT NULL,
                `creditHours` INTEGER,
                `roomNo` TEXT,
                `building` TEXT,
                `notes` TEXT,
                `effectiveFrom` TEXT,
                `effectiveTo` TEXT,
                `createdAt` INTEGER NOT NULL DEFAULT 0,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL DEFAULT 0,
                `updatedBy` TEXT,
                `isDeleted` INTEGER NOT NULL DEFAULT 0,
                `deletedAt` INTEGER,
                `deletedBy` TEXT,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `session_periods_new`
            (`id`,`sessionId`,`deptId`,`day`,`startTime`,`endTime`,`courseCode`,`subjectName`,
             `teacherId`,`teacherName`,`periodType`,`creditHours`,`roomNo`,`building`,`notes`,
             `effectiveFrom`,`effectiveTo`,`createdAt`,`createdBy`,`updatedAt`,`updatedBy`,
             `isDeleted`,`deletedAt`,`deletedBy`)
            SELECT
             `id`,`sessionId`,`deptId`,`day`,`startTime`,`endTime`,`courseCode`,`subjectName`,
             `teacherId`,`teacherName`,`periodType`,`creditHours`,`roomNo`,`building`,`notes`,
             `effectiveFrom`,`effectiveTo`,`createdAt`,`createdBy`,`updatedAt`,`updatedBy`,
             `isDeleted`,`deletedAt`,`deletedBy`
            FROM `session_periods`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `session_periods`")
        db.execSQL("ALTER TABLE `session_periods_new` RENAME TO `session_periods`")

        db.execSQL(
            """
            CREATE TABLE `session_students_new` (
                `id` TEXT NOT NULL,
                `sessionId` TEXT NOT NULL,
                `deptId` TEXT NOT NULL,
                `rollNumber` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `linkedEmail` TEXT,
                `gpa` REAL,
                `cgpa` REAL,
                `profileJson` TEXT,
                `createdAt` INTEGER NOT NULL DEFAULT 0,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL DEFAULT 0,
                `updatedBy` TEXT,
                `isDeleted` INTEGER NOT NULL DEFAULT 0,
                `deletedAt` INTEGER,
                `deletedBy` TEXT,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `session_students_new`
            (`id`,`sessionId`,`deptId`,`rollNumber`,`name`,`linkedEmail`,`gpa`,`cgpa`,`profileJson`,
             `createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`)
            SELECT
             `id`,`sessionId`,`deptId`,`rollNumber`,`name`,`linkedEmail`,`gpa`,`cgpa`,`profileJson`,
             `createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`
            FROM `session_students`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `session_students`")
        db.execSQL("ALTER TABLE `session_students_new` RENAME TO `session_students`")

        db.execSQL(
            """
            CREATE TABLE `student_semester_gpa_new` (
                `id` TEXT NOT NULL,
                `sessionId` TEXT NOT NULL,
                `rollNumber` TEXT NOT NULL,
                `semester` INTEGER NOT NULL,
                `gpa` REAL NOT NULL,
                `cgpa` REAL NOT NULL,
                `termLabel` TEXT,
                `resultStatus` TEXT NOT NULL,
                `classPosition` INTEGER,
                `remarks` TEXT,
                `supplyCoursesJson` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL DEFAULT 0,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL DEFAULT 0,
                `updatedBy` TEXT,
                `isDeleted` INTEGER NOT NULL DEFAULT 0,
                `deletedAt` INTEGER,
                `deletedBy` TEXT,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `student_semester_gpa_new`
            (`id`,`sessionId`,`rollNumber`,`semester`,`gpa`,`cgpa`,`termLabel`,`resultStatus`,
             `classPosition`,`remarks`,`supplyCoursesJson`,`createdAt`,`createdBy`,`updatedAt`,
             `updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`)
            SELECT
             `id`,`sessionId`,`rollNumber`,`semester`,`gpa`,`cgpa`,`termLabel`,`resultStatus`,
             `classPosition`,`remarks`,`supplyCoursesJson`,`createdAt`,`createdBy`,`updatedAt`,
             `updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`
            FROM `student_semester_gpa`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `student_semester_gpa`")
        db.execSQL("ALTER TABLE `student_semester_gpa_new` RENAME TO `student_semester_gpa`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_student_semester_gpa_sessionId_rollNumber_semester` ON `student_semester_gpa` (`sessionId`, `rollNumber`, `semester`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_student_semester_gpa_sessionId_semester_rollNumber` ON `student_semester_gpa` (`sessionId`, `semester`, `rollNumber`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_student_semester_gpa_sessionId_rollNumber_updatedAt` ON `student_semester_gpa` (`sessionId`, `rollNumber`, `updatedAt`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_student_semester_gpa_sessionId_semester_updatedAt` ON `student_semester_gpa` (`sessionId`, `semester`, `updatedAt`)")

        db.execSQL(
            """
            CREATE TABLE `administrator_accounts_new` (
                `id` TEXT NOT NULL,
                `email` TEXT NOT NULL,
                `status` TEXT NOT NULL,
                `lastLoginAt` INTEGER,
                `createdAt` INTEGER NOT NULL DEFAULT 0,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL DEFAULT 0,
                `updatedBy` TEXT,
                `isDeleted` INTEGER NOT NULL DEFAULT 0,
                `deletedAt` INTEGER,
                `deletedBy` TEXT,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `administrator_accounts_new`
            (`id`,`email`,`status`,`lastLoginAt`,`createdAt`,`createdBy`,`updatedAt`,`updatedBy`,
             `isDeleted`,`deletedAt`,`deletedBy`)
            SELECT
             `id`,`email`,`status`,`lastLoginAt`,`createdAt`,`createdBy`,`updatedAt`,`updatedBy`,
             `isDeleted`,`deletedAt`,`deletedBy`
            FROM `administrator_accounts`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `administrator_accounts`")
        db.execSQL("ALTER TABLE `administrator_accounts_new` RENAME TO `administrator_accounts`")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_administrator_accounts_email` ON `administrator_accounts` (`email`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_administrator_accounts_updatedAt` ON `administrator_accounts` (`updatedAt`)")

        db.execSQL(
            """
            CREATE TABLE `datesheets_new` (
                `datesheetId` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `examType` TEXT NOT NULL,
                `sessionId` TEXT,
                `published` INTEGER NOT NULL DEFAULT 0,
                `instructions` TEXT,
                `createdAt` INTEGER NOT NULL DEFAULT 0,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL DEFAULT 0,
                `updatedBy` TEXT,
                `isDeleted` INTEGER NOT NULL DEFAULT 0,
                `deletedAt` INTEGER,
                `deletedBy` TEXT,
                PRIMARY KEY(`datesheetId`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `datesheets_new`
            (`datesheetId`,`title`,`examType`,`sessionId`,`published`,`instructions`,
             `createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`)
            SELECT
             `datesheetId`,`title`,`examType`,`sessionId`,`published`,`instructions`,
             `createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`
            FROM `datesheets`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `datesheets`")
        db.execSQL("ALTER TABLE `datesheets_new` RENAME TO `datesheets`")

        db.execSQL(
            """
            CREATE TABLE `datesheet_slots_new` (
                `slotId` TEXT NOT NULL,
                `datesheetId` TEXT NOT NULL,
                `examDate` TEXT NOT NULL,
                `startTime` TEXT,
                `endTime` TEXT,
                `durationMinutes` INTEGER,
                `courseCode` TEXT,
                `subjectName` TEXT,
                `roomNo` TEXT,
                `building` TEXT,
                `invigilatorEmail` TEXT,
                `createdAt` INTEGER NOT NULL DEFAULT 0,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL DEFAULT 0,
                `updatedBy` TEXT,
                `isDeleted` INTEGER NOT NULL DEFAULT 0,
                `deletedAt` INTEGER,
                `deletedBy` TEXT,
                PRIMARY KEY(`slotId`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `datesheet_slots_new`
            (`slotId`,`datesheetId`,`examDate`,`startTime`,`endTime`,`durationMinutes`,`courseCode`,
             `subjectName`,`roomNo`,`building`,`invigilatorEmail`,`createdAt`,`createdBy`,
             `updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`)
            SELECT
             `slotId`,`datesheetId`,`examDate`,`startTime`,`endTime`,`durationMinutes`,`courseCode`,
             `subjectName`,`roomNo`,`building`,`invigilatorEmail`,`createdAt`,`createdBy`,
             `updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`
            FROM `datesheet_slots`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `datesheet_slots`")
        db.execSQL("ALTER TABLE `datesheet_slots_new` RENAME TO `datesheet_slots`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_datesheet_slots_datesheetId_examDate` ON `datesheet_slots` (`datesheetId`, `examDate`)")

        db.execSQL(
            """
            CREATE TABLE `departments_new` (
                `deptId` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `code` TEXT NOT NULL,
                `hodEmail` TEXT,
                `description` TEXT,
                `createdAt` INTEGER NOT NULL DEFAULT 0,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL DEFAULT 0,
                `updatedBy` TEXT,
                `isDeleted` INTEGER NOT NULL DEFAULT 0,
                `deletedAt` INTEGER,
                `deletedBy` TEXT,
                PRIMARY KEY(`deptId`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `departments_new`
            (`deptId`,`name`,`code`,`hodEmail`,`description`,
             `createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`)
            SELECT
             `deptId`,`name`,`code`,`hodEmail`,`description`,
             `createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`
            FROM `departments`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `departments`")
        db.execSQL("ALTER TABLE `departments_new` RENAME TO `departments`")

        db.execSQL(
            """
            CREATE TABLE `exam_paper_submissions_new` (
                `submissionId` TEXT NOT NULL,
                `offeringId` TEXT NOT NULL,
                `subjectId` TEXT NOT NULL,
                `examType` TEXT NOT NULL,
                `teacherId` TEXT NOT NULL,
                `storagePath` TEXT,
                `fileName` TEXT,
                `uploadedAt` INTEGER NOT NULL,
                `mimeType` TEXT,
                `keyStoragePath` TEXT,
                `teacherNotes` TEXT,
                `reviewStatus` TEXT NOT NULL DEFAULT 'SUBMITTED',
                `reviewedBy` TEXT,
                `reviewedAt` INTEGER,
                `createdBy` TEXT,
                `createdAt` INTEGER NOT NULL DEFAULT 0,
                `updatedAt` INTEGER NOT NULL DEFAULT 0,
                `updatedBy` TEXT,
                `isDeleted` INTEGER NOT NULL DEFAULT 0,
                `deletedAt` INTEGER,
                `deletedBy` TEXT,
                PRIMARY KEY(`submissionId`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `exam_paper_submissions_new`
            (`submissionId`,`offeringId`,`subjectId`,`examType`,`teacherId`,`storagePath`,`fileName`,
             `uploadedAt`,`mimeType`,`keyStoragePath`,`teacherNotes`,`reviewStatus`,`reviewedBy`,
             `reviewedAt`,`createdBy`,`createdAt`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`)
            SELECT
             `submissionId`,`offeringId`,`subjectId`,`examType`,`teacherId`,`storagePath`,`fileName`,
             `uploadedAt`,`mimeType`,`keyStoragePath`,`teacherNotes`,`reviewStatus`,`reviewedBy`,
             `reviewedAt`,`createdBy`,`createdAt`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`
            FROM `exam_paper_submissions`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `exam_paper_submissions`")
        db.execSQL("ALTER TABLE `exam_paper_submissions_new` RENAME TO `exam_paper_submissions`")

        db.execSQL(
            """
            CREATE TABLE `notifications_new` (
                `notificationId` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `body` TEXT,
                `targetRole` TEXT NOT NULL,
                `targetOfferingId` TEXT,
                `createdByUid` TEXT,
                `priority` TEXT NOT NULL,
                `targetDeptId` TEXT,
                `attachmentPath` TEXT,
                `expiresAt` INTEGER,
                `createdAt` INTEGER NOT NULL DEFAULT 0,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL DEFAULT 0,
                `updatedBy` TEXT,
                `isDeleted` INTEGER NOT NULL DEFAULT 0,
                `deletedAt` INTEGER,
                `deletedBy` TEXT,
                PRIMARY KEY(`notificationId`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `notifications_new`
            (`notificationId`,`title`,`body`,`targetRole`,`targetOfferingId`,`createdByUid`,`priority`,
             `targetDeptId`,`attachmentPath`,`expiresAt`,`createdAt`,`createdBy`,`updatedAt`,`updatedBy`,
             `isDeleted`,`deletedAt`,`deletedBy`)
            SELECT
             `notificationId`,`title`,`body`,`targetRole`,`targetOfferingId`,`createdByUid`,`priority`,
             `targetDeptId`,`attachmentPath`,`expiresAt`,`createdAt`,`createdBy`,`updatedAt`,`updatedBy`,
             `isDeleted`,`deletedAt`,`deletedBy`
            FROM `notifications`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `notifications`")
        db.execSQL("ALTER TABLE `notifications_new` RENAME TO `notifications`")

        db.execSQL(
            """
            CREATE TABLE `calendar_events_new` (
                `eventId` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `eventType` TEXT NOT NULL,
                `startDate` TEXT NOT NULL,
                `endDate` TEXT,
                `startTime` TEXT,
                `endTime` TEXT,
                `description` TEXT,
                `venue` TEXT,
                `audience` TEXT NOT NULL,
                `deptId` TEXT,
                `sessionId` TEXT,
                `createdAt` INTEGER NOT NULL DEFAULT 0,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL DEFAULT 0,
                `updatedBy` TEXT,
                `isDeleted` INTEGER NOT NULL DEFAULT 0,
                `deletedAt` INTEGER,
                `deletedBy` TEXT,
                PRIMARY KEY(`eventId`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `calendar_events_new`
            (`eventId`,`title`,`eventType`,`startDate`,`endDate`,`startTime`,`endTime`,`description`,
             `venue`,`audience`,`deptId`,`sessionId`,`createdAt`,`createdBy`,`updatedAt`,`updatedBy`,
             `isDeleted`,`deletedAt`,`deletedBy`)
            SELECT
             `eventId`,`title`,`eventType`,`startDate`,`endDate`,`startTime`,`endTime`,`description`,
             `venue`,`audience`,`deptId`,`sessionId`,`createdAt`,`createdBy`,`updatedAt`,`updatedBy`,
             `isDeleted`,`deletedAt`,`deletedBy`
            FROM `calendar_events`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `calendar_events`")
        db.execSQL("ALTER TABLE `calendar_events_new` RENAME TO `calendar_events`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_calendar_events_startDate` ON `calendar_events` (`startDate`)")

        db.execSQL(
            """
            CREATE TABLE `fines_new` (
                `fineId` TEXT NOT NULL,
                `sessionId` TEXT NOT NULL,
                `rollNumber` TEXT NOT NULL,
                `category` TEXT NOT NULL,
                `amount` REAL NOT NULL,
                `reason` TEXT,
                `issuedBy` TEXT,
                `issuedAt` INTEGER,
                `createdAt` INTEGER NOT NULL DEFAULT 0,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL DEFAULT 0,
                `updatedBy` TEXT,
                `isDeleted` INTEGER NOT NULL DEFAULT 0,
                `deletedAt` INTEGER,
                `deletedBy` TEXT,
                PRIMARY KEY(`fineId`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `fines_new`
            (`fineId`,`sessionId`,`rollNumber`,`category`,`amount`,`reason`,`issuedBy`,`issuedAt`,
             `createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`)
            SELECT
             `fineId`,`sessionId`,`rollNumber`,`category`,`amount`,`reason`,`issuedBy`,`issuedAt`,
             `createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`
            FROM `fines`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `fines`")
        db.execSQL("ALTER TABLE `fines_new` RENAME TO `fines`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_fines_sessionId_rollNumber` ON `fines` (`sessionId`, `rollNumber`)")

        db.execSQL(
            """
            CREATE TABLE `mark_edit_requests_new` (
                `requestId` TEXT NOT NULL,
                `sessionId` TEXT NOT NULL,
                `semester` INTEGER NOT NULL,
                `courseCode` TEXT NOT NULL,
                `examType` TEXT NOT NULL,
                `rollNumber` TEXT NOT NULL,
                `currentScore` INTEGER,
                `requestedScore` INTEGER NOT NULL,
                `reason` TEXT,
                `status` TEXT NOT NULL,
                `requestedBy` TEXT NOT NULL,
                `reviewedBy` TEXT,
                `requestedAt` INTEGER NOT NULL,
                `reviewedAt` INTEGER,
                `createdAt` INTEGER NOT NULL DEFAULT 0,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL DEFAULT 0,
                `updatedBy` TEXT,
                `isDeleted` INTEGER NOT NULL DEFAULT 0,
                `deletedAt` INTEGER,
                `deletedBy` TEXT,
                PRIMARY KEY(`requestId`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `mark_edit_requests_new`
            (`requestId`,`sessionId`,`semester`,`courseCode`,`examType`,`rollNumber`,`currentScore`,
             `requestedScore`,`reason`,`status`,`requestedBy`,`reviewedBy`,`requestedAt`,`reviewedAt`,
             `createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`)
            SELECT
             `requestId`,`sessionId`,`semester`,`courseCode`,`examType`,`rollNumber`,`currentScore`,
             `requestedScore`,`reason`,`status`,`requestedBy`,`reviewedBy`,`requestedAt`,`reviewedAt`,
             `createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`
            FROM `mark_edit_requests`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `mark_edit_requests`")
        db.execSQL("ALTER TABLE `mark_edit_requests_new` RENAME TO `mark_edit_requests`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_mark_edit_requests_sessionId_courseCode_examType_status_rollNumber` ON `mark_edit_requests` (`sessionId`, `courseCode`, `examType`, `status`, `rollNumber`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_mark_edit_requests_sessionId_courseCode_examType_status_updatedAt` ON `mark_edit_requests` (`sessionId`, `courseCode`, `examType`, `status`, `updatedAt`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_mark_edit_requests_status_requestedAt` ON `mark_edit_requests` (`status`, `requestedAt`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_mark_edit_requests_status_updatedAt` ON `mark_edit_requests` (`status`, `updatedAt`)")

        db.execSQL(
            """
            CREATE TABLE `session_fees_new` (
                `sessionId` TEXT NOT NULL,
                `cadence` TEXT NOT NULL,
                `academicYear` TEXT,
                `dueDate` TEXT,
                `lateFineNote` TEXT,
                `paymentNote` TEXT,
                `createdAt` INTEGER NOT NULL DEFAULT 0,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL DEFAULT 0,
                `updatedBy` TEXT,
                `isDeleted` INTEGER NOT NULL DEFAULT 0,
                `deletedAt` INTEGER,
                `deletedBy` TEXT,
                PRIMARY KEY(`sessionId`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `session_fees_new`
            (`sessionId`,`cadence`,`academicYear`,`dueDate`,`lateFineNote`,`paymentNote`,
             `createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`)
            SELECT
             `sessionId`,`cadence`,`academicYear`,`dueDate`,`lateFineNote`,`paymentNote`,
             `createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`
            FROM `session_fees`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `session_fees`")
        db.execSQL("ALTER TABLE `session_fees_new` RENAME TO `session_fees`")

        db.execSQL(
            """
            CREATE TABLE `session_fee_heads_new` (
                `id` TEXT NOT NULL,
                `sessionId` TEXT NOT NULL,
                `label` TEXT NOT NULL,
                `amount` REAL NOT NULL,
                `position` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL DEFAULT 0,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL DEFAULT 0,
                `updatedBy` TEXT,
                `isDeleted` INTEGER NOT NULL DEFAULT 0,
                `deletedAt` INTEGER,
                `deletedBy` TEXT,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `session_fee_heads_new`
            (`id`,`sessionId`,`label`,`amount`,`position`,
             `createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`)
            SELECT
             `id`,`sessionId`,`label`,`amount`,`position`,
             `createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`
            FROM `session_fee_heads`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `session_fee_heads`")
        db.execSQL("ALTER TABLE `session_fee_heads_new` RENAME TO `session_fee_heads`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_fee_heads_sessionId_position` ON `session_fee_heads` (`sessionId`, `position`)")

        db.execSQL(
            """
            CREATE TABLE `student_link_requests_new` (
                `requestId` TEXT NOT NULL,
                `requestedByUid` TEXT NOT NULL,
                `sessionIdClaimed` TEXT,
                `rollNumberClaimed` TEXT,
                `nameClaimed` TEXT,
                `cnicClaimed` TEXT,
                `dobClaimed` TEXT,
                `universityRollClaimed` TEXT,
                `registrationNoClaimed` TEXT,
                `message` TEXT,
                `status` TEXT NOT NULL,
                `reviewedBy` TEXT,
                `reviewedAt` INTEGER,
                `rejectionReason` TEXT,
                `attemptCount` INTEGER NOT NULL DEFAULT 0,
                `createdAt` INTEGER NOT NULL DEFAULT 0,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL DEFAULT 0,
                `updatedBy` TEXT,
                `isDeleted` INTEGER NOT NULL DEFAULT 0,
                `deletedAt` INTEGER,
                `deletedBy` TEXT,
                PRIMARY KEY(`requestId`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `student_link_requests_new`
            (`requestId`,`requestedByUid`,`sessionIdClaimed`,`rollNumberClaimed`,`nameClaimed`,
             `cnicClaimed`,`dobClaimed`,`universityRollClaimed`,`registrationNoClaimed`,`message`,
             `status`,`reviewedBy`,`reviewedAt`,`rejectionReason`,`attemptCount`,
             `createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`)
            SELECT
             `requestId`,`requestedByUid`,`sessionIdClaimed`,`rollNumberClaimed`,`nameClaimed`,
             `cnicClaimed`,`dobClaimed`,`universityRollClaimed`,`registrationNoClaimed`,`message`,
             `status`,`reviewedBy`,`reviewedAt`,`rejectionReason`,`attemptCount`,
             `createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`
            FROM `student_link_requests`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `student_link_requests`")
        db.execSQL("ALTER TABLE `student_link_requests_new` RENAME TO `student_link_requests`")

        db.execSQL(
            """
            CREATE TABLE `teachers_new` (
                `teacherId` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `email` TEXT NOT NULL,
                `phone` TEXT,
                `deptId` TEXT,
                `designation` TEXT,
                `qualification` TEXT,
                `specialization` TEXT,
                `officeRoom` TEXT,
                `gender` TEXT,
                `authUid` TEXT,
                `isAdmin` INTEGER NOT NULL DEFAULT 0,
                `isHod` INTEGER NOT NULL DEFAULT 0,
                `photoPath` TEXT,
                `canApproveLinkRequests` INTEGER NOT NULL DEFAULT 0,
                `canEditTimetable` INTEGER NOT NULL DEFAULT 0,
                `canSendNotifications` INTEGER NOT NULL DEFAULT 0,
                `canManageDatesheets` INTEGER NOT NULL DEFAULT 0,
                `status` TEXT NOT NULL,
                `isActive` INTEGER NOT NULL DEFAULT 1,
                `createdAt` INTEGER NOT NULL DEFAULT 0,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL DEFAULT 0,
                `updatedBy` TEXT,
                `isDeleted` INTEGER NOT NULL DEFAULT 0,
                `deletedAt` INTEGER,
                `deletedBy` TEXT,
                PRIMARY KEY(`teacherId`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `teachers_new`
            (`teacherId`,`name`,`email`,`phone`,`deptId`,`designation`,`qualification`,
             `specialization`,`officeRoom`,`gender`,`authUid`,`isAdmin`,`isHod`,`photoPath`,
             `canApproveLinkRequests`,`canEditTimetable`,`canSendNotifications`,`canManageDatesheets`,
             `status`,`isActive`,`createdAt`,`createdBy`,`updatedAt`,`updatedBy`,
             `isDeleted`,`deletedAt`,`deletedBy`)
            SELECT
             `teacherId`,`name`,`email`,`phone`,`deptId`,`designation`,`qualification`,
             `specialization`,`officeRoom`,`gender`,`authUid`,`isAdmin`,`isHod`,`photoPath`,
             `canApproveLinkRequests`,`canEditTimetable`,`canSendNotifications`,`canManageDatesheets`,
             `status`,`isActive`,`createdAt`,`createdBy`,`updatedAt`,`updatedBy`,
             `isDeleted`,`deletedAt`,`deletedBy`
            FROM `teachers`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `teachers`")
        db.execSQL("ALTER TABLE `teachers_new` RENAME TO `teachers`")
    }
}

val MIGRATION_39_40: Migration = object : Migration(39, 40) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `buildings` (
                `buildingId` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `code` TEXT,
                `isActive` INTEGER NOT NULL DEFAULT 1,
                `createdAt` INTEGER NOT NULL DEFAULT 0,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL DEFAULT 0,
                `updatedBy` TEXT,
                `isDeleted` INTEGER NOT NULL DEFAULT 0,
                `deletedAt` INTEGER,
                `deletedBy` TEXT,
                PRIMARY KEY(`buildingId`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `rooms` (
                `roomId` TEXT NOT NULL,
                `buildingId` TEXT NOT NULL,
                `roomNo` TEXT NOT NULL,
                `name` TEXT,
                `capacity` INTEGER,
                `isOffice` INTEGER NOT NULL DEFAULT 0,
                `isActive` INTEGER NOT NULL DEFAULT 1,
                `createdAt` INTEGER NOT NULL DEFAULT 0,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL DEFAULT 0,
                `updatedBy` TEXT,
                `isDeleted` INTEGER NOT NULL DEFAULT 0,
                `deletedAt` INTEGER,
                `deletedBy` TEXT,
                PRIMARY KEY(`roomId`)
            )
            """.trimIndent(),
        )
    }
}

val MIGRATION_40_41: Migration = object : Migration(40, 41) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `app_logs` (
                `logId` TEXT NOT NULL,
                `occurredAtMillis` INTEGER NOT NULL,
                `severity` TEXT NOT NULL,
                `kind` TEXT NOT NULL,
                `tag` TEXT NOT NULL,
                `message` TEXT NOT NULL,
                `stackTrace` TEXT,
                `accountEmail` TEXT,
                `appId` TEXT,
                `appVersion` TEXT,
                `platform` TEXT,
                `deviceInfo` TEXT,
                PRIMARY KEY(`logId`)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_app_logs_occurredAtMillis` ON `app_logs` (`occurredAtMillis`)")
    }
}

val MIGRATION_41_42: Migration = object : Migration(41, 42) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Datesheet rework: scoped to (session, semester), curriculum-linked papers, building/room
        // FKs instead of free text. The local cache rebuilds itself from Supabase on next sync, so
        // recreate rather than ALTER (Room offers no in-place column-drop story on SQLite anyway).
        db.execSQL("DROP TABLE IF EXISTS `datesheets`")
        db.execSQL("DROP TABLE IF EXISTS `datesheet_slots`")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `datesheets` (
                `datesheetId` TEXT NOT NULL,
                `sessionId` TEXT NOT NULL,
                `semester` INTEGER NOT NULL,
                `defaultStartTime` TEXT,
                `defaultEndTime` TEXT,
                `defaultBuildingId` TEXT,
                `published` INTEGER NOT NULL DEFAULT 0,
                `instructions` TEXT,
                `createdAt` INTEGER NOT NULL DEFAULT 0,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL DEFAULT 0,
                `updatedBy` TEXT,
                `isDeleted` INTEGER NOT NULL DEFAULT 0,
                `deletedAt` INTEGER,
                `deletedBy` TEXT,
                PRIMARY KEY(`datesheetId`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `datesheet_slots` (
                `slotId` TEXT NOT NULL,
                `datesheetId` TEXT NOT NULL,
                `courseCode` TEXT NOT NULL,
                `subjectName` TEXT NOT NULL,
                `examDate` TEXT,
                `startTime` TEXT,
                `endTime` TEXT,
                `buildingId` TEXT,
                `building` TEXT,
                `roomId` TEXT,
                `roomNo` TEXT,
                `invigilatorEmail` TEXT,
                `createdAt` INTEGER NOT NULL DEFAULT 0,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL DEFAULT 0,
                `updatedBy` TEXT,
                `isDeleted` INTEGER NOT NULL DEFAULT 0,
                `deletedAt` INTEGER,
                `deletedBy` TEXT,
                PRIMARY KEY(`slotId`)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_datesheet_slots_datesheetId_examDate` ON `datesheet_slots` (`datesheetId`, `examDate`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_datesheet_slots_datesheetId_courseCode` ON `datesheet_slots` (`datesheetId`, `courseCode`)")
    }
}

val MIGRATION_42_43: Migration = object : Migration(42, 43) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Semester terms were previously cached only in an in-memory map (CurriculumRepositoryImpl),
        // which meant the cache was empty on every cold start and never survived a partial sync --
        // silently blocking session promotion since canPromote() couldn't see a term that had
        // already ended. Give it a real local cache like every other synced table.
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `semester_terms` (
                `sessionId` TEXT NOT NULL,
                `semester` INTEGER NOT NULL,
                `startDate` TEXT,
                `endDate` TEXT,
                `createdAt` INTEGER NOT NULL DEFAULT 0,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL DEFAULT 0,
                `updatedBy` TEXT,
                `isDeleted` INTEGER NOT NULL DEFAULT 0,
                `deletedAt` INTEGER,
                `deletedBy` TEXT,
                PRIMARY KEY(`sessionId`, `semester`)
            )
            """.trimIndent(),
        )
    }
}

val MIGRATION_43_44: Migration = object : Migration(43, 44) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Exam paper submissions now bind to a specific published datesheet slot instead of a free
        // (session, course, exam type) choice, and the admin review columns are gone -- admin just
        // downloads papers to print, doesn't grade them. Existing local rows are stale under the old
        // model (no slot to map to), so this recreates the table empty rather than force-fitting old
        // data; the next global sync repopulates it correctly shaped.
        db.execSQL("DROP TABLE IF EXISTS `exam_paper_submissions`")
        db.execSQL(
            """
            CREATE TABLE `exam_paper_submissions` (
                `submissionId` TEXT NOT NULL,
                `datesheetSlotId` TEXT NOT NULL,
                `offeringId` TEXT NOT NULL,
                `semester` INTEGER NOT NULL,
                `subjectId` TEXT NOT NULL,
                `teacherId` TEXT NOT NULL,
                `storagePath` TEXT,
                `fileName` TEXT,
                `fileSizeBytes` INTEGER,
                `uploadedAt` INTEGER NOT NULL,
                `mimeType` TEXT,
                `description` TEXT,
                `createdBy` TEXT,
                `createdAt` INTEGER NOT NULL DEFAULT 0,
                `updatedAt` INTEGER NOT NULL DEFAULT 0,
                `updatedBy` TEXT,
                `isDeleted` INTEGER NOT NULL DEFAULT 0,
                `deletedAt` INTEGER,
                `deletedBy` TEXT,
                PRIMARY KEY(`submissionId`)
            )
            """.trimIndent(),
        )
    }
}

/** Drops teachers.canEditTimetable/canManageDatesheets -- both removed as delegatable teacher
 * permissions (timetable editing and datesheet management are admin-app-only now). */
val MIGRATION_44_45: Migration = object : Migration(44, 45) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE `teachers_new` (
                `teacherId` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `email` TEXT NOT NULL,
                `phone` TEXT,
                `deptId` TEXT,
                `designation` TEXT,
                `qualification` TEXT,
                `specialization` TEXT,
                `officeRoom` TEXT,
                `gender` TEXT,
                `authUid` TEXT,
                `isAdmin` INTEGER NOT NULL DEFAULT 0,
                `isHod` INTEGER NOT NULL DEFAULT 0,
                `photoPath` TEXT,
                `canApproveLinkRequests` INTEGER NOT NULL DEFAULT 0,
                `canSendNotifications` INTEGER NOT NULL DEFAULT 0,
                `status` TEXT NOT NULL,
                `isActive` INTEGER NOT NULL DEFAULT 1,
                `createdAt` INTEGER NOT NULL DEFAULT 0,
                `createdBy` TEXT,
                `updatedAt` INTEGER NOT NULL DEFAULT 0,
                `updatedBy` TEXT,
                `isDeleted` INTEGER NOT NULL DEFAULT 0,
                `deletedAt` INTEGER,
                `deletedBy` TEXT,
                PRIMARY KEY(`teacherId`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `teachers_new`
            (`teacherId`,`name`,`email`,`phone`,`deptId`,`designation`,`qualification`,
             `specialization`,`officeRoom`,`gender`,`authUid`,`isAdmin`,`isHod`,`photoPath`,
             `canApproveLinkRequests`,`canSendNotifications`,
             `status`,`isActive`,`createdAt`,`createdBy`,`updatedAt`,`updatedBy`,
             `isDeleted`,`deletedAt`,`deletedBy`)
            SELECT
             `teacherId`,`name`,`email`,`phone`,`deptId`,`designation`,`qualification`,
             `specialization`,`officeRoom`,`gender`,`authUid`,`isAdmin`,`isHod`,`photoPath`,
             `canApproveLinkRequests`,`canSendNotifications`,
             `status`,`isActive`,`createdAt`,`createdBy`,`updatedAt`,`updatedBy`,
             `isDeleted`,`deletedAt`,`deletedBy`
            FROM `teachers`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `teachers`")
        db.execSQL("ALTER TABLE `teachers_new` RENAME TO `teachers`")
    }
}

/**
 * Session & shift consolidation: one academic session per intake now serves Morning, Evening or both.
 * Sessions carry shiftMode instead of shift and are keyed "{deptId}_{startYear}"; students, periods, fee
 * structures and datesheets gained a shift; events/notifications gained optional shift targeting; the
 * insights overview is per session and shift.
 *
 * Every session id changed format, so the cached session-scoped data can't be carried over: the changed
 * tables are recreated empty, the other session-scoped caches are emptied, and every sync checkpoint is
 * reset so the next sync repopulates everything from the server.
 */
val MIGRATION_45_46: Migration = object : Migration(45, 46) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS `academic_sessions`")
        db.execSQL("CREATE TABLE IF NOT EXISTS `academic_sessions` (`sessionId` TEXT NOT NULL, `deptId` TEXT NOT NULL, `startYear` INTEGER NOT NULL, `endYear` INTEGER NOT NULL, `shiftMode` TEXT NOT NULL, `currentSemester` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, `programName` TEXT, `inchargeEmail` TEXT, `maxStudents` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `createdBy` TEXT, `updatedAt` INTEGER NOT NULL, `updatedBy` TEXT, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `deletedBy` TEXT, PRIMARY KEY(`sessionId`))")
        db.execSQL("DROP TABLE IF EXISTS `session_students`")
        db.execSQL("CREATE TABLE IF NOT EXISTS `session_students` (`id` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `deptId` TEXT NOT NULL, `rollNumber` TEXT NOT NULL, `name` TEXT NOT NULL, `shift` TEXT NOT NULL, `linkedEmail` TEXT, `gpa` REAL, `cgpa` REAL, `profileJson` TEXT, `createdAt` INTEGER NOT NULL, `createdBy` TEXT, `updatedAt` INTEGER NOT NULL, `updatedBy` TEXT, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `deletedBy` TEXT, PRIMARY KEY(`id`))")
        db.execSQL("DROP TABLE IF EXISTS `session_periods`")
        db.execSQL("CREATE TABLE IF NOT EXISTS `session_periods` (`id` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `shift` TEXT NOT NULL, `deptId` TEXT NOT NULL, `day` TEXT NOT NULL, `startTime` TEXT, `endTime` TEXT, `courseCode` TEXT, `subjectName` TEXT, `teacherId` TEXT, `teacherName` TEXT, `periodType` TEXT NOT NULL, `creditHours` INTEGER, `roomNo` TEXT, `building` TEXT, `notes` TEXT, `effectiveFrom` TEXT, `effectiveTo` TEXT, `createdAt` INTEGER NOT NULL, `createdBy` TEXT, `updatedAt` INTEGER NOT NULL, `updatedBy` TEXT, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `deletedBy` TEXT, PRIMARY KEY(`id`))")
        db.execSQL("DROP TABLE IF EXISTS `session_fees`")
        db.execSQL("CREATE TABLE IF NOT EXISTS `session_fees` (`sessionId` TEXT NOT NULL, `shift` TEXT NOT NULL, `cadence` TEXT NOT NULL, `academicYear` TEXT, `dueDate` TEXT, `lateFineNote` TEXT, `paymentNote` TEXT, `createdAt` INTEGER NOT NULL, `createdBy` TEXT, `updatedAt` INTEGER NOT NULL, `updatedBy` TEXT, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `deletedBy` TEXT, PRIMARY KEY(`sessionId`, `shift`))")
        db.execSQL("DROP TABLE IF EXISTS `session_fee_heads`")
        db.execSQL("CREATE TABLE IF NOT EXISTS `session_fee_heads` (`id` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `shift` TEXT NOT NULL, `label` TEXT NOT NULL, `amount` REAL NOT NULL, `position` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `createdBy` TEXT, `updatedAt` INTEGER NOT NULL, `updatedBy` TEXT, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `deletedBy` TEXT, PRIMARY KEY(`id`))")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_fee_heads_sessionId_shift_position` ON `session_fee_heads` (`sessionId`, `shift`, `position`)")
        db.execSQL("DROP TABLE IF EXISTS `datesheets`")
        db.execSQL("CREATE TABLE IF NOT EXISTS `datesheets` (`datesheetId` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `shift` TEXT NOT NULL, `semester` INTEGER NOT NULL, `defaultStartTime` TEXT, `defaultEndTime` TEXT, `defaultBuildingId` TEXT, `published` INTEGER NOT NULL, `instructions` TEXT, `createdAt` INTEGER NOT NULL, `createdBy` TEXT, `updatedAt` INTEGER NOT NULL, `updatedBy` TEXT, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `deletedBy` TEXT, PRIMARY KEY(`datesheetId`))")
        db.execSQL("DROP TABLE IF EXISTS `calendar_events`")
        db.execSQL("CREATE TABLE IF NOT EXISTS `calendar_events` (`eventId` TEXT NOT NULL, `title` TEXT NOT NULL, `eventType` TEXT NOT NULL, `startDate` TEXT NOT NULL, `endDate` TEXT, `startTime` TEXT, `endTime` TEXT, `description` TEXT, `venue` TEXT, `audience` TEXT NOT NULL, `deptId` TEXT, `sessionId` TEXT, `shift` TEXT, `createdAt` INTEGER NOT NULL, `createdBy` TEXT, `updatedAt` INTEGER NOT NULL, `updatedBy` TEXT, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `deletedBy` TEXT, PRIMARY KEY(`eventId`))")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_calendar_events_startDate` ON `calendar_events` (`startDate`)")
        db.execSQL("DROP TABLE IF EXISTS `notifications`")
        db.execSQL("CREATE TABLE IF NOT EXISTS `notifications` (`notificationId` TEXT NOT NULL, `title` TEXT NOT NULL, `body` TEXT, `targetRole` TEXT NOT NULL, `targetOfferingId` TEXT, `createdByUid` TEXT, `priority` TEXT NOT NULL, `targetDeptId` TEXT, `targetShift` TEXT, `attachmentPath` TEXT, `expiresAt` INTEGER, `createdAt` INTEGER NOT NULL, `createdBy` TEXT, `updatedAt` INTEGER NOT NULL, `updatedBy` TEXT, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `deletedBy` TEXT, PRIMARY KEY(`notificationId`))")
        db.execSQL("DROP TABLE IF EXISTS `insight_session_overviews`")
        db.execSQL("CREATE TABLE IF NOT EXISTS `insight_session_overviews` (`sessionId` TEXT NOT NULL, `deptId` TEXT NOT NULL, `shift` TEXT NOT NULL, `currentSemester` INTEGER NOT NULL, `students` INTEGER NOT NULL, `avgCgpa` REAL, `avgAttendance` REAL, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`sessionId`, `shift`))")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_insight_session_overviews_deptId_sessionId` ON `insight_session_overviews` (`deptId`, `sessionId`)")
        db.execSQL("DELETE FROM `semester_subjects`")
        db.execSQL("DELETE FROM `semester_terms`")
        db.execSQL("DELETE FROM `session_attendance_rows`")
        db.execSQL("DELETE FROM `session_attendance_tally`")
        db.execSQL("DELETE FROM `session_marks`")
        db.execSQL("DELETE FROM `student_semester_gpa`")
        db.execSQL("DELETE FROM `fines`")
        db.execSQL("DELETE FROM `datesheet_slots`")
        db.execSQL("DELETE FROM `mark_edit_requests`")
        db.execSQL("DELETE FROM `exam_paper_submissions`")
        db.execSQL("DELETE FROM `student_link_requests`")
        db.execSQL("DELETE FROM `insight_at_risk_students`")
        db.execSQL("DELETE FROM `insight_exam_stats`")
        db.execSQL("DELETE FROM `table_sync_state`")
        db.execSQL("DELETE FROM `sync_state`")
    }
}

/**
 * 46 -> 47: insights at-risk and exam-stat caches carry the shift, so filters and teacher insights can narrow to
 * one shift. Both tables are caches rebuilt by the next insights sync, so they are recreated empty.
 */
val MIGRATION_46_47: Migration = object : Migration(46, 47) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS `insight_at_risk_students`")
        db.execSQL("CREATE TABLE IF NOT EXISTS `insight_at_risk_students` (`id` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `rollNumber` TEXT NOT NULL, `name` TEXT NOT NULL, `cgpa` REAL, `attendance` REAL, `cachedAt` INTEGER NOT NULL, `shift` TEXT NOT NULL, PRIMARY KEY(`id`))")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_insight_at_risk_students_sessionId_rollNumber` ON `insight_at_risk_students` (`sessionId`, `rollNumber`)")
        db.execSQL("DROP TABLE IF EXISTS `insight_exam_stats`")
        db.execSQL("CREATE TABLE IF NOT EXISTS `insight_exam_stats` (`id` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `semester` INTEGER NOT NULL, `courseCode` TEXT NOT NULL, `examType` TEXT NOT NULL, `entered` INTEGER NOT NULL, `avgScore` REAL, `minScore` INTEGER, `maxScore` INTEGER, `stddev` REAL, `outOf` INTEGER NOT NULL, `passRate` REAL, `cachedAt` INTEGER NOT NULL, `shift` TEXT NOT NULL, PRIMARY KEY(`id`))")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_insight_exam_stats_sessionId_semester_courseCode_examType` ON `insight_exam_stats` (`sessionId`, `semester`, `courseCode`, `examType`)")
    }
}

/**
 * 47 -> 48: academic_sessions gains programType (BS / MA_REPLACEMENT) so a department can run a
 * 2-year MA-Replacement intake alongside its 4-year BS intake. Existing rows backfill to 'BS'.
 */
val MIGRATION_47_48: Migration = object : Migration(47, 48) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `academic_sessions` ADD COLUMN `programType` TEXT NOT NULL DEFAULT 'BS'")
    }
}

/**
 * 48 -> 49: session_periods gains remotePeriodId (the true timetable_periods row a local row maps to) and
 * linkedSessionIds (denormalized merge membership), supporting merged-lecture "shadow" rows so a session
 * merely linked to another session's lecture can still see it on its own grid.
 */
val MIGRATION_48_49: Migration = object : Migration(48, 49) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `session_periods` ADD COLUMN `remotePeriodId` TEXT NOT NULL DEFAULT ''")
        db.execSQL("UPDATE `session_periods` SET `remotePeriodId` = `id` WHERE `remotePeriodId` = ''")
        db.execSQL("ALTER TABLE `session_periods` ADD COLUMN `linkedSessionIds` TEXT NOT NULL DEFAULT ''")
    }
}

/**
 * 49 -> 50: session_subjects gains courseType (denormalized from the server's new subject_pool, since a
 * course's own definition is now shared college-wide instead of copied per session+semester -- see
 * CourseCategory), and a new subject_pool table caches that pool locally for an "add from pool" picker.
 */
val MIGRATION_49_50: Migration = object : Migration(49, 50) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `semester_subjects` ADD COLUMN `courseType` TEXT NOT NULL DEFAULT 'MAJOR'")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `subject_pool` (" +
                "`courseCode` TEXT NOT NULL, `name` TEXT NOT NULL, `creditHours` INTEGER NOT NULL, " +
                "`subjectType` TEXT NOT NULL, `courseType` TEXT NOT NULL DEFAULT 'MAJOR', `outline` TEXT, " +
                "`createdAt` INTEGER NOT NULL, `createdBy` TEXT, `updatedAt` INTEGER NOT NULL, `updatedBy` TEXT, " +
                "`isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `deletedBy` TEXT, " +
                "PRIMARY KEY(`courseCode`))",
        )
    }
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
    MIGRATION_30_31,
    MIGRATION_31_32,
    MIGRATION_32_33,
    MIGRATION_33_34,
    MIGRATION_34_35,
    MIGRATION_35_36,
    MIGRATION_36_37,
    MIGRATION_37_38,
    MIGRATION_38_39,
    MIGRATION_39_40,
    MIGRATION_40_41,
    MIGRATION_41_42,
    MIGRATION_42_43,
    MIGRATION_43_44,
    MIGRATION_44_45,
    MIGRATION_45_46,
    MIGRATION_46_47,
    MIGRATION_47_48,
    MIGRATION_48_49,
    MIGRATION_49_50,
)
