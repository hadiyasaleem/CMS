package com.mbd.cmscommon.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.EntityUpsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomDatabaseKt;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.mbd.cmscommon.data.local.entity.SessionAttendanceRowEntity;
import com.mbd.cmscommon.data.local.entity.SessionAttendanceTallyEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SessionAttendanceDao_Impl implements SessionAttendanceDao {
  private final RoomDatabase __db;

  private final SharedSQLiteStatement __preparedStmtOfDeleteFor;

  private final SharedSQLiteStatement __preparedStmtOfDeleteForSession;

  private final EntityUpsertionAdapter<SessionAttendanceTallyEntity> __upsertionAdapterOfSessionAttendanceTallyEntity;

  private final EntityUpsertionAdapter<SessionAttendanceRowEntity> __upsertionAdapterOfSessionAttendanceRowEntity;

  public SessionAttendanceDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__preparedStmtOfDeleteFor = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM session_attendance_tallies WHERE sessionId = ? AND courseCode = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteForSession = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM session_attendance_tallies WHERE sessionId = ?";
        return _query;
      }
    };
    this.__upsertionAdapterOfSessionAttendanceTallyEntity = new EntityUpsertionAdapter<SessionAttendanceTallyEntity>(new EntityInsertionAdapter<SessionAttendanceTallyEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `session_attendance_tallies` (`id`,`sessionId`,`courseCode`,`rollNumber`,`present`,`absent`,`leave`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SessionAttendanceTallyEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getSessionId());
        statement.bindString(3, entity.getCourseCode());
        statement.bindString(4, entity.getRollNumber());
        statement.bindLong(5, entity.getPresent());
        statement.bindLong(6, entity.getAbsent());
        statement.bindLong(7, entity.getLeave());
      }
    }, new EntityDeletionOrUpdateAdapter<SessionAttendanceTallyEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `session_attendance_tallies` SET `id` = ?,`sessionId` = ?,`courseCode` = ?,`rollNumber` = ?,`present` = ?,`absent` = ?,`leave` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SessionAttendanceTallyEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getSessionId());
        statement.bindString(3, entity.getCourseCode());
        statement.bindString(4, entity.getRollNumber());
        statement.bindLong(5, entity.getPresent());
        statement.bindLong(6, entity.getAbsent());
        statement.bindLong(7, entity.getLeave());
        statement.bindString(8, entity.getId());
      }
    });
    this.__upsertionAdapterOfSessionAttendanceRowEntity = new EntityUpsertionAdapter<SessionAttendanceRowEntity>(new EntityInsertionAdapter<SessionAttendanceRowEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `session_attendance_rows` (`id`,`sessionId`,`semester`,`courseCode`,`date`,`rollNumber`,`status`,`teacherEmail`,`isLate`,`remark`,`lectureTopic`,`recordedAt`,`entityId`,`createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SessionAttendanceRowEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getSessionId());
        statement.bindLong(3, entity.getSemester());
        statement.bindString(4, entity.getCourseCode());
        statement.bindString(5, entity.getDate());
        statement.bindString(6, entity.getRollNumber());
        statement.bindString(7, entity.getStatus());
        statement.bindString(8, entity.getTeacherEmail());
        final int _tmp = entity.isLate() ? 1 : 0;
        statement.bindLong(9, _tmp);
        if (entity.getRemark() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getRemark());
        }
        if (entity.getLectureTopic() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getLectureTopic());
        }
        statement.bindLong(12, entity.getRecordedAt());
        statement.bindLong(13, entity.getEntityId());
        statement.bindLong(14, entity.getCreatedAt());
        if (entity.getCreatedBy() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getCreatedBy());
        }
        statement.bindLong(16, entity.getUpdatedAt());
        if (entity.getUpdatedBy() == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, entity.getUpdatedBy());
        }
        final int _tmp_1 = entity.isDeleted() ? 1 : 0;
        statement.bindLong(18, _tmp_1);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(19);
        } else {
          statement.bindLong(19, entity.getDeletedAt());
        }
        if (entity.getDeletedBy() == null) {
          statement.bindNull(20);
        } else {
          statement.bindString(20, entity.getDeletedBy());
        }
      }
    }, new EntityDeletionOrUpdateAdapter<SessionAttendanceRowEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `session_attendance_rows` SET `id` = ?,`sessionId` = ?,`semester` = ?,`courseCode` = ?,`date` = ?,`rollNumber` = ?,`status` = ?,`teacherEmail` = ?,`isLate` = ?,`remark` = ?,`lectureTopic` = ?,`recordedAt` = ?,`entityId` = ?,`createdAt` = ?,`createdBy` = ?,`updatedAt` = ?,`updatedBy` = ?,`isDeleted` = ?,`deletedAt` = ?,`deletedBy` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SessionAttendanceRowEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getSessionId());
        statement.bindLong(3, entity.getSemester());
        statement.bindString(4, entity.getCourseCode());
        statement.bindString(5, entity.getDate());
        statement.bindString(6, entity.getRollNumber());
        statement.bindString(7, entity.getStatus());
        statement.bindString(8, entity.getTeacherEmail());
        final int _tmp = entity.isLate() ? 1 : 0;
        statement.bindLong(9, _tmp);
        if (entity.getRemark() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getRemark());
        }
        if (entity.getLectureTopic() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getLectureTopic());
        }
        statement.bindLong(12, entity.getRecordedAt());
        statement.bindLong(13, entity.getEntityId());
        statement.bindLong(14, entity.getCreatedAt());
        if (entity.getCreatedBy() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getCreatedBy());
        }
        statement.bindLong(16, entity.getUpdatedAt());
        if (entity.getUpdatedBy() == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, entity.getUpdatedBy());
        }
        final int _tmp_1 = entity.isDeleted() ? 1 : 0;
        statement.bindLong(18, _tmp_1);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(19);
        } else {
          statement.bindLong(19, entity.getDeletedAt());
        }
        if (entity.getDeletedBy() == null) {
          statement.bindNull(20);
        } else {
          statement.bindString(20, entity.getDeletedBy());
        }
        statement.bindString(21, entity.getId());
      }
    });
  }

  @Override
  public Object applyRowDelta(final List<SessionAttendanceRowEntity> active,
      final List<String> tombstoneIds, final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> SessionAttendanceDao.DefaultImpls.applyRowDelta(SessionAttendanceDao_Impl.this, active, tombstoneIds, __cont), $completion);
  }

  @Override
  public Object deleteFor(final String sessionId, final String courseCode,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteFor.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, sessionId);
        _argIndex = 2;
        _stmt.bindString(_argIndex, courseCode);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteFor.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteForSession(final String sessionId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteForSession.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, sessionId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteForSession.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertAll(final List<SessionAttendanceTallyEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfSessionAttendanceTallyEntity.upsert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertRows(final List<SessionAttendanceRowEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfSessionAttendanceRowEntity.upsert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<SessionAttendanceTallyEntity>> observeTallies(final String sessionId,
      final String courseCode) {
    final String _sql = "SELECT * FROM session_attendance_tallies WHERE sessionId = ? AND courseCode = ? ORDER BY rollNumber";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    _argIndex = 2;
    _statement.bindString(_argIndex, courseCode);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"session_attendance_tallies"}, new Callable<List<SessionAttendanceTallyEntity>>() {
      @Override
      @NonNull
      public List<SessionAttendanceTallyEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfCourseCode = CursorUtil.getColumnIndexOrThrow(_cursor, "courseCode");
          final int _cursorIndexOfRollNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNumber");
          final int _cursorIndexOfPresent = CursorUtil.getColumnIndexOrThrow(_cursor, "present");
          final int _cursorIndexOfAbsent = CursorUtil.getColumnIndexOrThrow(_cursor, "absent");
          final int _cursorIndexOfLeave = CursorUtil.getColumnIndexOrThrow(_cursor, "leave");
          final List<SessionAttendanceTallyEntity> _result = new ArrayList<SessionAttendanceTallyEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SessionAttendanceTallyEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpCourseCode;
            _tmpCourseCode = _cursor.getString(_cursorIndexOfCourseCode);
            final String _tmpRollNumber;
            _tmpRollNumber = _cursor.getString(_cursorIndexOfRollNumber);
            final int _tmpPresent;
            _tmpPresent = _cursor.getInt(_cursorIndexOfPresent);
            final int _tmpAbsent;
            _tmpAbsent = _cursor.getInt(_cursorIndexOfAbsent);
            final int _tmpLeave;
            _tmpLeave = _cursor.getInt(_cursorIndexOfLeave);
            _item = new SessionAttendanceTallyEntity(_tmpId,_tmpSessionId,_tmpCourseCode,_tmpRollNumber,_tmpPresent,_tmpAbsent,_tmpLeave);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<SessionAttendanceTallyEntity>> observeForSession(final String sessionId) {
    final String _sql = "SELECT * FROM session_attendance_tallies WHERE sessionId = ? ORDER BY courseCode, rollNumber";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"session_attendance_tallies"}, new Callable<List<SessionAttendanceTallyEntity>>() {
      @Override
      @NonNull
      public List<SessionAttendanceTallyEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfCourseCode = CursorUtil.getColumnIndexOrThrow(_cursor, "courseCode");
          final int _cursorIndexOfRollNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNumber");
          final int _cursorIndexOfPresent = CursorUtil.getColumnIndexOrThrow(_cursor, "present");
          final int _cursorIndexOfAbsent = CursorUtil.getColumnIndexOrThrow(_cursor, "absent");
          final int _cursorIndexOfLeave = CursorUtil.getColumnIndexOrThrow(_cursor, "leave");
          final List<SessionAttendanceTallyEntity> _result = new ArrayList<SessionAttendanceTallyEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SessionAttendanceTallyEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpCourseCode;
            _tmpCourseCode = _cursor.getString(_cursorIndexOfCourseCode);
            final String _tmpRollNumber;
            _tmpRollNumber = _cursor.getString(_cursorIndexOfRollNumber);
            final int _tmpPresent;
            _tmpPresent = _cursor.getInt(_cursorIndexOfPresent);
            final int _tmpAbsent;
            _tmpAbsent = _cursor.getInt(_cursorIndexOfAbsent);
            final int _tmpLeave;
            _tmpLeave = _cursor.getInt(_cursorIndexOfLeave);
            _item = new SessionAttendanceTallyEntity(_tmpId,_tmpSessionId,_tmpCourseCode,_tmpRollNumber,_tmpPresent,_tmpAbsent,_tmpLeave);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<SessionAttendanceTallyEntity>> observeForStudent(final String sessionId,
      final String rollNumber) {
    final String _sql = "SELECT * FROM session_attendance_tallies WHERE sessionId = ? AND rollNumber = ? ORDER BY courseCode";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    _argIndex = 2;
    _statement.bindString(_argIndex, rollNumber);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"session_attendance_tallies"}, new Callable<List<SessionAttendanceTallyEntity>>() {
      @Override
      @NonNull
      public List<SessionAttendanceTallyEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfCourseCode = CursorUtil.getColumnIndexOrThrow(_cursor, "courseCode");
          final int _cursorIndexOfRollNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNumber");
          final int _cursorIndexOfPresent = CursorUtil.getColumnIndexOrThrow(_cursor, "present");
          final int _cursorIndexOfAbsent = CursorUtil.getColumnIndexOrThrow(_cursor, "absent");
          final int _cursorIndexOfLeave = CursorUtil.getColumnIndexOrThrow(_cursor, "leave");
          final List<SessionAttendanceTallyEntity> _result = new ArrayList<SessionAttendanceTallyEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SessionAttendanceTallyEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpCourseCode;
            _tmpCourseCode = _cursor.getString(_cursorIndexOfCourseCode);
            final String _tmpRollNumber;
            _tmpRollNumber = _cursor.getString(_cursorIndexOfRollNumber);
            final int _tmpPresent;
            _tmpPresent = _cursor.getInt(_cursorIndexOfPresent);
            final int _tmpAbsent;
            _tmpAbsent = _cursor.getInt(_cursorIndexOfAbsent);
            final int _tmpLeave;
            _tmpLeave = _cursor.getInt(_cursorIndexOfLeave);
            _item = new SessionAttendanceTallyEntity(_tmpId,_tmpSessionId,_tmpCourseCode,_tmpRollNumber,_tmpPresent,_tmpAbsent,_tmpLeave);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<SessionAttendanceRowEntity>> observeRows(final String sessionId,
      final String courseCode) {
    final String _sql = "SELECT * FROM session_attendance_rows WHERE sessionId = ? AND courseCode = ? ORDER BY date, rollNumber";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    _argIndex = 2;
    _statement.bindString(_argIndex, courseCode);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"session_attendance_rows"}, new Callable<List<SessionAttendanceRowEntity>>() {
      @Override
      @NonNull
      public List<SessionAttendanceRowEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfSemester = CursorUtil.getColumnIndexOrThrow(_cursor, "semester");
          final int _cursorIndexOfCourseCode = CursorUtil.getColumnIndexOrThrow(_cursor, "courseCode");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfRollNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNumber");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfTeacherEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "teacherEmail");
          final int _cursorIndexOfIsLate = CursorUtil.getColumnIndexOrThrow(_cursor, "isLate");
          final int _cursorIndexOfRemark = CursorUtil.getColumnIndexOrThrow(_cursor, "remark");
          final int _cursorIndexOfLectureTopic = CursorUtil.getColumnIndexOrThrow(_cursor, "lectureTopic");
          final int _cursorIndexOfRecordedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "recordedAt");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final List<SessionAttendanceRowEntity> _result = new ArrayList<SessionAttendanceRowEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SessionAttendanceRowEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final int _tmpSemester;
            _tmpSemester = _cursor.getInt(_cursorIndexOfSemester);
            final String _tmpCourseCode;
            _tmpCourseCode = _cursor.getString(_cursorIndexOfCourseCode);
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final String _tmpRollNumber;
            _tmpRollNumber = _cursor.getString(_cursorIndexOfRollNumber);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpTeacherEmail;
            _tmpTeacherEmail = _cursor.getString(_cursorIndexOfTeacherEmail);
            final boolean _tmpIsLate;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsLate);
            _tmpIsLate = _tmp != 0;
            final String _tmpRemark;
            if (_cursor.isNull(_cursorIndexOfRemark)) {
              _tmpRemark = null;
            } else {
              _tmpRemark = _cursor.getString(_cursorIndexOfRemark);
            }
            final String _tmpLectureTopic;
            if (_cursor.isNull(_cursorIndexOfLectureTopic)) {
              _tmpLectureTopic = null;
            } else {
              _tmpLectureTopic = _cursor.getString(_cursorIndexOfLectureTopic);
            }
            final long _tmpRecordedAt;
            _tmpRecordedAt = _cursor.getLong(_cursorIndexOfRecordedAt);
            final long _tmpEntityId;
            _tmpEntityId = _cursor.getLong(_cursorIndexOfEntityId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpCreatedBy;
            if (_cursor.isNull(_cursorIndexOfCreatedBy)) {
              _tmpCreatedBy = null;
            } else {
              _tmpCreatedBy = _cursor.getString(_cursorIndexOfCreatedBy);
            }
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final String _tmpUpdatedBy;
            if (_cursor.isNull(_cursorIndexOfUpdatedBy)) {
              _tmpUpdatedBy = null;
            } else {
              _tmpUpdatedBy = _cursor.getString(_cursorIndexOfUpdatedBy);
            }
            final boolean _tmpIsDeleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_1 != 0;
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            final String _tmpDeletedBy;
            if (_cursor.isNull(_cursorIndexOfDeletedBy)) {
              _tmpDeletedBy = null;
            } else {
              _tmpDeletedBy = _cursor.getString(_cursorIndexOfDeletedBy);
            }
            _item = new SessionAttendanceRowEntity(_tmpId,_tmpSessionId,_tmpSemester,_tmpCourseCode,_tmpDate,_tmpRollNumber,_tmpStatus,_tmpTeacherEmail,_tmpIsLate,_tmpRemark,_tmpLectureTopic,_tmpRecordedAt,_tmpEntityId,_tmpCreatedAt,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<SessionAttendanceRowEntity>> observeRowsForSession(final String sessionId) {
    final String _sql = "SELECT * FROM session_attendance_rows WHERE sessionId = ? ORDER BY courseCode, rollNumber, date";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"session_attendance_rows"}, new Callable<List<SessionAttendanceRowEntity>>() {
      @Override
      @NonNull
      public List<SessionAttendanceRowEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfSemester = CursorUtil.getColumnIndexOrThrow(_cursor, "semester");
          final int _cursorIndexOfCourseCode = CursorUtil.getColumnIndexOrThrow(_cursor, "courseCode");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfRollNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNumber");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfTeacherEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "teacherEmail");
          final int _cursorIndexOfIsLate = CursorUtil.getColumnIndexOrThrow(_cursor, "isLate");
          final int _cursorIndexOfRemark = CursorUtil.getColumnIndexOrThrow(_cursor, "remark");
          final int _cursorIndexOfLectureTopic = CursorUtil.getColumnIndexOrThrow(_cursor, "lectureTopic");
          final int _cursorIndexOfRecordedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "recordedAt");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final List<SessionAttendanceRowEntity> _result = new ArrayList<SessionAttendanceRowEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SessionAttendanceRowEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final int _tmpSemester;
            _tmpSemester = _cursor.getInt(_cursorIndexOfSemester);
            final String _tmpCourseCode;
            _tmpCourseCode = _cursor.getString(_cursorIndexOfCourseCode);
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final String _tmpRollNumber;
            _tmpRollNumber = _cursor.getString(_cursorIndexOfRollNumber);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpTeacherEmail;
            _tmpTeacherEmail = _cursor.getString(_cursorIndexOfTeacherEmail);
            final boolean _tmpIsLate;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsLate);
            _tmpIsLate = _tmp != 0;
            final String _tmpRemark;
            if (_cursor.isNull(_cursorIndexOfRemark)) {
              _tmpRemark = null;
            } else {
              _tmpRemark = _cursor.getString(_cursorIndexOfRemark);
            }
            final String _tmpLectureTopic;
            if (_cursor.isNull(_cursorIndexOfLectureTopic)) {
              _tmpLectureTopic = null;
            } else {
              _tmpLectureTopic = _cursor.getString(_cursorIndexOfLectureTopic);
            }
            final long _tmpRecordedAt;
            _tmpRecordedAt = _cursor.getLong(_cursorIndexOfRecordedAt);
            final long _tmpEntityId;
            _tmpEntityId = _cursor.getLong(_cursorIndexOfEntityId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpCreatedBy;
            if (_cursor.isNull(_cursorIndexOfCreatedBy)) {
              _tmpCreatedBy = null;
            } else {
              _tmpCreatedBy = _cursor.getString(_cursorIndexOfCreatedBy);
            }
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final String _tmpUpdatedBy;
            if (_cursor.isNull(_cursorIndexOfUpdatedBy)) {
              _tmpUpdatedBy = null;
            } else {
              _tmpUpdatedBy = _cursor.getString(_cursorIndexOfUpdatedBy);
            }
            final boolean _tmpIsDeleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_1 != 0;
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            final String _tmpDeletedBy;
            if (_cursor.isNull(_cursorIndexOfDeletedBy)) {
              _tmpDeletedBy = null;
            } else {
              _tmpDeletedBy = _cursor.getString(_cursorIndexOfDeletedBy);
            }
            _item = new SessionAttendanceRowEntity(_tmpId,_tmpSessionId,_tmpSemester,_tmpCourseCode,_tmpDate,_tmpRollNumber,_tmpStatus,_tmpTeacherEmail,_tmpIsLate,_tmpRemark,_tmpLectureTopic,_tmpRecordedAt,_tmpEntityId,_tmpCreatedAt,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<SessionAttendanceRowEntity>> observeRowsForStudent(final String sessionId,
      final String rollNumber) {
    final String _sql = "SELECT * FROM session_attendance_rows WHERE sessionId = ? AND rollNumber = ? ORDER BY courseCode, date";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    _argIndex = 2;
    _statement.bindString(_argIndex, rollNumber);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"session_attendance_rows"}, new Callable<List<SessionAttendanceRowEntity>>() {
      @Override
      @NonNull
      public List<SessionAttendanceRowEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfSemester = CursorUtil.getColumnIndexOrThrow(_cursor, "semester");
          final int _cursorIndexOfCourseCode = CursorUtil.getColumnIndexOrThrow(_cursor, "courseCode");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfRollNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNumber");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfTeacherEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "teacherEmail");
          final int _cursorIndexOfIsLate = CursorUtil.getColumnIndexOrThrow(_cursor, "isLate");
          final int _cursorIndexOfRemark = CursorUtil.getColumnIndexOrThrow(_cursor, "remark");
          final int _cursorIndexOfLectureTopic = CursorUtil.getColumnIndexOrThrow(_cursor, "lectureTopic");
          final int _cursorIndexOfRecordedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "recordedAt");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final List<SessionAttendanceRowEntity> _result = new ArrayList<SessionAttendanceRowEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SessionAttendanceRowEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final int _tmpSemester;
            _tmpSemester = _cursor.getInt(_cursorIndexOfSemester);
            final String _tmpCourseCode;
            _tmpCourseCode = _cursor.getString(_cursorIndexOfCourseCode);
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final String _tmpRollNumber;
            _tmpRollNumber = _cursor.getString(_cursorIndexOfRollNumber);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpTeacherEmail;
            _tmpTeacherEmail = _cursor.getString(_cursorIndexOfTeacherEmail);
            final boolean _tmpIsLate;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsLate);
            _tmpIsLate = _tmp != 0;
            final String _tmpRemark;
            if (_cursor.isNull(_cursorIndexOfRemark)) {
              _tmpRemark = null;
            } else {
              _tmpRemark = _cursor.getString(_cursorIndexOfRemark);
            }
            final String _tmpLectureTopic;
            if (_cursor.isNull(_cursorIndexOfLectureTopic)) {
              _tmpLectureTopic = null;
            } else {
              _tmpLectureTopic = _cursor.getString(_cursorIndexOfLectureTopic);
            }
            final long _tmpRecordedAt;
            _tmpRecordedAt = _cursor.getLong(_cursorIndexOfRecordedAt);
            final long _tmpEntityId;
            _tmpEntityId = _cursor.getLong(_cursorIndexOfEntityId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpCreatedBy;
            if (_cursor.isNull(_cursorIndexOfCreatedBy)) {
              _tmpCreatedBy = null;
            } else {
              _tmpCreatedBy = _cursor.getString(_cursorIndexOfCreatedBy);
            }
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final String _tmpUpdatedBy;
            if (_cursor.isNull(_cursorIndexOfUpdatedBy)) {
              _tmpUpdatedBy = null;
            } else {
              _tmpUpdatedBy = _cursor.getString(_cursorIndexOfUpdatedBy);
            }
            final boolean _tmpIsDeleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_1 != 0;
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            final String _tmpDeletedBy;
            if (_cursor.isNull(_cursorIndexOfDeletedBy)) {
              _tmpDeletedBy = null;
            } else {
              _tmpDeletedBy = _cursor.getString(_cursorIndexOfDeletedBy);
            }
            _item = new SessionAttendanceRowEntity(_tmpId,_tmpSessionId,_tmpSemester,_tmpCourseCode,_tmpDate,_tmpRollNumber,_tmpStatus,_tmpTeacherEmail,_tmpIsLate,_tmpRemark,_tmpLectureTopic,_tmpRecordedAt,_tmpEntityId,_tmpCreatedAt,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getMarkedOn(final String sessionId, final String courseCode, final String date,
      final Continuation<? super SessionAttendanceRowEntity> $completion) {
    final String _sql = "SELECT * FROM session_attendance_rows WHERE sessionId = ? AND courseCode = ? AND date = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    _argIndex = 2;
    _statement.bindString(_argIndex, courseCode);
    _argIndex = 3;
    _statement.bindString(_argIndex, date);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SessionAttendanceRowEntity>() {
      @Override
      @Nullable
      public SessionAttendanceRowEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfSemester = CursorUtil.getColumnIndexOrThrow(_cursor, "semester");
          final int _cursorIndexOfCourseCode = CursorUtil.getColumnIndexOrThrow(_cursor, "courseCode");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfRollNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNumber");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfTeacherEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "teacherEmail");
          final int _cursorIndexOfIsLate = CursorUtil.getColumnIndexOrThrow(_cursor, "isLate");
          final int _cursorIndexOfRemark = CursorUtil.getColumnIndexOrThrow(_cursor, "remark");
          final int _cursorIndexOfLectureTopic = CursorUtil.getColumnIndexOrThrow(_cursor, "lectureTopic");
          final int _cursorIndexOfRecordedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "recordedAt");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final SessionAttendanceRowEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final int _tmpSemester;
            _tmpSemester = _cursor.getInt(_cursorIndexOfSemester);
            final String _tmpCourseCode;
            _tmpCourseCode = _cursor.getString(_cursorIndexOfCourseCode);
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final String _tmpRollNumber;
            _tmpRollNumber = _cursor.getString(_cursorIndexOfRollNumber);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpTeacherEmail;
            _tmpTeacherEmail = _cursor.getString(_cursorIndexOfTeacherEmail);
            final boolean _tmpIsLate;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsLate);
            _tmpIsLate = _tmp != 0;
            final String _tmpRemark;
            if (_cursor.isNull(_cursorIndexOfRemark)) {
              _tmpRemark = null;
            } else {
              _tmpRemark = _cursor.getString(_cursorIndexOfRemark);
            }
            final String _tmpLectureTopic;
            if (_cursor.isNull(_cursorIndexOfLectureTopic)) {
              _tmpLectureTopic = null;
            } else {
              _tmpLectureTopic = _cursor.getString(_cursorIndexOfLectureTopic);
            }
            final long _tmpRecordedAt;
            _tmpRecordedAt = _cursor.getLong(_cursorIndexOfRecordedAt);
            final long _tmpEntityId;
            _tmpEntityId = _cursor.getLong(_cursorIndexOfEntityId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpCreatedBy;
            if (_cursor.isNull(_cursorIndexOfCreatedBy)) {
              _tmpCreatedBy = null;
            } else {
              _tmpCreatedBy = _cursor.getString(_cursorIndexOfCreatedBy);
            }
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final String _tmpUpdatedBy;
            if (_cursor.isNull(_cursorIndexOfUpdatedBy)) {
              _tmpUpdatedBy = null;
            } else {
              _tmpUpdatedBy = _cursor.getString(_cursorIndexOfUpdatedBy);
            }
            final boolean _tmpIsDeleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_1 != 0;
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            final String _tmpDeletedBy;
            if (_cursor.isNull(_cursorIndexOfDeletedBy)) {
              _tmpDeletedBy = null;
            } else {
              _tmpDeletedBy = _cursor.getString(_cursorIndexOfDeletedBy);
            }
            _result = new SessionAttendanceRowEntity(_tmpId,_tmpSessionId,_tmpSemester,_tmpCourseCode,_tmpDate,_tmpRollNumber,_tmpStatus,_tmpTeacherEmail,_tmpIsLate,_tmpRemark,_tmpLectureTopic,_tmpRecordedAt,_tmpEntityId,_tmpCreatedAt,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getRowsBetween(final String sessionId, final String courseCode, final String from,
      final String to, final Continuation<? super List<SessionAttendanceRowEntity>> $completion) {
    final String _sql = "SELECT * FROM session_attendance_rows WHERE sessionId = ? AND courseCode = ? AND date BETWEEN ? AND ? ORDER BY date, rollNumber";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 4);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    _argIndex = 2;
    _statement.bindString(_argIndex, courseCode);
    _argIndex = 3;
    _statement.bindString(_argIndex, from);
    _argIndex = 4;
    _statement.bindString(_argIndex, to);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SessionAttendanceRowEntity>>() {
      @Override
      @NonNull
      public List<SessionAttendanceRowEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfSemester = CursorUtil.getColumnIndexOrThrow(_cursor, "semester");
          final int _cursorIndexOfCourseCode = CursorUtil.getColumnIndexOrThrow(_cursor, "courseCode");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfRollNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNumber");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfTeacherEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "teacherEmail");
          final int _cursorIndexOfIsLate = CursorUtil.getColumnIndexOrThrow(_cursor, "isLate");
          final int _cursorIndexOfRemark = CursorUtil.getColumnIndexOrThrow(_cursor, "remark");
          final int _cursorIndexOfLectureTopic = CursorUtil.getColumnIndexOrThrow(_cursor, "lectureTopic");
          final int _cursorIndexOfRecordedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "recordedAt");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final List<SessionAttendanceRowEntity> _result = new ArrayList<SessionAttendanceRowEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SessionAttendanceRowEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final int _tmpSemester;
            _tmpSemester = _cursor.getInt(_cursorIndexOfSemester);
            final String _tmpCourseCode;
            _tmpCourseCode = _cursor.getString(_cursorIndexOfCourseCode);
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final String _tmpRollNumber;
            _tmpRollNumber = _cursor.getString(_cursorIndexOfRollNumber);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpTeacherEmail;
            _tmpTeacherEmail = _cursor.getString(_cursorIndexOfTeacherEmail);
            final boolean _tmpIsLate;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsLate);
            _tmpIsLate = _tmp != 0;
            final String _tmpRemark;
            if (_cursor.isNull(_cursorIndexOfRemark)) {
              _tmpRemark = null;
            } else {
              _tmpRemark = _cursor.getString(_cursorIndexOfRemark);
            }
            final String _tmpLectureTopic;
            if (_cursor.isNull(_cursorIndexOfLectureTopic)) {
              _tmpLectureTopic = null;
            } else {
              _tmpLectureTopic = _cursor.getString(_cursorIndexOfLectureTopic);
            }
            final long _tmpRecordedAt;
            _tmpRecordedAt = _cursor.getLong(_cursorIndexOfRecordedAt);
            final long _tmpEntityId;
            _tmpEntityId = _cursor.getLong(_cursorIndexOfEntityId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpCreatedBy;
            if (_cursor.isNull(_cursorIndexOfCreatedBy)) {
              _tmpCreatedBy = null;
            } else {
              _tmpCreatedBy = _cursor.getString(_cursorIndexOfCreatedBy);
            }
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final String _tmpUpdatedBy;
            if (_cursor.isNull(_cursorIndexOfUpdatedBy)) {
              _tmpUpdatedBy = null;
            } else {
              _tmpUpdatedBy = _cursor.getString(_cursorIndexOfUpdatedBy);
            }
            final boolean _tmpIsDeleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_1 != 0;
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            final String _tmpDeletedBy;
            if (_cursor.isNull(_cursorIndexOfDeletedBy)) {
              _tmpDeletedBy = null;
            } else {
              _tmpDeletedBy = _cursor.getString(_cursorIndexOfDeletedBy);
            }
            _item = new SessionAttendanceRowEntity(_tmpId,_tmpSessionId,_tmpSemester,_tmpCourseCode,_tmpDate,_tmpRollNumber,_tmpStatus,_tmpTeacherEmail,_tmpIsLate,_tmpRemark,_tmpLectureTopic,_tmpRecordedAt,_tmpEntityId,_tmpCreatedAt,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getRowsForSemester(final String sessionId, final int semester,
      final Continuation<? super List<SessionAttendanceRowEntity>> $completion) {
    final String _sql = "SELECT * FROM session_attendance_rows WHERE sessionId = ? AND semester = ? ORDER BY courseCode, date, rollNumber";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, semester);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SessionAttendanceRowEntity>>() {
      @Override
      @NonNull
      public List<SessionAttendanceRowEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfSemester = CursorUtil.getColumnIndexOrThrow(_cursor, "semester");
          final int _cursorIndexOfCourseCode = CursorUtil.getColumnIndexOrThrow(_cursor, "courseCode");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfRollNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNumber");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfTeacherEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "teacherEmail");
          final int _cursorIndexOfIsLate = CursorUtil.getColumnIndexOrThrow(_cursor, "isLate");
          final int _cursorIndexOfRemark = CursorUtil.getColumnIndexOrThrow(_cursor, "remark");
          final int _cursorIndexOfLectureTopic = CursorUtil.getColumnIndexOrThrow(_cursor, "lectureTopic");
          final int _cursorIndexOfRecordedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "recordedAt");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final List<SessionAttendanceRowEntity> _result = new ArrayList<SessionAttendanceRowEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SessionAttendanceRowEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final int _tmpSemester;
            _tmpSemester = _cursor.getInt(_cursorIndexOfSemester);
            final String _tmpCourseCode;
            _tmpCourseCode = _cursor.getString(_cursorIndexOfCourseCode);
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final String _tmpRollNumber;
            _tmpRollNumber = _cursor.getString(_cursorIndexOfRollNumber);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpTeacherEmail;
            _tmpTeacherEmail = _cursor.getString(_cursorIndexOfTeacherEmail);
            final boolean _tmpIsLate;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsLate);
            _tmpIsLate = _tmp != 0;
            final String _tmpRemark;
            if (_cursor.isNull(_cursorIndexOfRemark)) {
              _tmpRemark = null;
            } else {
              _tmpRemark = _cursor.getString(_cursorIndexOfRemark);
            }
            final String _tmpLectureTopic;
            if (_cursor.isNull(_cursorIndexOfLectureTopic)) {
              _tmpLectureTopic = null;
            } else {
              _tmpLectureTopic = _cursor.getString(_cursorIndexOfLectureTopic);
            }
            final long _tmpRecordedAt;
            _tmpRecordedAt = _cursor.getLong(_cursorIndexOfRecordedAt);
            final long _tmpEntityId;
            _tmpEntityId = _cursor.getLong(_cursorIndexOfEntityId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpCreatedBy;
            if (_cursor.isNull(_cursorIndexOfCreatedBy)) {
              _tmpCreatedBy = null;
            } else {
              _tmpCreatedBy = _cursor.getString(_cursorIndexOfCreatedBy);
            }
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final String _tmpUpdatedBy;
            if (_cursor.isNull(_cursorIndexOfUpdatedBy)) {
              _tmpUpdatedBy = null;
            } else {
              _tmpUpdatedBy = _cursor.getString(_cursorIndexOfUpdatedBy);
            }
            final boolean _tmpIsDeleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_1 != 0;
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            final String _tmpDeletedBy;
            if (_cursor.isNull(_cursorIndexOfDeletedBy)) {
              _tmpDeletedBy = null;
            } else {
              _tmpDeletedBy = _cursor.getString(_cursorIndexOfDeletedBy);
            }
            _item = new SessionAttendanceRowEntity(_tmpId,_tmpSessionId,_tmpSemester,_tmpCourseCode,_tmpDate,_tmpRollNumber,_tmpStatus,_tmpTeacherEmail,_tmpIsLate,_tmpRemark,_tmpLectureTopic,_tmpRecordedAt,_tmpEntityId,_tmpCreatedAt,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteRowsByIds(final List<String> ids,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("DELETE FROM session_attendance_rows WHERE id IN (");
        final int _inputSize = ids.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        for (String _item : ids) {
          _stmt.bindString(_argIndex, _item);
          _argIndex++;
        }
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
