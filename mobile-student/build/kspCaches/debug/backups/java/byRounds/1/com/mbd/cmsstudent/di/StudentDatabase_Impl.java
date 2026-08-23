package com.mbd.cmsstudent.di;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.mbd.cmscommon.data.local.dao.AcademicSessionDao;
import com.mbd.cmscommon.data.local.dao.AcademicSessionDao_Impl;
import com.mbd.cmscommon.data.local.dao.AdministratorAccountDao;
import com.mbd.cmscommon.data.local.dao.AdministratorAccountDao_Impl;
import com.mbd.cmscommon.data.local.dao.CalendarEventDao;
import com.mbd.cmscommon.data.local.dao.CalendarEventDao_Impl;
import com.mbd.cmscommon.data.local.dao.DatesheetDao;
import com.mbd.cmscommon.data.local.dao.DatesheetDao_Impl;
import com.mbd.cmscommon.data.local.dao.DepartmentDao;
import com.mbd.cmscommon.data.local.dao.DepartmentDao_Impl;
import com.mbd.cmscommon.data.local.dao.DocumentDao;
import com.mbd.cmscommon.data.local.dao.DocumentDao_Impl;
import com.mbd.cmscommon.data.local.dao.ExamPaperSubmissionDao;
import com.mbd.cmscommon.data.local.dao.ExamPaperSubmissionDao_Impl;
import com.mbd.cmscommon.data.local.dao.FineDao;
import com.mbd.cmscommon.data.local.dao.FineDao_Impl;
import com.mbd.cmscommon.data.local.dao.InsightsDao;
import com.mbd.cmscommon.data.local.dao.InsightsDao_Impl;
import com.mbd.cmscommon.data.local.dao.MarkEditRequestDao;
import com.mbd.cmscommon.data.local.dao.MarkEditRequestDao_Impl;
import com.mbd.cmscommon.data.local.dao.NotificationDao;
import com.mbd.cmscommon.data.local.dao.NotificationDao_Impl;
import com.mbd.cmscommon.data.local.dao.SemesterSubjectDao;
import com.mbd.cmscommon.data.local.dao.SemesterSubjectDao_Impl;
import com.mbd.cmscommon.data.local.dao.SessionAttendanceDao;
import com.mbd.cmscommon.data.local.dao.SessionAttendanceDao_Impl;
import com.mbd.cmscommon.data.local.dao.SessionFeeDao;
import com.mbd.cmscommon.data.local.dao.SessionFeeDao_Impl;
import com.mbd.cmscommon.data.local.dao.SessionMarkDao;
import com.mbd.cmscommon.data.local.dao.SessionMarkDao_Impl;
import com.mbd.cmscommon.data.local.dao.SessionPeriodDao;
import com.mbd.cmscommon.data.local.dao.SessionPeriodDao_Impl;
import com.mbd.cmscommon.data.local.dao.SessionStudentDao;
import com.mbd.cmscommon.data.local.dao.SessionStudentDao_Impl;
import com.mbd.cmscommon.data.local.dao.StudentLinkRequestDao;
import com.mbd.cmscommon.data.local.dao.StudentLinkRequestDao_Impl;
import com.mbd.cmscommon.data.local.dao.StudentSemesterGpaDao;
import com.mbd.cmscommon.data.local.dao.StudentSemesterGpaDao_Impl;
import com.mbd.cmscommon.data.local.dao.SyncStateDao;
import com.mbd.cmscommon.data.local.dao.SyncStateDao_Impl;
import com.mbd.cmscommon.data.local.dao.TableSyncStateDao;
import com.mbd.cmscommon.data.local.dao.TableSyncStateDao_Impl;
import com.mbd.cmscommon.data.local.dao.TeacherDao;
import com.mbd.cmscommon.data.local.dao.TeacherDao_Impl;
import com.mbd.cmscommon.data.local.dao.UserDao;
import com.mbd.cmscommon.data.local.dao.UserDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class StudentDatabase_Impl extends StudentDatabase {
  private volatile DepartmentDao _departmentDao;

  private volatile AdministratorAccountDao _administratorAccountDao;

  private volatile TeacherDao _teacherDao;

  private volatile UserDao _userDao;

  private volatile StudentLinkRequestDao _studentLinkRequestDao;

  private volatile DocumentDao _documentDao;

  private volatile ExamPaperSubmissionDao _examPaperSubmissionDao;

  private volatile NotificationDao _notificationDao;

  private volatile FineDao _fineDao;

  private volatile CalendarEventDao _calendarEventDao;

  private volatile MarkEditRequestDao _markEditRequestDao;

  private volatile InsightsDao _insightsDao;

  private volatile DatesheetDao _datesheetDao;

  private volatile SyncStateDao _syncStateDao;

  private volatile TableSyncStateDao _tableSyncStateDao;

  private volatile AcademicSessionDao _academicSessionDao;

  private volatile SemesterSubjectDao _semesterSubjectDao;

  private volatile SessionStudentDao _sessionStudentDao;

  private volatile SessionPeriodDao _sessionPeriodDao;

  private volatile SessionAttendanceDao _sessionAttendanceDao;

  private volatile SessionMarkDao _sessionMarkDao;

  private volatile StudentSemesterGpaDao _studentSemesterGpaDao;

  private volatile SessionFeeDao _sessionFeeDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(30) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `departments` (`deptId` TEXT NOT NULL, `entityId` INTEGER NOT NULL, `name` TEXT NOT NULL, `code` TEXT NOT NULL, `hodEmail` TEXT, `description` TEXT, `isActive` INTEGER NOT NULL, `archivedAt` INTEGER, `createdAt` INTEGER NOT NULL, `createdBy` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL, `updatedBy` TEXT NOT NULL, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `deletedBy` TEXT, PRIMARY KEY(`deptId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `teachers` (`teacherId` TEXT NOT NULL, `entityId` INTEGER NOT NULL, `name` TEXT NOT NULL, `email` TEXT NOT NULL, `phone` TEXT, `deptId` TEXT, `designation` TEXT, `qualification` TEXT, `specialization` TEXT, `officeRoom` TEXT, `gender` TEXT, `canApproveLinkRequests` INTEGER NOT NULL, `canEditTimetable` INTEGER NOT NULL, `canSendNotifications` INTEGER NOT NULL, `canManageDatesheets` INTEGER NOT NULL, `status` TEXT NOT NULL, `isActive` INTEGER NOT NULL, `archivedAt` INTEGER, `createdAt` INTEGER NOT NULL, `createdBy` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL, `updatedBy` TEXT NOT NULL, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `deletedBy` TEXT, PRIMARY KEY(`teacherId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `administrator_accounts` (`id` TEXT NOT NULL, `email` TEXT NOT NULL, `status` TEXT NOT NULL, `lastLoginAt` INTEGER, `entityId` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `createdBy` TEXT, `updatedAt` INTEGER NOT NULL, `updatedBy` TEXT, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `deletedBy` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_administrator_accounts_email` ON `administrator_accounts` (`email`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_administrator_accounts_updatedAt_entityId` ON `administrator_accounts` (`updatedAt`, `entityId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `users` (`uid` TEXT NOT NULL, `role` TEXT NOT NULL, `teacherId` TEXT, `linkedStudentId` TEXT, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`uid`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `student_link_requests` (`requestId` TEXT NOT NULL, `requestedByUid` TEXT NOT NULL, `sessionIdClaimed` TEXT, `rollNumberClaimed` TEXT NOT NULL, `nameClaimed` TEXT, `cnicClaimed` TEXT, `dobClaimed` TEXT, `universityRollClaimed` TEXT, `registrationNoClaimed` TEXT, `message` TEXT, `status` TEXT NOT NULL, `reviewedBy` TEXT, `reviewedAt` INTEGER, `rejectionReason` TEXT, `attemptCount` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `entityId` INTEGER NOT NULL, `createdBy` TEXT, `updatedAt` INTEGER NOT NULL, `updatedBy` TEXT, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `deletedBy` TEXT, PRIMARY KEY(`requestId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `documents` (`documentId` TEXT NOT NULL, `kind` TEXT NOT NULL, `title` TEXT NOT NULL, `storagePath` TEXT, `body` TEXT, `deptId` TEXT, `audience` TEXT NOT NULL, `tagsJson` TEXT NOT NULL, `published` INTEGER NOT NULL, `publishedBy` TEXT, `entityId` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `createdBy` TEXT, `updatedAt` INTEGER NOT NULL, `updatedBy` TEXT, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `deletedBy` TEXT, PRIMARY KEY(`documentId`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_documents_createdAt` ON `documents` (`createdAt`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_documents_updatedAt_entityId` ON `documents` (`updatedAt`, `entityId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `exam_paper_submissions` (`submissionId` TEXT NOT NULL, `offeringId` TEXT NOT NULL, `subjectId` TEXT NOT NULL, `examType` TEXT NOT NULL, `teacherId` TEXT NOT NULL, `storagePath` TEXT NOT NULL, `fileName` TEXT NOT NULL, `uploadedAt` INTEGER NOT NULL, `createdBy` TEXT NOT NULL, `entityId` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `updatedBy` TEXT, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `deletedBy` TEXT, PRIMARY KEY(`submissionId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `notifications` (`notificationId` TEXT NOT NULL, `title` TEXT NOT NULL, `body` TEXT NOT NULL, `targetRole` TEXT, `targetOfferingId` TEXT, `createdByUid` TEXT NOT NULL, `priority` TEXT NOT NULL, `targetDeptId` TEXT, `attachmentPath` TEXT, `expiresAt` INTEGER, `createdAt` INTEGER NOT NULL, `entityId` INTEGER NOT NULL, `createdBy` TEXT, `updatedAt` INTEGER NOT NULL, `updatedBy` TEXT, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `deletedBy` TEXT, PRIMARY KEY(`notificationId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `sync_state` (`collectionName` TEXT NOT NULL, `lastSyncedAt` INTEGER NOT NULL, PRIMARY KEY(`collectionName`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `table_sync_state` (`owner_key` TEXT NOT NULL, `table_name` TEXT NOT NULL, `scope_key` TEXT NOT NULL, `last_updated_at` TEXT NOT NULL, `last_successful_sync_at` TEXT NOT NULL, `created_at` INTEGER NOT NULL, `created_by` TEXT, `updated_at` INTEGER NOT NULL, `updated_by` TEXT, PRIMARY KEY(`owner_key`, `table_name`, `scope_key`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `academic_sessions` (`sessionId` TEXT NOT NULL, `deptId` TEXT NOT NULL, `startYear` INTEGER NOT NULL, `endYear` INTEGER NOT NULL, `shift` TEXT NOT NULL, `currentSemester` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, `programName` TEXT, `inchargeEmail` TEXT, `maxStudents` INTEGER NOT NULL, `archivedAt` INTEGER, `entityId` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `createdBy` TEXT, `updatedAt` INTEGER NOT NULL, `updatedBy` TEXT, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `deletedBy` TEXT, PRIMARY KEY(`sessionId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `semester_subjects` (`id` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `semester` INTEGER NOT NULL, `courseCode` TEXT NOT NULL, `name` TEXT NOT NULL, `creditHours` INTEGER NOT NULL, `subjectType` TEXT NOT NULL, `isElective` INTEGER NOT NULL, `outline` TEXT, `entityId` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `createdBy` TEXT, `updatedAt` INTEGER NOT NULL, `updatedBy` TEXT, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `deletedBy` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `session_students` (`id` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `deptId` TEXT NOT NULL, `rollNumber` TEXT NOT NULL, `name` TEXT NOT NULL, `linkedEmail` TEXT NOT NULL, `gpa` REAL, `cgpa` REAL, `entityId` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `createdBy` TEXT, `updatedAt` INTEGER NOT NULL, `updatedBy` TEXT, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `deletedBy` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `session_periods` (`id` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `deptId` TEXT NOT NULL, `day` TEXT NOT NULL, `startTime` TEXT NOT NULL, `endTime` TEXT NOT NULL, `courseCode` TEXT NOT NULL, `subjectName` TEXT NOT NULL, `teacherId` TEXT NOT NULL, `teacherName` TEXT NOT NULL, `periodType` TEXT NOT NULL, `creditHours` INTEGER, `roomNo` TEXT, `building` TEXT, `notes` TEXT, `effectiveFrom` TEXT, `effectiveTo` TEXT, `entityId` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `createdBy` TEXT, `updatedAt` INTEGER NOT NULL, `updatedBy` TEXT, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `deletedBy` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `session_attendance_tallies` (`id` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `courseCode` TEXT NOT NULL, `rollNumber` TEXT NOT NULL, `present` INTEGER NOT NULL, `absent` INTEGER NOT NULL, `leave` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `session_attendance_rows` (`id` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `semester` INTEGER NOT NULL, `courseCode` TEXT NOT NULL, `date` TEXT NOT NULL, `rollNumber` TEXT NOT NULL, `status` TEXT NOT NULL, `teacherEmail` TEXT NOT NULL, `isLate` INTEGER NOT NULL, `remark` TEXT, `lectureTopic` TEXT, `recordedAt` INTEGER NOT NULL, `entityId` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `createdBy` TEXT, `updatedAt` INTEGER NOT NULL, `updatedBy` TEXT, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `deletedBy` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_attendance_rows_sessionId_courseCode_date_rollNumber` ON `session_attendance_rows` (`sessionId`, `courseCode`, `date`, `rollNumber`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_attendance_rows_sessionId_courseCode_updatedAt_entityId` ON `session_attendance_rows` (`sessionId`, `courseCode`, `updatedAt`, `entityId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_attendance_rows_sessionId_updatedAt_entityId` ON `session_attendance_rows` (`sessionId`, `updatedAt`, `entityId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_attendance_rows_sessionId_semester` ON `session_attendance_rows` (`sessionId`, `semester`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `session_marks` (`id` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `courseCode` TEXT NOT NULL, `examType` TEXT NOT NULL, `rollNumber` TEXT NOT NULL, `score` INTEGER NOT NULL, `maxMarks` INTEGER NOT NULL, `wasAbsent` INTEGER NOT NULL, `remarks` TEXT, `entityId` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `createdBy` TEXT, `updatedAt` INTEGER NOT NULL, `updatedBy` TEXT, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `deletedBy` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `student_semester_gpa` (`id` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `rollNumber` TEXT NOT NULL, `semester` INTEGER NOT NULL, `gpa` REAL NOT NULL, `cgpa` REAL NOT NULL, `termLabel` TEXT, `resultStatus` TEXT NOT NULL, `classPosition` INTEGER, `remarks` TEXT, `supplyCoursesJson` TEXT NOT NULL, `entityId` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `createdBy` TEXT, `updatedAt` INTEGER NOT NULL, `updatedBy` TEXT, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `deletedBy` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_student_semester_gpa_sessionId_rollNumber_semester` ON `student_semester_gpa` (`sessionId`, `rollNumber`, `semester`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_student_semester_gpa_sessionId_semester_rollNumber` ON `student_semester_gpa` (`sessionId`, `semester`, `rollNumber`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_student_semester_gpa_sessionId_rollNumber_updatedAt_entityId` ON `student_semester_gpa` (`sessionId`, `rollNumber`, `updatedAt`, `entityId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_student_semester_gpa_sessionId_semester_updatedAt_entityId` ON `student_semester_gpa` (`sessionId`, `semester`, `updatedAt`, `entityId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `session_fees` (`sessionId` TEXT NOT NULL, `cadence` TEXT NOT NULL, `academicYear` TEXT, `dueDate` TEXT, `lateFineNote` TEXT, `paymentNote` TEXT, `entityId` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `createdBy` TEXT, `updatedAt` INTEGER NOT NULL, `updatedBy` TEXT, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `deletedBy` TEXT, PRIMARY KEY(`sessionId`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_fees_updatedAt_entityId` ON `session_fees` (`updatedAt`, `entityId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `session_fee_heads` (`id` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `label` TEXT NOT NULL, `amount` REAL NOT NULL, `position` INTEGER NOT NULL, `entityId` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `createdBy` TEXT, `updatedAt` INTEGER NOT NULL, `updatedBy` TEXT, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `deletedBy` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_fee_heads_sessionId` ON `session_fee_heads` (`sessionId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_fee_heads_updatedAt_entityId` ON `session_fee_heads` (`updatedAt`, `entityId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `fines` (`fineId` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `rollNumber` TEXT NOT NULL, `category` TEXT NOT NULL, `amount` REAL NOT NULL, `reason` TEXT NOT NULL, `issuedBy` TEXT, `issuedAt` INTEGER, `entityId` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `createdBy` TEXT, `updatedAt` INTEGER NOT NULL, `updatedBy` TEXT, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `deletedBy` TEXT, PRIMARY KEY(`fineId`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_fines_sessionId_rollNumber` ON `fines` (`sessionId`, `rollNumber`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_fines_updatedAt_entityId` ON `fines` (`updatedAt`, `entityId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `calendar_events` (`eventId` TEXT NOT NULL, `title` TEXT NOT NULL, `eventType` TEXT NOT NULL, `startDate` TEXT NOT NULL, `endDate` TEXT, `startTime` TEXT, `endTime` TEXT, `description` TEXT, `venue` TEXT, `audience` TEXT NOT NULL, `deptId` TEXT, `sessionId` TEXT, `entityId` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `createdBy` TEXT, `updatedAt` INTEGER NOT NULL, `updatedBy` TEXT, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `deletedBy` TEXT, PRIMARY KEY(`eventId`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_calendar_events_startDate` ON `calendar_events` (`startDate`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_calendar_events_updatedAt_entityId` ON `calendar_events` (`updatedAt`, `entityId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `mark_edit_requests` (`requestId` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `semester` INTEGER NOT NULL, `courseCode` TEXT NOT NULL, `examType` TEXT NOT NULL, `rollNumber` TEXT NOT NULL, `currentScore` INTEGER, `requestedScore` INTEGER NOT NULL, `reason` TEXT, `status` TEXT NOT NULL, `requestedBy` TEXT NOT NULL, `reviewedBy` TEXT, `requestedAt` INTEGER NOT NULL, `reviewedAt` INTEGER, `entityId` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `createdBy` TEXT, `updatedAt` INTEGER NOT NULL, `updatedBy` TEXT, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `deletedBy` TEXT, PRIMARY KEY(`requestId`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_mark_edit_requests_sessionId_courseCode_examType_status_rollNumber` ON `mark_edit_requests` (`sessionId`, `courseCode`, `examType`, `status`, `rollNumber`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_mark_edit_requests_sessionId_courseCode_examType_status_updatedAt_entityId` ON `mark_edit_requests` (`sessionId`, `courseCode`, `examType`, `status`, `updatedAt`, `entityId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_mark_edit_requests_status_requestedAt` ON `mark_edit_requests` (`status`, `requestedAt`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_mark_edit_requests_status_updatedAt_entityId` ON `mark_edit_requests` (`status`, `updatedAt`, `entityId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `insight_session_overviews` (`sessionId` TEXT NOT NULL, `deptId` TEXT NOT NULL, `shift` TEXT NOT NULL, `currentSemester` INTEGER NOT NULL, `students` INTEGER NOT NULL, `avgCgpa` REAL, `avgAttendance` REAL, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`sessionId`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_insight_session_overviews_deptId_sessionId` ON `insight_session_overviews` (`deptId`, `sessionId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `insight_at_risk_students` (`id` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `rollNumber` TEXT NOT NULL, `name` TEXT NOT NULL, `cgpa` REAL, `attendance` REAL, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_insight_at_risk_students_sessionId_rollNumber` ON `insight_at_risk_students` (`sessionId`, `rollNumber`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `insight_exam_stats` (`id` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `semester` INTEGER NOT NULL, `courseCode` TEXT NOT NULL, `examType` TEXT NOT NULL, `entered` INTEGER NOT NULL, `avgScore` REAL, `minScore` INTEGER, `maxScore` INTEGER, `stddev` REAL, `outOf` INTEGER NOT NULL, `passRate` REAL, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_insight_exam_stats_sessionId_semester_courseCode_examType` ON `insight_exam_stats` (`sessionId`, `semester`, `courseCode`, `examType`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `datesheets` (`datesheetId` TEXT NOT NULL, `title` TEXT NOT NULL, `examType` TEXT, `sessionId` TEXT, `published` INTEGER NOT NULL, `instructions` TEXT, `entityId` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `createdBy` TEXT, `updatedAt` INTEGER NOT NULL, `updatedBy` TEXT, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `deletedBy` TEXT, PRIMARY KEY(`datesheetId`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_datesheets_sessionId` ON `datesheets` (`sessionId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_datesheets_updatedAt_entityId` ON `datesheets` (`updatedAt`, `entityId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `datesheet_slots` (`slotId` TEXT NOT NULL, `datesheetId` TEXT NOT NULL, `examDate` TEXT NOT NULL, `startTime` TEXT, `endTime` TEXT, `durationMinutes` INTEGER, `courseCode` TEXT, `subjectName` TEXT, `roomNo` TEXT, `building` TEXT, `invigilatorEmail` TEXT, `entityId` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `createdBy` TEXT, `updatedAt` INTEGER NOT NULL, `updatedBy` TEXT, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `deletedBy` TEXT, PRIMARY KEY(`slotId`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_datesheet_slots_datesheetId` ON `datesheet_slots` (`datesheetId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_datesheet_slots_updatedAt_entityId` ON `datesheet_slots` (`updatedAt`, `entityId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'd89e963a609905ae7d1e1de3ee1e54b2')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `departments`");
        db.execSQL("DROP TABLE IF EXISTS `teachers`");
        db.execSQL("DROP TABLE IF EXISTS `administrator_accounts`");
        db.execSQL("DROP TABLE IF EXISTS `users`");
        db.execSQL("DROP TABLE IF EXISTS `student_link_requests`");
        db.execSQL("DROP TABLE IF EXISTS `documents`");
        db.execSQL("DROP TABLE IF EXISTS `exam_paper_submissions`");
        db.execSQL("DROP TABLE IF EXISTS `notifications`");
        db.execSQL("DROP TABLE IF EXISTS `sync_state`");
        db.execSQL("DROP TABLE IF EXISTS `table_sync_state`");
        db.execSQL("DROP TABLE IF EXISTS `academic_sessions`");
        db.execSQL("DROP TABLE IF EXISTS `semester_subjects`");
        db.execSQL("DROP TABLE IF EXISTS `session_students`");
        db.execSQL("DROP TABLE IF EXISTS `session_periods`");
        db.execSQL("DROP TABLE IF EXISTS `session_attendance_tallies`");
        db.execSQL("DROP TABLE IF EXISTS `session_attendance_rows`");
        db.execSQL("DROP TABLE IF EXISTS `session_marks`");
        db.execSQL("DROP TABLE IF EXISTS `student_semester_gpa`");
        db.execSQL("DROP TABLE IF EXISTS `session_fees`");
        db.execSQL("DROP TABLE IF EXISTS `session_fee_heads`");
        db.execSQL("DROP TABLE IF EXISTS `fines`");
        db.execSQL("DROP TABLE IF EXISTS `calendar_events`");
        db.execSQL("DROP TABLE IF EXISTS `mark_edit_requests`");
        db.execSQL("DROP TABLE IF EXISTS `insight_session_overviews`");
        db.execSQL("DROP TABLE IF EXISTS `insight_at_risk_students`");
        db.execSQL("DROP TABLE IF EXISTS `insight_exam_stats`");
        db.execSQL("DROP TABLE IF EXISTS `datesheets`");
        db.execSQL("DROP TABLE IF EXISTS `datesheet_slots`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsDepartments = new HashMap<String, TableInfo.Column>(15);
        _columnsDepartments.put("deptId", new TableInfo.Column("deptId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDepartments.put("entityId", new TableInfo.Column("entityId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDepartments.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDepartments.put("code", new TableInfo.Column("code", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDepartments.put("hodEmail", new TableInfo.Column("hodEmail", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDepartments.put("description", new TableInfo.Column("description", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDepartments.put("isActive", new TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDepartments.put("archivedAt", new TableInfo.Column("archivedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDepartments.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDepartments.put("createdBy", new TableInfo.Column("createdBy", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDepartments.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDepartments.put("updatedBy", new TableInfo.Column("updatedBy", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDepartments.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDepartments.put("deletedAt", new TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDepartments.put("deletedBy", new TableInfo.Column("deletedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDepartments = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDepartments = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoDepartments = new TableInfo("departments", _columnsDepartments, _foreignKeysDepartments, _indicesDepartments);
        final TableInfo _existingDepartments = TableInfo.read(db, "departments");
        if (!_infoDepartments.equals(_existingDepartments)) {
          return new RoomOpenHelper.ValidationResult(false, "departments(com.mbd.cmscommon.data.local.entity.DepartmentEntity).\n"
                  + " Expected:\n" + _infoDepartments + "\n"
                  + " Found:\n" + _existingDepartments);
        }
        final HashMap<String, TableInfo.Column> _columnsTeachers = new HashMap<String, TableInfo.Column>(25);
        _columnsTeachers.put("teacherId", new TableInfo.Column("teacherId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeachers.put("entityId", new TableInfo.Column("entityId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeachers.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeachers.put("email", new TableInfo.Column("email", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeachers.put("phone", new TableInfo.Column("phone", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeachers.put("deptId", new TableInfo.Column("deptId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeachers.put("designation", new TableInfo.Column("designation", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeachers.put("qualification", new TableInfo.Column("qualification", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeachers.put("specialization", new TableInfo.Column("specialization", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeachers.put("officeRoom", new TableInfo.Column("officeRoom", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeachers.put("gender", new TableInfo.Column("gender", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeachers.put("canApproveLinkRequests", new TableInfo.Column("canApproveLinkRequests", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeachers.put("canEditTimetable", new TableInfo.Column("canEditTimetable", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeachers.put("canSendNotifications", new TableInfo.Column("canSendNotifications", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeachers.put("canManageDatesheets", new TableInfo.Column("canManageDatesheets", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeachers.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeachers.put("isActive", new TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeachers.put("archivedAt", new TableInfo.Column("archivedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeachers.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeachers.put("createdBy", new TableInfo.Column("createdBy", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeachers.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeachers.put("updatedBy", new TableInfo.Column("updatedBy", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeachers.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeachers.put("deletedAt", new TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeachers.put("deletedBy", new TableInfo.Column("deletedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTeachers = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTeachers = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTeachers = new TableInfo("teachers", _columnsTeachers, _foreignKeysTeachers, _indicesTeachers);
        final TableInfo _existingTeachers = TableInfo.read(db, "teachers");
        if (!_infoTeachers.equals(_existingTeachers)) {
          return new RoomOpenHelper.ValidationResult(false, "teachers(com.mbd.cmscommon.data.local.entity.TeacherEntity).\n"
                  + " Expected:\n" + _infoTeachers + "\n"
                  + " Found:\n" + _existingTeachers);
        }
        final HashMap<String, TableInfo.Column> _columnsAdministratorAccounts = new HashMap<String, TableInfo.Column>(12);
        _columnsAdministratorAccounts.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAdministratorAccounts.put("email", new TableInfo.Column("email", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAdministratorAccounts.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAdministratorAccounts.put("lastLoginAt", new TableInfo.Column("lastLoginAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAdministratorAccounts.put("entityId", new TableInfo.Column("entityId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAdministratorAccounts.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAdministratorAccounts.put("createdBy", new TableInfo.Column("createdBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAdministratorAccounts.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAdministratorAccounts.put("updatedBy", new TableInfo.Column("updatedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAdministratorAccounts.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAdministratorAccounts.put("deletedAt", new TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAdministratorAccounts.put("deletedBy", new TableInfo.Column("deletedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAdministratorAccounts = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAdministratorAccounts = new HashSet<TableInfo.Index>(2);
        _indicesAdministratorAccounts.add(new TableInfo.Index("index_administrator_accounts_email", true, Arrays.asList("email"), Arrays.asList("ASC")));
        _indicesAdministratorAccounts.add(new TableInfo.Index("index_administrator_accounts_updatedAt_entityId", false, Arrays.asList("updatedAt", "entityId"), Arrays.asList("ASC", "ASC")));
        final TableInfo _infoAdministratorAccounts = new TableInfo("administrator_accounts", _columnsAdministratorAccounts, _foreignKeysAdministratorAccounts, _indicesAdministratorAccounts);
        final TableInfo _existingAdministratorAccounts = TableInfo.read(db, "administrator_accounts");
        if (!_infoAdministratorAccounts.equals(_existingAdministratorAccounts)) {
          return new RoomOpenHelper.ValidationResult(false, "administrator_accounts(com.mbd.cmscommon.data.local.entity.AdministratorAccountEntity).\n"
                  + " Expected:\n" + _infoAdministratorAccounts + "\n"
                  + " Found:\n" + _existingAdministratorAccounts);
        }
        final HashMap<String, TableInfo.Column> _columnsUsers = new HashMap<String, TableInfo.Column>(5);
        _columnsUsers.put("uid", new TableInfo.Column("uid", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("role", new TableInfo.Column("role", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("teacherId", new TableInfo.Column("teacherId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("linkedStudentId", new TableInfo.Column("linkedStudentId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUsers = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUsers = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoUsers = new TableInfo("users", _columnsUsers, _foreignKeysUsers, _indicesUsers);
        final TableInfo _existingUsers = TableInfo.read(db, "users");
        if (!_infoUsers.equals(_existingUsers)) {
          return new RoomOpenHelper.ValidationResult(false, "users(com.mbd.cmscommon.data.local.entity.UserEntity).\n"
                  + " Expected:\n" + _infoUsers + "\n"
                  + " Found:\n" + _existingUsers);
        }
        final HashMap<String, TableInfo.Column> _columnsStudentLinkRequests = new HashMap<String, TableInfo.Column>(23);
        _columnsStudentLinkRequests.put("requestId", new TableInfo.Column("requestId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentLinkRequests.put("requestedByUid", new TableInfo.Column("requestedByUid", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentLinkRequests.put("sessionIdClaimed", new TableInfo.Column("sessionIdClaimed", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentLinkRequests.put("rollNumberClaimed", new TableInfo.Column("rollNumberClaimed", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentLinkRequests.put("nameClaimed", new TableInfo.Column("nameClaimed", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentLinkRequests.put("cnicClaimed", new TableInfo.Column("cnicClaimed", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentLinkRequests.put("dobClaimed", new TableInfo.Column("dobClaimed", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentLinkRequests.put("universityRollClaimed", new TableInfo.Column("universityRollClaimed", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentLinkRequests.put("registrationNoClaimed", new TableInfo.Column("registrationNoClaimed", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentLinkRequests.put("message", new TableInfo.Column("message", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentLinkRequests.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentLinkRequests.put("reviewedBy", new TableInfo.Column("reviewedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentLinkRequests.put("reviewedAt", new TableInfo.Column("reviewedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentLinkRequests.put("rejectionReason", new TableInfo.Column("rejectionReason", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentLinkRequests.put("attemptCount", new TableInfo.Column("attemptCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentLinkRequests.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentLinkRequests.put("entityId", new TableInfo.Column("entityId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentLinkRequests.put("createdBy", new TableInfo.Column("createdBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentLinkRequests.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentLinkRequests.put("updatedBy", new TableInfo.Column("updatedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentLinkRequests.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentLinkRequests.put("deletedAt", new TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentLinkRequests.put("deletedBy", new TableInfo.Column("deletedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysStudentLinkRequests = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesStudentLinkRequests = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoStudentLinkRequests = new TableInfo("student_link_requests", _columnsStudentLinkRequests, _foreignKeysStudentLinkRequests, _indicesStudentLinkRequests);
        final TableInfo _existingStudentLinkRequests = TableInfo.read(db, "student_link_requests");
        if (!_infoStudentLinkRequests.equals(_existingStudentLinkRequests)) {
          return new RoomOpenHelper.ValidationResult(false, "student_link_requests(com.mbd.cmscommon.data.local.entity.StudentLinkRequestEntity).\n"
                  + " Expected:\n" + _infoStudentLinkRequests + "\n"
                  + " Found:\n" + _existingStudentLinkRequests);
        }
        final HashMap<String, TableInfo.Column> _columnsDocuments = new HashMap<String, TableInfo.Column>(18);
        _columnsDocuments.put("documentId", new TableInfo.Column("documentId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocuments.put("kind", new TableInfo.Column("kind", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocuments.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocuments.put("storagePath", new TableInfo.Column("storagePath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocuments.put("body", new TableInfo.Column("body", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocuments.put("deptId", new TableInfo.Column("deptId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocuments.put("audience", new TableInfo.Column("audience", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocuments.put("tagsJson", new TableInfo.Column("tagsJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocuments.put("published", new TableInfo.Column("published", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocuments.put("publishedBy", new TableInfo.Column("publishedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocuments.put("entityId", new TableInfo.Column("entityId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocuments.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocuments.put("createdBy", new TableInfo.Column("createdBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocuments.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocuments.put("updatedBy", new TableInfo.Column("updatedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocuments.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocuments.put("deletedAt", new TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocuments.put("deletedBy", new TableInfo.Column("deletedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDocuments = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDocuments = new HashSet<TableInfo.Index>(2);
        _indicesDocuments.add(new TableInfo.Index("index_documents_createdAt", false, Arrays.asList("createdAt"), Arrays.asList("ASC")));
        _indicesDocuments.add(new TableInfo.Index("index_documents_updatedAt_entityId", false, Arrays.asList("updatedAt", "entityId"), Arrays.asList("ASC", "ASC")));
        final TableInfo _infoDocuments = new TableInfo("documents", _columnsDocuments, _foreignKeysDocuments, _indicesDocuments);
        final TableInfo _existingDocuments = TableInfo.read(db, "documents");
        if (!_infoDocuments.equals(_existingDocuments)) {
          return new RoomOpenHelper.ValidationResult(false, "documents(com.mbd.cmscommon.data.local.entity.DocumentEntity).\n"
                  + " Expected:\n" + _infoDocuments + "\n"
                  + " Found:\n" + _existingDocuments);
        }
        final HashMap<String, TableInfo.Column> _columnsExamPaperSubmissions = new HashMap<String, TableInfo.Column>(16);
        _columnsExamPaperSubmissions.put("submissionId", new TableInfo.Column("submissionId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExamPaperSubmissions.put("offeringId", new TableInfo.Column("offeringId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExamPaperSubmissions.put("subjectId", new TableInfo.Column("subjectId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExamPaperSubmissions.put("examType", new TableInfo.Column("examType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExamPaperSubmissions.put("teacherId", new TableInfo.Column("teacherId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExamPaperSubmissions.put("storagePath", new TableInfo.Column("storagePath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExamPaperSubmissions.put("fileName", new TableInfo.Column("fileName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExamPaperSubmissions.put("uploadedAt", new TableInfo.Column("uploadedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExamPaperSubmissions.put("createdBy", new TableInfo.Column("createdBy", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExamPaperSubmissions.put("entityId", new TableInfo.Column("entityId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExamPaperSubmissions.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExamPaperSubmissions.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExamPaperSubmissions.put("updatedBy", new TableInfo.Column("updatedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExamPaperSubmissions.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExamPaperSubmissions.put("deletedAt", new TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExamPaperSubmissions.put("deletedBy", new TableInfo.Column("deletedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysExamPaperSubmissions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesExamPaperSubmissions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoExamPaperSubmissions = new TableInfo("exam_paper_submissions", _columnsExamPaperSubmissions, _foreignKeysExamPaperSubmissions, _indicesExamPaperSubmissions);
        final TableInfo _existingExamPaperSubmissions = TableInfo.read(db, "exam_paper_submissions");
        if (!_infoExamPaperSubmissions.equals(_existingExamPaperSubmissions)) {
          return new RoomOpenHelper.ValidationResult(false, "exam_paper_submissions(com.mbd.cmscommon.data.local.entity.ExamPaperSubmissionEntity).\n"
                  + " Expected:\n" + _infoExamPaperSubmissions + "\n"
                  + " Found:\n" + _existingExamPaperSubmissions);
        }
        final HashMap<String, TableInfo.Column> _columnsNotifications = new HashMap<String, TableInfo.Column>(18);
        _columnsNotifications.put("notificationId", new TableInfo.Column("notificationId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("body", new TableInfo.Column("body", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("targetRole", new TableInfo.Column("targetRole", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("targetOfferingId", new TableInfo.Column("targetOfferingId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("createdByUid", new TableInfo.Column("createdByUid", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("priority", new TableInfo.Column("priority", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("targetDeptId", new TableInfo.Column("targetDeptId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("attachmentPath", new TableInfo.Column("attachmentPath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("expiresAt", new TableInfo.Column("expiresAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("entityId", new TableInfo.Column("entityId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("createdBy", new TableInfo.Column("createdBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("updatedBy", new TableInfo.Column("updatedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("deletedAt", new TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("deletedBy", new TableInfo.Column("deletedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysNotifications = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesNotifications = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoNotifications = new TableInfo("notifications", _columnsNotifications, _foreignKeysNotifications, _indicesNotifications);
        final TableInfo _existingNotifications = TableInfo.read(db, "notifications");
        if (!_infoNotifications.equals(_existingNotifications)) {
          return new RoomOpenHelper.ValidationResult(false, "notifications(com.mbd.cmscommon.data.local.entity.NotificationEntity).\n"
                  + " Expected:\n" + _infoNotifications + "\n"
                  + " Found:\n" + _existingNotifications);
        }
        final HashMap<String, TableInfo.Column> _columnsSyncState = new HashMap<String, TableInfo.Column>(2);
        _columnsSyncState.put("collectionName", new TableInfo.Column("collectionName", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncState.put("lastSyncedAt", new TableInfo.Column("lastSyncedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSyncState = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSyncState = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSyncState = new TableInfo("sync_state", _columnsSyncState, _foreignKeysSyncState, _indicesSyncState);
        final TableInfo _existingSyncState = TableInfo.read(db, "sync_state");
        if (!_infoSyncState.equals(_existingSyncState)) {
          return new RoomOpenHelper.ValidationResult(false, "sync_state(com.mbd.cmscommon.data.local.entity.SyncStateEntity).\n"
                  + " Expected:\n" + _infoSyncState + "\n"
                  + " Found:\n" + _existingSyncState);
        }
        final HashMap<String, TableInfo.Column> _columnsTableSyncState = new HashMap<String, TableInfo.Column>(9);
        _columnsTableSyncState.put("owner_key", new TableInfo.Column("owner_key", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTableSyncState.put("table_name", new TableInfo.Column("table_name", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTableSyncState.put("scope_key", new TableInfo.Column("scope_key", "TEXT", true, 3, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTableSyncState.put("last_updated_at", new TableInfo.Column("last_updated_at", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTableSyncState.put("last_successful_sync_at", new TableInfo.Column("last_successful_sync_at", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTableSyncState.put("created_at", new TableInfo.Column("created_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTableSyncState.put("created_by", new TableInfo.Column("created_by", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTableSyncState.put("updated_at", new TableInfo.Column("updated_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTableSyncState.put("updated_by", new TableInfo.Column("updated_by", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTableSyncState = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTableSyncState = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTableSyncState = new TableInfo("table_sync_state", _columnsTableSyncState, _foreignKeysTableSyncState, _indicesTableSyncState);
        final TableInfo _existingTableSyncState = TableInfo.read(db, "table_sync_state");
        if (!_infoTableSyncState.equals(_existingTableSyncState)) {
          return new RoomOpenHelper.ValidationResult(false, "table_sync_state(com.mbd.cmscommon.data.local.entity.TableSyncStateEntity).\n"
                  + " Expected:\n" + _infoTableSyncState + "\n"
                  + " Found:\n" + _existingTableSyncState);
        }
        final HashMap<String, TableInfo.Column> _columnsAcademicSessions = new HashMap<String, TableInfo.Column>(19);
        _columnsAcademicSessions.put("sessionId", new TableInfo.Column("sessionId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAcademicSessions.put("deptId", new TableInfo.Column("deptId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAcademicSessions.put("startYear", new TableInfo.Column("startYear", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAcademicSessions.put("endYear", new TableInfo.Column("endYear", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAcademicSessions.put("shift", new TableInfo.Column("shift", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAcademicSessions.put("currentSemester", new TableInfo.Column("currentSemester", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAcademicSessions.put("isActive", new TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAcademicSessions.put("programName", new TableInfo.Column("programName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAcademicSessions.put("inchargeEmail", new TableInfo.Column("inchargeEmail", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAcademicSessions.put("maxStudents", new TableInfo.Column("maxStudents", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAcademicSessions.put("archivedAt", new TableInfo.Column("archivedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAcademicSessions.put("entityId", new TableInfo.Column("entityId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAcademicSessions.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAcademicSessions.put("createdBy", new TableInfo.Column("createdBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAcademicSessions.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAcademicSessions.put("updatedBy", new TableInfo.Column("updatedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAcademicSessions.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAcademicSessions.put("deletedAt", new TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAcademicSessions.put("deletedBy", new TableInfo.Column("deletedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAcademicSessions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAcademicSessions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAcademicSessions = new TableInfo("academic_sessions", _columnsAcademicSessions, _foreignKeysAcademicSessions, _indicesAcademicSessions);
        final TableInfo _existingAcademicSessions = TableInfo.read(db, "academic_sessions");
        if (!_infoAcademicSessions.equals(_existingAcademicSessions)) {
          return new RoomOpenHelper.ValidationResult(false, "academic_sessions(com.mbd.cmscommon.data.local.entity.AcademicSessionEntity).\n"
                  + " Expected:\n" + _infoAcademicSessions + "\n"
                  + " Found:\n" + _existingAcademicSessions);
        }
        final HashMap<String, TableInfo.Column> _columnsSemesterSubjects = new HashMap<String, TableInfo.Column>(17);
        _columnsSemesterSubjects.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSemesterSubjects.put("sessionId", new TableInfo.Column("sessionId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSemesterSubjects.put("semester", new TableInfo.Column("semester", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSemesterSubjects.put("courseCode", new TableInfo.Column("courseCode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSemesterSubjects.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSemesterSubjects.put("creditHours", new TableInfo.Column("creditHours", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSemesterSubjects.put("subjectType", new TableInfo.Column("subjectType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSemesterSubjects.put("isElective", new TableInfo.Column("isElective", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSemesterSubjects.put("outline", new TableInfo.Column("outline", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSemesterSubjects.put("entityId", new TableInfo.Column("entityId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSemesterSubjects.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSemesterSubjects.put("createdBy", new TableInfo.Column("createdBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSemesterSubjects.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSemesterSubjects.put("updatedBy", new TableInfo.Column("updatedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSemesterSubjects.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSemesterSubjects.put("deletedAt", new TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSemesterSubjects.put("deletedBy", new TableInfo.Column("deletedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSemesterSubjects = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSemesterSubjects = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSemesterSubjects = new TableInfo("semester_subjects", _columnsSemesterSubjects, _foreignKeysSemesterSubjects, _indicesSemesterSubjects);
        final TableInfo _existingSemesterSubjects = TableInfo.read(db, "semester_subjects");
        if (!_infoSemesterSubjects.equals(_existingSemesterSubjects)) {
          return new RoomOpenHelper.ValidationResult(false, "semester_subjects(com.mbd.cmscommon.data.local.entity.SemesterSubjectEntity).\n"
                  + " Expected:\n" + _infoSemesterSubjects + "\n"
                  + " Found:\n" + _existingSemesterSubjects);
        }
        final HashMap<String, TableInfo.Column> _columnsSessionStudents = new HashMap<String, TableInfo.Column>(16);
        _columnsSessionStudents.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionStudents.put("sessionId", new TableInfo.Column("sessionId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionStudents.put("deptId", new TableInfo.Column("deptId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionStudents.put("rollNumber", new TableInfo.Column("rollNumber", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionStudents.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionStudents.put("linkedEmail", new TableInfo.Column("linkedEmail", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionStudents.put("gpa", new TableInfo.Column("gpa", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionStudents.put("cgpa", new TableInfo.Column("cgpa", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionStudents.put("entityId", new TableInfo.Column("entityId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionStudents.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionStudents.put("createdBy", new TableInfo.Column("createdBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionStudents.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionStudents.put("updatedBy", new TableInfo.Column("updatedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionStudents.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionStudents.put("deletedAt", new TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionStudents.put("deletedBy", new TableInfo.Column("deletedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSessionStudents = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSessionStudents = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSessionStudents = new TableInfo("session_students", _columnsSessionStudents, _foreignKeysSessionStudents, _indicesSessionStudents);
        final TableInfo _existingSessionStudents = TableInfo.read(db, "session_students");
        if (!_infoSessionStudents.equals(_existingSessionStudents)) {
          return new RoomOpenHelper.ValidationResult(false, "session_students(com.mbd.cmscommon.data.local.entity.SessionStudentEntity).\n"
                  + " Expected:\n" + _infoSessionStudents + "\n"
                  + " Found:\n" + _existingSessionStudents);
        }
        final HashMap<String, TableInfo.Column> _columnsSessionPeriods = new HashMap<String, TableInfo.Column>(25);
        _columnsSessionPeriods.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionPeriods.put("sessionId", new TableInfo.Column("sessionId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionPeriods.put("deptId", new TableInfo.Column("deptId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionPeriods.put("day", new TableInfo.Column("day", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionPeriods.put("startTime", new TableInfo.Column("startTime", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionPeriods.put("endTime", new TableInfo.Column("endTime", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionPeriods.put("courseCode", new TableInfo.Column("courseCode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionPeriods.put("subjectName", new TableInfo.Column("subjectName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionPeriods.put("teacherId", new TableInfo.Column("teacherId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionPeriods.put("teacherName", new TableInfo.Column("teacherName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionPeriods.put("periodType", new TableInfo.Column("periodType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionPeriods.put("creditHours", new TableInfo.Column("creditHours", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionPeriods.put("roomNo", new TableInfo.Column("roomNo", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionPeriods.put("building", new TableInfo.Column("building", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionPeriods.put("notes", new TableInfo.Column("notes", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionPeriods.put("effectiveFrom", new TableInfo.Column("effectiveFrom", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionPeriods.put("effectiveTo", new TableInfo.Column("effectiveTo", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionPeriods.put("entityId", new TableInfo.Column("entityId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionPeriods.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionPeriods.put("createdBy", new TableInfo.Column("createdBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionPeriods.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionPeriods.put("updatedBy", new TableInfo.Column("updatedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionPeriods.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionPeriods.put("deletedAt", new TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionPeriods.put("deletedBy", new TableInfo.Column("deletedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSessionPeriods = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSessionPeriods = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSessionPeriods = new TableInfo("session_periods", _columnsSessionPeriods, _foreignKeysSessionPeriods, _indicesSessionPeriods);
        final TableInfo _existingSessionPeriods = TableInfo.read(db, "session_periods");
        if (!_infoSessionPeriods.equals(_existingSessionPeriods)) {
          return new RoomOpenHelper.ValidationResult(false, "session_periods(com.mbd.cmscommon.data.local.entity.SessionPeriodEntity).\n"
                  + " Expected:\n" + _infoSessionPeriods + "\n"
                  + " Found:\n" + _existingSessionPeriods);
        }
        final HashMap<String, TableInfo.Column> _columnsSessionAttendanceTallies = new HashMap<String, TableInfo.Column>(7);
        _columnsSessionAttendanceTallies.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionAttendanceTallies.put("sessionId", new TableInfo.Column("sessionId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionAttendanceTallies.put("courseCode", new TableInfo.Column("courseCode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionAttendanceTallies.put("rollNumber", new TableInfo.Column("rollNumber", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionAttendanceTallies.put("present", new TableInfo.Column("present", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionAttendanceTallies.put("absent", new TableInfo.Column("absent", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionAttendanceTallies.put("leave", new TableInfo.Column("leave", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSessionAttendanceTallies = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSessionAttendanceTallies = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSessionAttendanceTallies = new TableInfo("session_attendance_tallies", _columnsSessionAttendanceTallies, _foreignKeysSessionAttendanceTallies, _indicesSessionAttendanceTallies);
        final TableInfo _existingSessionAttendanceTallies = TableInfo.read(db, "session_attendance_tallies");
        if (!_infoSessionAttendanceTallies.equals(_existingSessionAttendanceTallies)) {
          return new RoomOpenHelper.ValidationResult(false, "session_attendance_tallies(com.mbd.cmscommon.data.local.entity.SessionAttendanceTallyEntity).\n"
                  + " Expected:\n" + _infoSessionAttendanceTallies + "\n"
                  + " Found:\n" + _existingSessionAttendanceTallies);
        }
        final HashMap<String, TableInfo.Column> _columnsSessionAttendanceRows = new HashMap<String, TableInfo.Column>(20);
        _columnsSessionAttendanceRows.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionAttendanceRows.put("sessionId", new TableInfo.Column("sessionId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionAttendanceRows.put("semester", new TableInfo.Column("semester", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionAttendanceRows.put("courseCode", new TableInfo.Column("courseCode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionAttendanceRows.put("date", new TableInfo.Column("date", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionAttendanceRows.put("rollNumber", new TableInfo.Column("rollNumber", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionAttendanceRows.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionAttendanceRows.put("teacherEmail", new TableInfo.Column("teacherEmail", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionAttendanceRows.put("isLate", new TableInfo.Column("isLate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionAttendanceRows.put("remark", new TableInfo.Column("remark", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionAttendanceRows.put("lectureTopic", new TableInfo.Column("lectureTopic", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionAttendanceRows.put("recordedAt", new TableInfo.Column("recordedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionAttendanceRows.put("entityId", new TableInfo.Column("entityId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionAttendanceRows.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionAttendanceRows.put("createdBy", new TableInfo.Column("createdBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionAttendanceRows.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionAttendanceRows.put("updatedBy", new TableInfo.Column("updatedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionAttendanceRows.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionAttendanceRows.put("deletedAt", new TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionAttendanceRows.put("deletedBy", new TableInfo.Column("deletedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSessionAttendanceRows = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSessionAttendanceRows = new HashSet<TableInfo.Index>(4);
        _indicesSessionAttendanceRows.add(new TableInfo.Index("index_session_attendance_rows_sessionId_courseCode_date_rollNumber", false, Arrays.asList("sessionId", "courseCode", "date", "rollNumber"), Arrays.asList("ASC", "ASC", "ASC", "ASC")));
        _indicesSessionAttendanceRows.add(new TableInfo.Index("index_session_attendance_rows_sessionId_courseCode_updatedAt_entityId", false, Arrays.asList("sessionId", "courseCode", "updatedAt", "entityId"), Arrays.asList("ASC", "ASC", "ASC", "ASC")));
        _indicesSessionAttendanceRows.add(new TableInfo.Index("index_session_attendance_rows_sessionId_updatedAt_entityId", false, Arrays.asList("sessionId", "updatedAt", "entityId"), Arrays.asList("ASC", "ASC", "ASC")));
        _indicesSessionAttendanceRows.add(new TableInfo.Index("index_session_attendance_rows_sessionId_semester", false, Arrays.asList("sessionId", "semester"), Arrays.asList("ASC", "ASC")));
        final TableInfo _infoSessionAttendanceRows = new TableInfo("session_attendance_rows", _columnsSessionAttendanceRows, _foreignKeysSessionAttendanceRows, _indicesSessionAttendanceRows);
        final TableInfo _existingSessionAttendanceRows = TableInfo.read(db, "session_attendance_rows");
        if (!_infoSessionAttendanceRows.equals(_existingSessionAttendanceRows)) {
          return new RoomOpenHelper.ValidationResult(false, "session_attendance_rows(com.mbd.cmscommon.data.local.entity.SessionAttendanceRowEntity).\n"
                  + " Expected:\n" + _infoSessionAttendanceRows + "\n"
                  + " Found:\n" + _existingSessionAttendanceRows);
        }
        final HashMap<String, TableInfo.Column> _columnsSessionMarks = new HashMap<String, TableInfo.Column>(17);
        _columnsSessionMarks.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionMarks.put("sessionId", new TableInfo.Column("sessionId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionMarks.put("courseCode", new TableInfo.Column("courseCode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionMarks.put("examType", new TableInfo.Column("examType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionMarks.put("rollNumber", new TableInfo.Column("rollNumber", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionMarks.put("score", new TableInfo.Column("score", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionMarks.put("maxMarks", new TableInfo.Column("maxMarks", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionMarks.put("wasAbsent", new TableInfo.Column("wasAbsent", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionMarks.put("remarks", new TableInfo.Column("remarks", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionMarks.put("entityId", new TableInfo.Column("entityId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionMarks.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionMarks.put("createdBy", new TableInfo.Column("createdBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionMarks.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionMarks.put("updatedBy", new TableInfo.Column("updatedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionMarks.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionMarks.put("deletedAt", new TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionMarks.put("deletedBy", new TableInfo.Column("deletedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSessionMarks = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSessionMarks = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSessionMarks = new TableInfo("session_marks", _columnsSessionMarks, _foreignKeysSessionMarks, _indicesSessionMarks);
        final TableInfo _existingSessionMarks = TableInfo.read(db, "session_marks");
        if (!_infoSessionMarks.equals(_existingSessionMarks)) {
          return new RoomOpenHelper.ValidationResult(false, "session_marks(com.mbd.cmscommon.data.local.entity.SessionMarkEntity).\n"
                  + " Expected:\n" + _infoSessionMarks + "\n"
                  + " Found:\n" + _existingSessionMarks);
        }
        final HashMap<String, TableInfo.Column> _columnsStudentSemesterGpa = new HashMap<String, TableInfo.Column>(19);
        _columnsStudentSemesterGpa.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentSemesterGpa.put("sessionId", new TableInfo.Column("sessionId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentSemesterGpa.put("rollNumber", new TableInfo.Column("rollNumber", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentSemesterGpa.put("semester", new TableInfo.Column("semester", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentSemesterGpa.put("gpa", new TableInfo.Column("gpa", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentSemesterGpa.put("cgpa", new TableInfo.Column("cgpa", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentSemesterGpa.put("termLabel", new TableInfo.Column("termLabel", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentSemesterGpa.put("resultStatus", new TableInfo.Column("resultStatus", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentSemesterGpa.put("classPosition", new TableInfo.Column("classPosition", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentSemesterGpa.put("remarks", new TableInfo.Column("remarks", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentSemesterGpa.put("supplyCoursesJson", new TableInfo.Column("supplyCoursesJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentSemesterGpa.put("entityId", new TableInfo.Column("entityId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentSemesterGpa.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentSemesterGpa.put("createdBy", new TableInfo.Column("createdBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentSemesterGpa.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentSemesterGpa.put("updatedBy", new TableInfo.Column("updatedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentSemesterGpa.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentSemesterGpa.put("deletedAt", new TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStudentSemesterGpa.put("deletedBy", new TableInfo.Column("deletedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysStudentSemesterGpa = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesStudentSemesterGpa = new HashSet<TableInfo.Index>(4);
        _indicesStudentSemesterGpa.add(new TableInfo.Index("index_student_semester_gpa_sessionId_rollNumber_semester", false, Arrays.asList("sessionId", "rollNumber", "semester"), Arrays.asList("ASC", "ASC", "ASC")));
        _indicesStudentSemesterGpa.add(new TableInfo.Index("index_student_semester_gpa_sessionId_semester_rollNumber", false, Arrays.asList("sessionId", "semester", "rollNumber"), Arrays.asList("ASC", "ASC", "ASC")));
        _indicesStudentSemesterGpa.add(new TableInfo.Index("index_student_semester_gpa_sessionId_rollNumber_updatedAt_entityId", false, Arrays.asList("sessionId", "rollNumber", "updatedAt", "entityId"), Arrays.asList("ASC", "ASC", "ASC", "ASC")));
        _indicesStudentSemesterGpa.add(new TableInfo.Index("index_student_semester_gpa_sessionId_semester_updatedAt_entityId", false, Arrays.asList("sessionId", "semester", "updatedAt", "entityId"), Arrays.asList("ASC", "ASC", "ASC", "ASC")));
        final TableInfo _infoStudentSemesterGpa = new TableInfo("student_semester_gpa", _columnsStudentSemesterGpa, _foreignKeysStudentSemesterGpa, _indicesStudentSemesterGpa);
        final TableInfo _existingStudentSemesterGpa = TableInfo.read(db, "student_semester_gpa");
        if (!_infoStudentSemesterGpa.equals(_existingStudentSemesterGpa)) {
          return new RoomOpenHelper.ValidationResult(false, "student_semester_gpa(com.mbd.cmscommon.data.local.entity.StudentSemesterGpaEntity).\n"
                  + " Expected:\n" + _infoStudentSemesterGpa + "\n"
                  + " Found:\n" + _existingStudentSemesterGpa);
        }
        final HashMap<String, TableInfo.Column> _columnsSessionFees = new HashMap<String, TableInfo.Column>(14);
        _columnsSessionFees.put("sessionId", new TableInfo.Column("sessionId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionFees.put("cadence", new TableInfo.Column("cadence", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionFees.put("academicYear", new TableInfo.Column("academicYear", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionFees.put("dueDate", new TableInfo.Column("dueDate", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionFees.put("lateFineNote", new TableInfo.Column("lateFineNote", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionFees.put("paymentNote", new TableInfo.Column("paymentNote", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionFees.put("entityId", new TableInfo.Column("entityId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionFees.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionFees.put("createdBy", new TableInfo.Column("createdBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionFees.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionFees.put("updatedBy", new TableInfo.Column("updatedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionFees.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionFees.put("deletedAt", new TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionFees.put("deletedBy", new TableInfo.Column("deletedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSessionFees = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSessionFees = new HashSet<TableInfo.Index>(1);
        _indicesSessionFees.add(new TableInfo.Index("index_session_fees_updatedAt_entityId", false, Arrays.asList("updatedAt", "entityId"), Arrays.asList("ASC", "ASC")));
        final TableInfo _infoSessionFees = new TableInfo("session_fees", _columnsSessionFees, _foreignKeysSessionFees, _indicesSessionFees);
        final TableInfo _existingSessionFees = TableInfo.read(db, "session_fees");
        if (!_infoSessionFees.equals(_existingSessionFees)) {
          return new RoomOpenHelper.ValidationResult(false, "session_fees(com.mbd.cmscommon.data.local.entity.SessionFeeEntity).\n"
                  + " Expected:\n" + _infoSessionFees + "\n"
                  + " Found:\n" + _existingSessionFees);
        }
        final HashMap<String, TableInfo.Column> _columnsSessionFeeHeads = new HashMap<String, TableInfo.Column>(13);
        _columnsSessionFeeHeads.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionFeeHeads.put("sessionId", new TableInfo.Column("sessionId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionFeeHeads.put("label", new TableInfo.Column("label", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionFeeHeads.put("amount", new TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionFeeHeads.put("position", new TableInfo.Column("position", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionFeeHeads.put("entityId", new TableInfo.Column("entityId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionFeeHeads.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionFeeHeads.put("createdBy", new TableInfo.Column("createdBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionFeeHeads.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionFeeHeads.put("updatedBy", new TableInfo.Column("updatedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionFeeHeads.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionFeeHeads.put("deletedAt", new TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionFeeHeads.put("deletedBy", new TableInfo.Column("deletedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSessionFeeHeads = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSessionFeeHeads = new HashSet<TableInfo.Index>(2);
        _indicesSessionFeeHeads.add(new TableInfo.Index("index_session_fee_heads_sessionId", false, Arrays.asList("sessionId"), Arrays.asList("ASC")));
        _indicesSessionFeeHeads.add(new TableInfo.Index("index_session_fee_heads_updatedAt_entityId", false, Arrays.asList("updatedAt", "entityId"), Arrays.asList("ASC", "ASC")));
        final TableInfo _infoSessionFeeHeads = new TableInfo("session_fee_heads", _columnsSessionFeeHeads, _foreignKeysSessionFeeHeads, _indicesSessionFeeHeads);
        final TableInfo _existingSessionFeeHeads = TableInfo.read(db, "session_fee_heads");
        if (!_infoSessionFeeHeads.equals(_existingSessionFeeHeads)) {
          return new RoomOpenHelper.ValidationResult(false, "session_fee_heads(com.mbd.cmscommon.data.local.entity.SessionFeeHeadEntity).\n"
                  + " Expected:\n" + _infoSessionFeeHeads + "\n"
                  + " Found:\n" + _existingSessionFeeHeads);
        }
        final HashMap<String, TableInfo.Column> _columnsFines = new HashMap<String, TableInfo.Column>(16);
        _columnsFines.put("fineId", new TableInfo.Column("fineId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFines.put("sessionId", new TableInfo.Column("sessionId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFines.put("rollNumber", new TableInfo.Column("rollNumber", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFines.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFines.put("amount", new TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFines.put("reason", new TableInfo.Column("reason", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFines.put("issuedBy", new TableInfo.Column("issuedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFines.put("issuedAt", new TableInfo.Column("issuedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFines.put("entityId", new TableInfo.Column("entityId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFines.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFines.put("createdBy", new TableInfo.Column("createdBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFines.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFines.put("updatedBy", new TableInfo.Column("updatedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFines.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFines.put("deletedAt", new TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFines.put("deletedBy", new TableInfo.Column("deletedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysFines = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesFines = new HashSet<TableInfo.Index>(2);
        _indicesFines.add(new TableInfo.Index("index_fines_sessionId_rollNumber", false, Arrays.asList("sessionId", "rollNumber"), Arrays.asList("ASC", "ASC")));
        _indicesFines.add(new TableInfo.Index("index_fines_updatedAt_entityId", false, Arrays.asList("updatedAt", "entityId"), Arrays.asList("ASC", "ASC")));
        final TableInfo _infoFines = new TableInfo("fines", _columnsFines, _foreignKeysFines, _indicesFines);
        final TableInfo _existingFines = TableInfo.read(db, "fines");
        if (!_infoFines.equals(_existingFines)) {
          return new RoomOpenHelper.ValidationResult(false, "fines(com.mbd.cmscommon.data.local.entity.FineEntity).\n"
                  + " Expected:\n" + _infoFines + "\n"
                  + " Found:\n" + _existingFines);
        }
        final HashMap<String, TableInfo.Column> _columnsCalendarEvents = new HashMap<String, TableInfo.Column>(20);
        _columnsCalendarEvents.put("eventId", new TableInfo.Column("eventId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalendarEvents.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalendarEvents.put("eventType", new TableInfo.Column("eventType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalendarEvents.put("startDate", new TableInfo.Column("startDate", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalendarEvents.put("endDate", new TableInfo.Column("endDate", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalendarEvents.put("startTime", new TableInfo.Column("startTime", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalendarEvents.put("endTime", new TableInfo.Column("endTime", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalendarEvents.put("description", new TableInfo.Column("description", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalendarEvents.put("venue", new TableInfo.Column("venue", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalendarEvents.put("audience", new TableInfo.Column("audience", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalendarEvents.put("deptId", new TableInfo.Column("deptId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalendarEvents.put("sessionId", new TableInfo.Column("sessionId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalendarEvents.put("entityId", new TableInfo.Column("entityId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalendarEvents.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalendarEvents.put("createdBy", new TableInfo.Column("createdBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalendarEvents.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalendarEvents.put("updatedBy", new TableInfo.Column("updatedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalendarEvents.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalendarEvents.put("deletedAt", new TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalendarEvents.put("deletedBy", new TableInfo.Column("deletedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCalendarEvents = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCalendarEvents = new HashSet<TableInfo.Index>(2);
        _indicesCalendarEvents.add(new TableInfo.Index("index_calendar_events_startDate", false, Arrays.asList("startDate"), Arrays.asList("ASC")));
        _indicesCalendarEvents.add(new TableInfo.Index("index_calendar_events_updatedAt_entityId", false, Arrays.asList("updatedAt", "entityId"), Arrays.asList("ASC", "ASC")));
        final TableInfo _infoCalendarEvents = new TableInfo("calendar_events", _columnsCalendarEvents, _foreignKeysCalendarEvents, _indicesCalendarEvents);
        final TableInfo _existingCalendarEvents = TableInfo.read(db, "calendar_events");
        if (!_infoCalendarEvents.equals(_existingCalendarEvents)) {
          return new RoomOpenHelper.ValidationResult(false, "calendar_events(com.mbd.cmscommon.data.local.entity.CalendarEventEntity).\n"
                  + " Expected:\n" + _infoCalendarEvents + "\n"
                  + " Found:\n" + _existingCalendarEvents);
        }
        final HashMap<String, TableInfo.Column> _columnsMarkEditRequests = new HashMap<String, TableInfo.Column>(22);
        _columnsMarkEditRequests.put("requestId", new TableInfo.Column("requestId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMarkEditRequests.put("sessionId", new TableInfo.Column("sessionId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMarkEditRequests.put("semester", new TableInfo.Column("semester", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMarkEditRequests.put("courseCode", new TableInfo.Column("courseCode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMarkEditRequests.put("examType", new TableInfo.Column("examType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMarkEditRequests.put("rollNumber", new TableInfo.Column("rollNumber", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMarkEditRequests.put("currentScore", new TableInfo.Column("currentScore", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMarkEditRequests.put("requestedScore", new TableInfo.Column("requestedScore", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMarkEditRequests.put("reason", new TableInfo.Column("reason", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMarkEditRequests.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMarkEditRequests.put("requestedBy", new TableInfo.Column("requestedBy", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMarkEditRequests.put("reviewedBy", new TableInfo.Column("reviewedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMarkEditRequests.put("requestedAt", new TableInfo.Column("requestedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMarkEditRequests.put("reviewedAt", new TableInfo.Column("reviewedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMarkEditRequests.put("entityId", new TableInfo.Column("entityId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMarkEditRequests.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMarkEditRequests.put("createdBy", new TableInfo.Column("createdBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMarkEditRequests.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMarkEditRequests.put("updatedBy", new TableInfo.Column("updatedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMarkEditRequests.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMarkEditRequests.put("deletedAt", new TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMarkEditRequests.put("deletedBy", new TableInfo.Column("deletedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMarkEditRequests = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMarkEditRequests = new HashSet<TableInfo.Index>(4);
        _indicesMarkEditRequests.add(new TableInfo.Index("index_mark_edit_requests_sessionId_courseCode_examType_status_rollNumber", false, Arrays.asList("sessionId", "courseCode", "examType", "status", "rollNumber"), Arrays.asList("ASC", "ASC", "ASC", "ASC", "ASC")));
        _indicesMarkEditRequests.add(new TableInfo.Index("index_mark_edit_requests_sessionId_courseCode_examType_status_updatedAt_entityId", false, Arrays.asList("sessionId", "courseCode", "examType", "status", "updatedAt", "entityId"), Arrays.asList("ASC", "ASC", "ASC", "ASC", "ASC", "ASC")));
        _indicesMarkEditRequests.add(new TableInfo.Index("index_mark_edit_requests_status_requestedAt", false, Arrays.asList("status", "requestedAt"), Arrays.asList("ASC", "ASC")));
        _indicesMarkEditRequests.add(new TableInfo.Index("index_mark_edit_requests_status_updatedAt_entityId", false, Arrays.asList("status", "updatedAt", "entityId"), Arrays.asList("ASC", "ASC", "ASC")));
        final TableInfo _infoMarkEditRequests = new TableInfo("mark_edit_requests", _columnsMarkEditRequests, _foreignKeysMarkEditRequests, _indicesMarkEditRequests);
        final TableInfo _existingMarkEditRequests = TableInfo.read(db, "mark_edit_requests");
        if (!_infoMarkEditRequests.equals(_existingMarkEditRequests)) {
          return new RoomOpenHelper.ValidationResult(false, "mark_edit_requests(com.mbd.cmscommon.data.local.entity.MarkEditRequestEntity).\n"
                  + " Expected:\n" + _infoMarkEditRequests + "\n"
                  + " Found:\n" + _existingMarkEditRequests);
        }
        final HashMap<String, TableInfo.Column> _columnsInsightSessionOverviews = new HashMap<String, TableInfo.Column>(8);
        _columnsInsightSessionOverviews.put("sessionId", new TableInfo.Column("sessionId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInsightSessionOverviews.put("deptId", new TableInfo.Column("deptId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInsightSessionOverviews.put("shift", new TableInfo.Column("shift", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInsightSessionOverviews.put("currentSemester", new TableInfo.Column("currentSemester", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInsightSessionOverviews.put("students", new TableInfo.Column("students", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInsightSessionOverviews.put("avgCgpa", new TableInfo.Column("avgCgpa", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInsightSessionOverviews.put("avgAttendance", new TableInfo.Column("avgAttendance", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInsightSessionOverviews.put("cachedAt", new TableInfo.Column("cachedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysInsightSessionOverviews = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesInsightSessionOverviews = new HashSet<TableInfo.Index>(1);
        _indicesInsightSessionOverviews.add(new TableInfo.Index("index_insight_session_overviews_deptId_sessionId", false, Arrays.asList("deptId", "sessionId"), Arrays.asList("ASC", "ASC")));
        final TableInfo _infoInsightSessionOverviews = new TableInfo("insight_session_overviews", _columnsInsightSessionOverviews, _foreignKeysInsightSessionOverviews, _indicesInsightSessionOverviews);
        final TableInfo _existingInsightSessionOverviews = TableInfo.read(db, "insight_session_overviews");
        if (!_infoInsightSessionOverviews.equals(_existingInsightSessionOverviews)) {
          return new RoomOpenHelper.ValidationResult(false, "insight_session_overviews(com.mbd.cmscommon.data.local.entity.InsightSessionOverviewEntity).\n"
                  + " Expected:\n" + _infoInsightSessionOverviews + "\n"
                  + " Found:\n" + _existingInsightSessionOverviews);
        }
        final HashMap<String, TableInfo.Column> _columnsInsightAtRiskStudents = new HashMap<String, TableInfo.Column>(7);
        _columnsInsightAtRiskStudents.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInsightAtRiskStudents.put("sessionId", new TableInfo.Column("sessionId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInsightAtRiskStudents.put("rollNumber", new TableInfo.Column("rollNumber", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInsightAtRiskStudents.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInsightAtRiskStudents.put("cgpa", new TableInfo.Column("cgpa", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInsightAtRiskStudents.put("attendance", new TableInfo.Column("attendance", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInsightAtRiskStudents.put("cachedAt", new TableInfo.Column("cachedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysInsightAtRiskStudents = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesInsightAtRiskStudents = new HashSet<TableInfo.Index>(1);
        _indicesInsightAtRiskStudents.add(new TableInfo.Index("index_insight_at_risk_students_sessionId_rollNumber", false, Arrays.asList("sessionId", "rollNumber"), Arrays.asList("ASC", "ASC")));
        final TableInfo _infoInsightAtRiskStudents = new TableInfo("insight_at_risk_students", _columnsInsightAtRiskStudents, _foreignKeysInsightAtRiskStudents, _indicesInsightAtRiskStudents);
        final TableInfo _existingInsightAtRiskStudents = TableInfo.read(db, "insight_at_risk_students");
        if (!_infoInsightAtRiskStudents.equals(_existingInsightAtRiskStudents)) {
          return new RoomOpenHelper.ValidationResult(false, "insight_at_risk_students(com.mbd.cmscommon.data.local.entity.InsightAtRiskStudentEntity).\n"
                  + " Expected:\n" + _infoInsightAtRiskStudents + "\n"
                  + " Found:\n" + _existingInsightAtRiskStudents);
        }
        final HashMap<String, TableInfo.Column> _columnsInsightExamStats = new HashMap<String, TableInfo.Column>(13);
        _columnsInsightExamStats.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInsightExamStats.put("sessionId", new TableInfo.Column("sessionId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInsightExamStats.put("semester", new TableInfo.Column("semester", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInsightExamStats.put("courseCode", new TableInfo.Column("courseCode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInsightExamStats.put("examType", new TableInfo.Column("examType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInsightExamStats.put("entered", new TableInfo.Column("entered", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInsightExamStats.put("avgScore", new TableInfo.Column("avgScore", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInsightExamStats.put("minScore", new TableInfo.Column("minScore", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInsightExamStats.put("maxScore", new TableInfo.Column("maxScore", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInsightExamStats.put("stddev", new TableInfo.Column("stddev", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInsightExamStats.put("outOf", new TableInfo.Column("outOf", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInsightExamStats.put("passRate", new TableInfo.Column("passRate", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInsightExamStats.put("cachedAt", new TableInfo.Column("cachedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysInsightExamStats = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesInsightExamStats = new HashSet<TableInfo.Index>(1);
        _indicesInsightExamStats.add(new TableInfo.Index("index_insight_exam_stats_sessionId_semester_courseCode_examType", false, Arrays.asList("sessionId", "semester", "courseCode", "examType"), Arrays.asList("ASC", "ASC", "ASC", "ASC")));
        final TableInfo _infoInsightExamStats = new TableInfo("insight_exam_stats", _columnsInsightExamStats, _foreignKeysInsightExamStats, _indicesInsightExamStats);
        final TableInfo _existingInsightExamStats = TableInfo.read(db, "insight_exam_stats");
        if (!_infoInsightExamStats.equals(_existingInsightExamStats)) {
          return new RoomOpenHelper.ValidationResult(false, "insight_exam_stats(com.mbd.cmscommon.data.local.entity.InsightExamStatEntity).\n"
                  + " Expected:\n" + _infoInsightExamStats + "\n"
                  + " Found:\n" + _existingInsightExamStats);
        }
        final HashMap<String, TableInfo.Column> _columnsDatesheets = new HashMap<String, TableInfo.Column>(14);
        _columnsDatesheets.put("datesheetId", new TableInfo.Column("datesheetId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDatesheets.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDatesheets.put("examType", new TableInfo.Column("examType", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDatesheets.put("sessionId", new TableInfo.Column("sessionId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDatesheets.put("published", new TableInfo.Column("published", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDatesheets.put("instructions", new TableInfo.Column("instructions", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDatesheets.put("entityId", new TableInfo.Column("entityId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDatesheets.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDatesheets.put("createdBy", new TableInfo.Column("createdBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDatesheets.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDatesheets.put("updatedBy", new TableInfo.Column("updatedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDatesheets.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDatesheets.put("deletedAt", new TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDatesheets.put("deletedBy", new TableInfo.Column("deletedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDatesheets = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDatesheets = new HashSet<TableInfo.Index>(2);
        _indicesDatesheets.add(new TableInfo.Index("index_datesheets_sessionId", false, Arrays.asList("sessionId"), Arrays.asList("ASC")));
        _indicesDatesheets.add(new TableInfo.Index("index_datesheets_updatedAt_entityId", false, Arrays.asList("updatedAt", "entityId"), Arrays.asList("ASC", "ASC")));
        final TableInfo _infoDatesheets = new TableInfo("datesheets", _columnsDatesheets, _foreignKeysDatesheets, _indicesDatesheets);
        final TableInfo _existingDatesheets = TableInfo.read(db, "datesheets");
        if (!_infoDatesheets.equals(_existingDatesheets)) {
          return new RoomOpenHelper.ValidationResult(false, "datesheets(com.mbd.cmscommon.data.local.entity.DatesheetEntity).\n"
                  + " Expected:\n" + _infoDatesheets + "\n"
                  + " Found:\n" + _existingDatesheets);
        }
        final HashMap<String, TableInfo.Column> _columnsDatesheetSlots = new HashMap<String, TableInfo.Column>(19);
        _columnsDatesheetSlots.put("slotId", new TableInfo.Column("slotId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDatesheetSlots.put("datesheetId", new TableInfo.Column("datesheetId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDatesheetSlots.put("examDate", new TableInfo.Column("examDate", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDatesheetSlots.put("startTime", new TableInfo.Column("startTime", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDatesheetSlots.put("endTime", new TableInfo.Column("endTime", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDatesheetSlots.put("durationMinutes", new TableInfo.Column("durationMinutes", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDatesheetSlots.put("courseCode", new TableInfo.Column("courseCode", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDatesheetSlots.put("subjectName", new TableInfo.Column("subjectName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDatesheetSlots.put("roomNo", new TableInfo.Column("roomNo", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDatesheetSlots.put("building", new TableInfo.Column("building", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDatesheetSlots.put("invigilatorEmail", new TableInfo.Column("invigilatorEmail", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDatesheetSlots.put("entityId", new TableInfo.Column("entityId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDatesheetSlots.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDatesheetSlots.put("createdBy", new TableInfo.Column("createdBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDatesheetSlots.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDatesheetSlots.put("updatedBy", new TableInfo.Column("updatedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDatesheetSlots.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDatesheetSlots.put("deletedAt", new TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDatesheetSlots.put("deletedBy", new TableInfo.Column("deletedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDatesheetSlots = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDatesheetSlots = new HashSet<TableInfo.Index>(2);
        _indicesDatesheetSlots.add(new TableInfo.Index("index_datesheet_slots_datesheetId", false, Arrays.asList("datesheetId"), Arrays.asList("ASC")));
        _indicesDatesheetSlots.add(new TableInfo.Index("index_datesheet_slots_updatedAt_entityId", false, Arrays.asList("updatedAt", "entityId"), Arrays.asList("ASC", "ASC")));
        final TableInfo _infoDatesheetSlots = new TableInfo("datesheet_slots", _columnsDatesheetSlots, _foreignKeysDatesheetSlots, _indicesDatesheetSlots);
        final TableInfo _existingDatesheetSlots = TableInfo.read(db, "datesheet_slots");
        if (!_infoDatesheetSlots.equals(_existingDatesheetSlots)) {
          return new RoomOpenHelper.ValidationResult(false, "datesheet_slots(com.mbd.cmscommon.data.local.entity.DatesheetSlotEntity).\n"
                  + " Expected:\n" + _infoDatesheetSlots + "\n"
                  + " Found:\n" + _existingDatesheetSlots);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "d89e963a609905ae7d1e1de3ee1e54b2", "95deff9c8703797bea813655f8ecfa36");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "departments","teachers","administrator_accounts","users","student_link_requests","documents","exam_paper_submissions","notifications","sync_state","table_sync_state","academic_sessions","semester_subjects","session_students","session_periods","session_attendance_tallies","session_attendance_rows","session_marks","student_semester_gpa","session_fees","session_fee_heads","fines","calendar_events","mark_edit_requests","insight_session_overviews","insight_at_risk_students","insight_exam_stats","datesheets","datesheet_slots");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `departments`");
      _db.execSQL("DELETE FROM `teachers`");
      _db.execSQL("DELETE FROM `administrator_accounts`");
      _db.execSQL("DELETE FROM `users`");
      _db.execSQL("DELETE FROM `student_link_requests`");
      _db.execSQL("DELETE FROM `documents`");
      _db.execSQL("DELETE FROM `exam_paper_submissions`");
      _db.execSQL("DELETE FROM `notifications`");
      _db.execSQL("DELETE FROM `sync_state`");
      _db.execSQL("DELETE FROM `table_sync_state`");
      _db.execSQL("DELETE FROM `academic_sessions`");
      _db.execSQL("DELETE FROM `semester_subjects`");
      _db.execSQL("DELETE FROM `session_students`");
      _db.execSQL("DELETE FROM `session_periods`");
      _db.execSQL("DELETE FROM `session_attendance_tallies`");
      _db.execSQL("DELETE FROM `session_attendance_rows`");
      _db.execSQL("DELETE FROM `session_marks`");
      _db.execSQL("DELETE FROM `student_semester_gpa`");
      _db.execSQL("DELETE FROM `session_fees`");
      _db.execSQL("DELETE FROM `session_fee_heads`");
      _db.execSQL("DELETE FROM `fines`");
      _db.execSQL("DELETE FROM `calendar_events`");
      _db.execSQL("DELETE FROM `mark_edit_requests`");
      _db.execSQL("DELETE FROM `insight_session_overviews`");
      _db.execSQL("DELETE FROM `insight_at_risk_students`");
      _db.execSQL("DELETE FROM `insight_exam_stats`");
      _db.execSQL("DELETE FROM `datesheets`");
      _db.execSQL("DELETE FROM `datesheet_slots`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(DepartmentDao.class, DepartmentDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(AdministratorAccountDao.class, AdministratorAccountDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TeacherDao.class, TeacherDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(UserDao.class, UserDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(StudentLinkRequestDao.class, StudentLinkRequestDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(DocumentDao.class, DocumentDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ExamPaperSubmissionDao.class, ExamPaperSubmissionDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(NotificationDao.class, NotificationDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(FineDao.class, FineDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CalendarEventDao.class, CalendarEventDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(MarkEditRequestDao.class, MarkEditRequestDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(InsightsDao.class, InsightsDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(DatesheetDao.class, DatesheetDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SyncStateDao.class, SyncStateDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TableSyncStateDao.class, TableSyncStateDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(AcademicSessionDao.class, AcademicSessionDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SemesterSubjectDao.class, SemesterSubjectDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SessionStudentDao.class, SessionStudentDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SessionPeriodDao.class, SessionPeriodDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SessionAttendanceDao.class, SessionAttendanceDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SessionMarkDao.class, SessionMarkDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(StudentSemesterGpaDao.class, StudentSemesterGpaDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SessionFeeDao.class, SessionFeeDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public DepartmentDao departmentDao() {
    if (_departmentDao != null) {
      return _departmentDao;
    } else {
      synchronized(this) {
        if(_departmentDao == null) {
          _departmentDao = new DepartmentDao_Impl(this);
        }
        return _departmentDao;
      }
    }
  }

  @Override
  public AdministratorAccountDao administratorAccountDao() {
    if (_administratorAccountDao != null) {
      return _administratorAccountDao;
    } else {
      synchronized(this) {
        if(_administratorAccountDao == null) {
          _administratorAccountDao = new AdministratorAccountDao_Impl(this);
        }
        return _administratorAccountDao;
      }
    }
  }

  @Override
  public TeacherDao teacherDao() {
    if (_teacherDao != null) {
      return _teacherDao;
    } else {
      synchronized(this) {
        if(_teacherDao == null) {
          _teacherDao = new TeacherDao_Impl(this);
        }
        return _teacherDao;
      }
    }
  }

  @Override
  public UserDao userDao() {
    if (_userDao != null) {
      return _userDao;
    } else {
      synchronized(this) {
        if(_userDao == null) {
          _userDao = new UserDao_Impl(this);
        }
        return _userDao;
      }
    }
  }

  @Override
  public StudentLinkRequestDao studentLinkRequestDao() {
    if (_studentLinkRequestDao != null) {
      return _studentLinkRequestDao;
    } else {
      synchronized(this) {
        if(_studentLinkRequestDao == null) {
          _studentLinkRequestDao = new StudentLinkRequestDao_Impl(this);
        }
        return _studentLinkRequestDao;
      }
    }
  }

  @Override
  public DocumentDao documentDao() {
    if (_documentDao != null) {
      return _documentDao;
    } else {
      synchronized(this) {
        if(_documentDao == null) {
          _documentDao = new DocumentDao_Impl(this);
        }
        return _documentDao;
      }
    }
  }

  @Override
  public ExamPaperSubmissionDao examPaperSubmissionDao() {
    if (_examPaperSubmissionDao != null) {
      return _examPaperSubmissionDao;
    } else {
      synchronized(this) {
        if(_examPaperSubmissionDao == null) {
          _examPaperSubmissionDao = new ExamPaperSubmissionDao_Impl(this);
        }
        return _examPaperSubmissionDao;
      }
    }
  }

  @Override
  public NotificationDao notificationDao() {
    if (_notificationDao != null) {
      return _notificationDao;
    } else {
      synchronized(this) {
        if(_notificationDao == null) {
          _notificationDao = new NotificationDao_Impl(this);
        }
        return _notificationDao;
      }
    }
  }

  @Override
  public FineDao fineDao() {
    if (_fineDao != null) {
      return _fineDao;
    } else {
      synchronized(this) {
        if(_fineDao == null) {
          _fineDao = new FineDao_Impl(this);
        }
        return _fineDao;
      }
    }
  }

  @Override
  public CalendarEventDao calendarEventDao() {
    if (_calendarEventDao != null) {
      return _calendarEventDao;
    } else {
      synchronized(this) {
        if(_calendarEventDao == null) {
          _calendarEventDao = new CalendarEventDao_Impl(this);
        }
        return _calendarEventDao;
      }
    }
  }

  @Override
  public MarkEditRequestDao markEditRequestDao() {
    if (_markEditRequestDao != null) {
      return _markEditRequestDao;
    } else {
      synchronized(this) {
        if(_markEditRequestDao == null) {
          _markEditRequestDao = new MarkEditRequestDao_Impl(this);
        }
        return _markEditRequestDao;
      }
    }
  }

  @Override
  public InsightsDao insightsDao() {
    if (_insightsDao != null) {
      return _insightsDao;
    } else {
      synchronized(this) {
        if(_insightsDao == null) {
          _insightsDao = new InsightsDao_Impl(this);
        }
        return _insightsDao;
      }
    }
  }

  @Override
  public DatesheetDao datesheetDao() {
    if (_datesheetDao != null) {
      return _datesheetDao;
    } else {
      synchronized(this) {
        if(_datesheetDao == null) {
          _datesheetDao = new DatesheetDao_Impl(this);
        }
        return _datesheetDao;
      }
    }
  }

  @Override
  public SyncStateDao syncStateDao() {
    if (_syncStateDao != null) {
      return _syncStateDao;
    } else {
      synchronized(this) {
        if(_syncStateDao == null) {
          _syncStateDao = new SyncStateDao_Impl(this);
        }
        return _syncStateDao;
      }
    }
  }

  @Override
  public TableSyncStateDao tableSyncStateDao() {
    if (_tableSyncStateDao != null) {
      return _tableSyncStateDao;
    } else {
      synchronized(this) {
        if(_tableSyncStateDao == null) {
          _tableSyncStateDao = new TableSyncStateDao_Impl(this);
        }
        return _tableSyncStateDao;
      }
    }
  }

  @Override
  public AcademicSessionDao academicSessionDao() {
    if (_academicSessionDao != null) {
      return _academicSessionDao;
    } else {
      synchronized(this) {
        if(_academicSessionDao == null) {
          _academicSessionDao = new AcademicSessionDao_Impl(this);
        }
        return _academicSessionDao;
      }
    }
  }

  @Override
  public SemesterSubjectDao semesterSubjectDao() {
    if (_semesterSubjectDao != null) {
      return _semesterSubjectDao;
    } else {
      synchronized(this) {
        if(_semesterSubjectDao == null) {
          _semesterSubjectDao = new SemesterSubjectDao_Impl(this);
        }
        return _semesterSubjectDao;
      }
    }
  }

  @Override
  public SessionStudentDao sessionStudentDao() {
    if (_sessionStudentDao != null) {
      return _sessionStudentDao;
    } else {
      synchronized(this) {
        if(_sessionStudentDao == null) {
          _sessionStudentDao = new SessionStudentDao_Impl(this);
        }
        return _sessionStudentDao;
      }
    }
  }

  @Override
  public SessionPeriodDao sessionPeriodDao() {
    if (_sessionPeriodDao != null) {
      return _sessionPeriodDao;
    } else {
      synchronized(this) {
        if(_sessionPeriodDao == null) {
          _sessionPeriodDao = new SessionPeriodDao_Impl(this);
        }
        return _sessionPeriodDao;
      }
    }
  }

  @Override
  public SessionAttendanceDao sessionAttendanceDao() {
    if (_sessionAttendanceDao != null) {
      return _sessionAttendanceDao;
    } else {
      synchronized(this) {
        if(_sessionAttendanceDao == null) {
          _sessionAttendanceDao = new SessionAttendanceDao_Impl(this);
        }
        return _sessionAttendanceDao;
      }
    }
  }

  @Override
  public SessionMarkDao sessionMarkDao() {
    if (_sessionMarkDao != null) {
      return _sessionMarkDao;
    } else {
      synchronized(this) {
        if(_sessionMarkDao == null) {
          _sessionMarkDao = new SessionMarkDao_Impl(this);
        }
        return _sessionMarkDao;
      }
    }
  }

  @Override
  public StudentSemesterGpaDao studentSemesterGpaDao() {
    if (_studentSemesterGpaDao != null) {
      return _studentSemesterGpaDao;
    } else {
      synchronized(this) {
        if(_studentSemesterGpaDao == null) {
          _studentSemesterGpaDao = new StudentSemesterGpaDao_Impl(this);
        }
        return _studentSemesterGpaDao;
      }
    }
  }

  @Override
  public SessionFeeDao sessionFeeDao() {
    if (_sessionFeeDao != null) {
      return _sessionFeeDao;
    } else {
      synchronized(this) {
        if(_sessionFeeDao == null) {
          _sessionFeeDao = new SessionFeeDao_Impl(this);
        }
        return _sessionFeeDao;
      }
    }
  }
}
