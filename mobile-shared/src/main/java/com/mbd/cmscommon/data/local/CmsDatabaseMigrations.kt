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
)
