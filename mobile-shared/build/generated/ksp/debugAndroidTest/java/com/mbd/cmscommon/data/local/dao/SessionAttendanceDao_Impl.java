package com.mbd.cmscommon.data.local.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.EntityUpsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.mbd.cmscommon.data.local.entity.SessionAttendanceTallyEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
