package com.mbd.cmscommon.data.local.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
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
import com.mbd.cmscommon.data.local.entity.SessionPeriodEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
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
public final class SessionPeriodDao_Impl implements SessionPeriodDao {
  private final RoomDatabase __db;

  private final SharedSQLiteStatement __preparedStmtOfDeleteForSessionDay;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteForSession;

  private final EntityUpsertionAdapter<SessionPeriodEntity> __upsertionAdapterOfSessionPeriodEntity;

  public SessionPeriodDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__preparedStmtOfDeleteForSessionDay = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM session_periods WHERE sessionId = ? AND day = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM session_periods WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteForSession = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM session_periods WHERE sessionId = ?";
        return _query;
      }
    };
    this.__upsertionAdapterOfSessionPeriodEntity = new EntityUpsertionAdapter<SessionPeriodEntity>(new EntityInsertionAdapter<SessionPeriodEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `session_periods` (`id`,`sessionId`,`deptId`,`day`,`startTime`,`endTime`,`courseCode`,`subjectName`,`teacherId`,`teacherName`,`periodType`,`creditHours`,`roomNo`,`building`,`notes`,`effectiveFrom`,`effectiveTo`,`entityId`,`createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SessionPeriodEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getSessionId());
        statement.bindString(3, entity.getDeptId());
        statement.bindString(4, entity.getDay());
        statement.bindString(5, entity.getStartTime());
        statement.bindString(6, entity.getEndTime());
        statement.bindString(7, entity.getCourseCode());
        statement.bindString(8, entity.getSubjectName());
        statement.bindString(9, entity.getTeacherId());
        statement.bindString(10, entity.getTeacherName());
        statement.bindString(11, entity.getPeriodType());
        if (entity.getCreditHours() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getCreditHours());
        }
        if (entity.getRoomNo() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getRoomNo());
        }
        if (entity.getBuilding() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getBuilding());
        }
        if (entity.getNotes() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getNotes());
        }
        if (entity.getEffectiveFrom() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getEffectiveFrom());
        }
        if (entity.getEffectiveTo() == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, entity.getEffectiveTo());
        }
        statement.bindLong(18, entity.getEntityId());
        statement.bindLong(19, entity.getCreatedAt());
        if (entity.getCreatedBy() == null) {
          statement.bindNull(20);
        } else {
          statement.bindString(20, entity.getCreatedBy());
        }
        statement.bindLong(21, entity.getUpdatedAt());
        if (entity.getUpdatedBy() == null) {
          statement.bindNull(22);
        } else {
          statement.bindString(22, entity.getUpdatedBy());
        }
        final int _tmp = entity.isDeleted() ? 1 : 0;
        statement.bindLong(23, _tmp);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(24);
        } else {
          statement.bindLong(24, entity.getDeletedAt());
        }
        if (entity.getDeletedBy() == null) {
          statement.bindNull(25);
        } else {
          statement.bindString(25, entity.getDeletedBy());
        }
      }
    }, new EntityDeletionOrUpdateAdapter<SessionPeriodEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `session_periods` SET `id` = ?,`sessionId` = ?,`deptId` = ?,`day` = ?,`startTime` = ?,`endTime` = ?,`courseCode` = ?,`subjectName` = ?,`teacherId` = ?,`teacherName` = ?,`periodType` = ?,`creditHours` = ?,`roomNo` = ?,`building` = ?,`notes` = ?,`effectiveFrom` = ?,`effectiveTo` = ?,`entityId` = ?,`createdAt` = ?,`createdBy` = ?,`updatedAt` = ?,`updatedBy` = ?,`isDeleted` = ?,`deletedAt` = ?,`deletedBy` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SessionPeriodEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getSessionId());
        statement.bindString(3, entity.getDeptId());
        statement.bindString(4, entity.getDay());
        statement.bindString(5, entity.getStartTime());
        statement.bindString(6, entity.getEndTime());
        statement.bindString(7, entity.getCourseCode());
        statement.bindString(8, entity.getSubjectName());
        statement.bindString(9, entity.getTeacherId());
        statement.bindString(10, entity.getTeacherName());
        statement.bindString(11, entity.getPeriodType());
        if (entity.getCreditHours() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getCreditHours());
        }
        if (entity.getRoomNo() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getRoomNo());
        }
        if (entity.getBuilding() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getBuilding());
        }
        if (entity.getNotes() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getNotes());
        }
        if (entity.getEffectiveFrom() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getEffectiveFrom());
        }
        if (entity.getEffectiveTo() == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, entity.getEffectiveTo());
        }
        statement.bindLong(18, entity.getEntityId());
        statement.bindLong(19, entity.getCreatedAt());
        if (entity.getCreatedBy() == null) {
          statement.bindNull(20);
        } else {
          statement.bindString(20, entity.getCreatedBy());
        }
        statement.bindLong(21, entity.getUpdatedAt());
        if (entity.getUpdatedBy() == null) {
          statement.bindNull(22);
        } else {
          statement.bindString(22, entity.getUpdatedBy());
        }
        final int _tmp = entity.isDeleted() ? 1 : 0;
        statement.bindLong(23, _tmp);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(24);
        } else {
          statement.bindLong(24, entity.getDeletedAt());
        }
        if (entity.getDeletedBy() == null) {
          statement.bindNull(25);
        } else {
          statement.bindString(25, entity.getDeletedBy());
        }
        statement.bindString(26, entity.getId());
      }
    });
  }

  @Override
  public Object applyDelta(final List<SessionPeriodEntity> active, final List<String> tombstoneIds,
      final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> SessionPeriodDao.DefaultImpls.applyDelta(SessionPeriodDao_Impl.this, active, tombstoneIds, __cont), $completion);
  }

  @Override
  public Object deleteForSessionDay(final String sessionId, final String day,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteForSessionDay.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, sessionId);
        _argIndex = 2;
        _stmt.bindString(_argIndex, day);
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
          __preparedStmtOfDeleteForSessionDay.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteById(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfDeleteById.release(_stmt);
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
  public Object upsertAll(final List<SessionPeriodEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfSessionPeriodEntity.upsert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<SessionPeriodEntity>> observeForSessionDay(final String sessionId,
      final String day) {
    final String _sql = "SELECT * FROM session_periods WHERE sessionId = ? AND day = ? ORDER BY startTime";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    _argIndex = 2;
    _statement.bindString(_argIndex, day);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"session_periods"}, new Callable<List<SessionPeriodEntity>>() {
      @Override
      @NonNull
      public List<SessionPeriodEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfDeptId = CursorUtil.getColumnIndexOrThrow(_cursor, "deptId");
          final int _cursorIndexOfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "day");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfCourseCode = CursorUtil.getColumnIndexOrThrow(_cursor, "courseCode");
          final int _cursorIndexOfSubjectName = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectName");
          final int _cursorIndexOfTeacherId = CursorUtil.getColumnIndexOrThrow(_cursor, "teacherId");
          final int _cursorIndexOfTeacherName = CursorUtil.getColumnIndexOrThrow(_cursor, "teacherName");
          final int _cursorIndexOfPeriodType = CursorUtil.getColumnIndexOrThrow(_cursor, "periodType");
          final int _cursorIndexOfCreditHours = CursorUtil.getColumnIndexOrThrow(_cursor, "creditHours");
          final int _cursorIndexOfRoomNo = CursorUtil.getColumnIndexOrThrow(_cursor, "roomNo");
          final int _cursorIndexOfBuilding = CursorUtil.getColumnIndexOrThrow(_cursor, "building");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfEffectiveFrom = CursorUtil.getColumnIndexOrThrow(_cursor, "effectiveFrom");
          final int _cursorIndexOfEffectiveTo = CursorUtil.getColumnIndexOrThrow(_cursor, "effectiveTo");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final List<SessionPeriodEntity> _result = new ArrayList<SessionPeriodEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SessionPeriodEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpDeptId;
            _tmpDeptId = _cursor.getString(_cursorIndexOfDeptId);
            final String _tmpDay;
            _tmpDay = _cursor.getString(_cursorIndexOfDay);
            final String _tmpStartTime;
            _tmpStartTime = _cursor.getString(_cursorIndexOfStartTime);
            final String _tmpEndTime;
            _tmpEndTime = _cursor.getString(_cursorIndexOfEndTime);
            final String _tmpCourseCode;
            _tmpCourseCode = _cursor.getString(_cursorIndexOfCourseCode);
            final String _tmpSubjectName;
            _tmpSubjectName = _cursor.getString(_cursorIndexOfSubjectName);
            final String _tmpTeacherId;
            _tmpTeacherId = _cursor.getString(_cursorIndexOfTeacherId);
            final String _tmpTeacherName;
            _tmpTeacherName = _cursor.getString(_cursorIndexOfTeacherName);
            final String _tmpPeriodType;
            _tmpPeriodType = _cursor.getString(_cursorIndexOfPeriodType);
            final Integer _tmpCreditHours;
            if (_cursor.isNull(_cursorIndexOfCreditHours)) {
              _tmpCreditHours = null;
            } else {
              _tmpCreditHours = _cursor.getInt(_cursorIndexOfCreditHours);
            }
            final String _tmpRoomNo;
            if (_cursor.isNull(_cursorIndexOfRoomNo)) {
              _tmpRoomNo = null;
            } else {
              _tmpRoomNo = _cursor.getString(_cursorIndexOfRoomNo);
            }
            final String _tmpBuilding;
            if (_cursor.isNull(_cursorIndexOfBuilding)) {
              _tmpBuilding = null;
            } else {
              _tmpBuilding = _cursor.getString(_cursorIndexOfBuilding);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final String _tmpEffectiveFrom;
            if (_cursor.isNull(_cursorIndexOfEffectiveFrom)) {
              _tmpEffectiveFrom = null;
            } else {
              _tmpEffectiveFrom = _cursor.getString(_cursorIndexOfEffectiveFrom);
            }
            final String _tmpEffectiveTo;
            if (_cursor.isNull(_cursorIndexOfEffectiveTo)) {
              _tmpEffectiveTo = null;
            } else {
              _tmpEffectiveTo = _cursor.getString(_cursorIndexOfEffectiveTo);
            }
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
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp != 0;
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
            _item = new SessionPeriodEntity(_tmpId,_tmpSessionId,_tmpDeptId,_tmpDay,_tmpStartTime,_tmpEndTime,_tmpCourseCode,_tmpSubjectName,_tmpTeacherId,_tmpTeacherName,_tmpPeriodType,_tmpCreditHours,_tmpRoomNo,_tmpBuilding,_tmpNotes,_tmpEffectiveFrom,_tmpEffectiveTo,_tmpEntityId,_tmpCreatedAt,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
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
  public Flow<List<SessionPeriodEntity>> observeForSession(final String sessionId) {
    final String _sql = "SELECT * FROM session_periods WHERE sessionId = ? ORDER BY day, startTime";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"session_periods"}, new Callable<List<SessionPeriodEntity>>() {
      @Override
      @NonNull
      public List<SessionPeriodEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfDeptId = CursorUtil.getColumnIndexOrThrow(_cursor, "deptId");
          final int _cursorIndexOfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "day");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfCourseCode = CursorUtil.getColumnIndexOrThrow(_cursor, "courseCode");
          final int _cursorIndexOfSubjectName = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectName");
          final int _cursorIndexOfTeacherId = CursorUtil.getColumnIndexOrThrow(_cursor, "teacherId");
          final int _cursorIndexOfTeacherName = CursorUtil.getColumnIndexOrThrow(_cursor, "teacherName");
          final int _cursorIndexOfPeriodType = CursorUtil.getColumnIndexOrThrow(_cursor, "periodType");
          final int _cursorIndexOfCreditHours = CursorUtil.getColumnIndexOrThrow(_cursor, "creditHours");
          final int _cursorIndexOfRoomNo = CursorUtil.getColumnIndexOrThrow(_cursor, "roomNo");
          final int _cursorIndexOfBuilding = CursorUtil.getColumnIndexOrThrow(_cursor, "building");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfEffectiveFrom = CursorUtil.getColumnIndexOrThrow(_cursor, "effectiveFrom");
          final int _cursorIndexOfEffectiveTo = CursorUtil.getColumnIndexOrThrow(_cursor, "effectiveTo");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final List<SessionPeriodEntity> _result = new ArrayList<SessionPeriodEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SessionPeriodEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpDeptId;
            _tmpDeptId = _cursor.getString(_cursorIndexOfDeptId);
            final String _tmpDay;
            _tmpDay = _cursor.getString(_cursorIndexOfDay);
            final String _tmpStartTime;
            _tmpStartTime = _cursor.getString(_cursorIndexOfStartTime);
            final String _tmpEndTime;
            _tmpEndTime = _cursor.getString(_cursorIndexOfEndTime);
            final String _tmpCourseCode;
            _tmpCourseCode = _cursor.getString(_cursorIndexOfCourseCode);
            final String _tmpSubjectName;
            _tmpSubjectName = _cursor.getString(_cursorIndexOfSubjectName);
            final String _tmpTeacherId;
            _tmpTeacherId = _cursor.getString(_cursorIndexOfTeacherId);
            final String _tmpTeacherName;
            _tmpTeacherName = _cursor.getString(_cursorIndexOfTeacherName);
            final String _tmpPeriodType;
            _tmpPeriodType = _cursor.getString(_cursorIndexOfPeriodType);
            final Integer _tmpCreditHours;
            if (_cursor.isNull(_cursorIndexOfCreditHours)) {
              _tmpCreditHours = null;
            } else {
              _tmpCreditHours = _cursor.getInt(_cursorIndexOfCreditHours);
            }
            final String _tmpRoomNo;
            if (_cursor.isNull(_cursorIndexOfRoomNo)) {
              _tmpRoomNo = null;
            } else {
              _tmpRoomNo = _cursor.getString(_cursorIndexOfRoomNo);
            }
            final String _tmpBuilding;
            if (_cursor.isNull(_cursorIndexOfBuilding)) {
              _tmpBuilding = null;
            } else {
              _tmpBuilding = _cursor.getString(_cursorIndexOfBuilding);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final String _tmpEffectiveFrom;
            if (_cursor.isNull(_cursorIndexOfEffectiveFrom)) {
              _tmpEffectiveFrom = null;
            } else {
              _tmpEffectiveFrom = _cursor.getString(_cursorIndexOfEffectiveFrom);
            }
            final String _tmpEffectiveTo;
            if (_cursor.isNull(_cursorIndexOfEffectiveTo)) {
              _tmpEffectiveTo = null;
            } else {
              _tmpEffectiveTo = _cursor.getString(_cursorIndexOfEffectiveTo);
            }
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
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp != 0;
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
            _item = new SessionPeriodEntity(_tmpId,_tmpSessionId,_tmpDeptId,_tmpDay,_tmpStartTime,_tmpEndTime,_tmpCourseCode,_tmpSubjectName,_tmpTeacherId,_tmpTeacherName,_tmpPeriodType,_tmpCreditHours,_tmpRoomNo,_tmpBuilding,_tmpNotes,_tmpEffectiveFrom,_tmpEffectiveTo,_tmpEntityId,_tmpCreatedAt,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
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
  public Flow<List<SessionPeriodEntity>> observeForTeacher(final String teacherId) {
    final String _sql = "SELECT * FROM session_periods WHERE teacherId = ? ORDER BY day, startTime";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, teacherId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"session_periods"}, new Callable<List<SessionPeriodEntity>>() {
      @Override
      @NonNull
      public List<SessionPeriodEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfDeptId = CursorUtil.getColumnIndexOrThrow(_cursor, "deptId");
          final int _cursorIndexOfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "day");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfCourseCode = CursorUtil.getColumnIndexOrThrow(_cursor, "courseCode");
          final int _cursorIndexOfSubjectName = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectName");
          final int _cursorIndexOfTeacherId = CursorUtil.getColumnIndexOrThrow(_cursor, "teacherId");
          final int _cursorIndexOfTeacherName = CursorUtil.getColumnIndexOrThrow(_cursor, "teacherName");
          final int _cursorIndexOfPeriodType = CursorUtil.getColumnIndexOrThrow(_cursor, "periodType");
          final int _cursorIndexOfCreditHours = CursorUtil.getColumnIndexOrThrow(_cursor, "creditHours");
          final int _cursorIndexOfRoomNo = CursorUtil.getColumnIndexOrThrow(_cursor, "roomNo");
          final int _cursorIndexOfBuilding = CursorUtil.getColumnIndexOrThrow(_cursor, "building");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfEffectiveFrom = CursorUtil.getColumnIndexOrThrow(_cursor, "effectiveFrom");
          final int _cursorIndexOfEffectiveTo = CursorUtil.getColumnIndexOrThrow(_cursor, "effectiveTo");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final List<SessionPeriodEntity> _result = new ArrayList<SessionPeriodEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SessionPeriodEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpDeptId;
            _tmpDeptId = _cursor.getString(_cursorIndexOfDeptId);
            final String _tmpDay;
            _tmpDay = _cursor.getString(_cursorIndexOfDay);
            final String _tmpStartTime;
            _tmpStartTime = _cursor.getString(_cursorIndexOfStartTime);
            final String _tmpEndTime;
            _tmpEndTime = _cursor.getString(_cursorIndexOfEndTime);
            final String _tmpCourseCode;
            _tmpCourseCode = _cursor.getString(_cursorIndexOfCourseCode);
            final String _tmpSubjectName;
            _tmpSubjectName = _cursor.getString(_cursorIndexOfSubjectName);
            final String _tmpTeacherId;
            _tmpTeacherId = _cursor.getString(_cursorIndexOfTeacherId);
            final String _tmpTeacherName;
            _tmpTeacherName = _cursor.getString(_cursorIndexOfTeacherName);
            final String _tmpPeriodType;
            _tmpPeriodType = _cursor.getString(_cursorIndexOfPeriodType);
            final Integer _tmpCreditHours;
            if (_cursor.isNull(_cursorIndexOfCreditHours)) {
              _tmpCreditHours = null;
            } else {
              _tmpCreditHours = _cursor.getInt(_cursorIndexOfCreditHours);
            }
            final String _tmpRoomNo;
            if (_cursor.isNull(_cursorIndexOfRoomNo)) {
              _tmpRoomNo = null;
            } else {
              _tmpRoomNo = _cursor.getString(_cursorIndexOfRoomNo);
            }
            final String _tmpBuilding;
            if (_cursor.isNull(_cursorIndexOfBuilding)) {
              _tmpBuilding = null;
            } else {
              _tmpBuilding = _cursor.getString(_cursorIndexOfBuilding);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final String _tmpEffectiveFrom;
            if (_cursor.isNull(_cursorIndexOfEffectiveFrom)) {
              _tmpEffectiveFrom = null;
            } else {
              _tmpEffectiveFrom = _cursor.getString(_cursorIndexOfEffectiveFrom);
            }
            final String _tmpEffectiveTo;
            if (_cursor.isNull(_cursorIndexOfEffectiveTo)) {
              _tmpEffectiveTo = null;
            } else {
              _tmpEffectiveTo = _cursor.getString(_cursorIndexOfEffectiveTo);
            }
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
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp != 0;
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
            _item = new SessionPeriodEntity(_tmpId,_tmpSessionId,_tmpDeptId,_tmpDay,_tmpStartTime,_tmpEndTime,_tmpCourseCode,_tmpSubjectName,_tmpTeacherId,_tmpTeacherName,_tmpPeriodType,_tmpCreditHours,_tmpRoomNo,_tmpBuilding,_tmpNotes,_tmpEffectiveFrom,_tmpEffectiveTo,_tmpEntityId,_tmpCreatedAt,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
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
  public Flow<List<SessionPeriodEntity>> observeForDay(final String day) {
    final String _sql = "SELECT * FROM session_periods WHERE day = ? ORDER BY sessionId, startTime";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, day);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"session_periods"}, new Callable<List<SessionPeriodEntity>>() {
      @Override
      @NonNull
      public List<SessionPeriodEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfDeptId = CursorUtil.getColumnIndexOrThrow(_cursor, "deptId");
          final int _cursorIndexOfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "day");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfCourseCode = CursorUtil.getColumnIndexOrThrow(_cursor, "courseCode");
          final int _cursorIndexOfSubjectName = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectName");
          final int _cursorIndexOfTeacherId = CursorUtil.getColumnIndexOrThrow(_cursor, "teacherId");
          final int _cursorIndexOfTeacherName = CursorUtil.getColumnIndexOrThrow(_cursor, "teacherName");
          final int _cursorIndexOfPeriodType = CursorUtil.getColumnIndexOrThrow(_cursor, "periodType");
          final int _cursorIndexOfCreditHours = CursorUtil.getColumnIndexOrThrow(_cursor, "creditHours");
          final int _cursorIndexOfRoomNo = CursorUtil.getColumnIndexOrThrow(_cursor, "roomNo");
          final int _cursorIndexOfBuilding = CursorUtil.getColumnIndexOrThrow(_cursor, "building");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfEffectiveFrom = CursorUtil.getColumnIndexOrThrow(_cursor, "effectiveFrom");
          final int _cursorIndexOfEffectiveTo = CursorUtil.getColumnIndexOrThrow(_cursor, "effectiveTo");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final List<SessionPeriodEntity> _result = new ArrayList<SessionPeriodEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SessionPeriodEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpDeptId;
            _tmpDeptId = _cursor.getString(_cursorIndexOfDeptId);
            final String _tmpDay;
            _tmpDay = _cursor.getString(_cursorIndexOfDay);
            final String _tmpStartTime;
            _tmpStartTime = _cursor.getString(_cursorIndexOfStartTime);
            final String _tmpEndTime;
            _tmpEndTime = _cursor.getString(_cursorIndexOfEndTime);
            final String _tmpCourseCode;
            _tmpCourseCode = _cursor.getString(_cursorIndexOfCourseCode);
            final String _tmpSubjectName;
            _tmpSubjectName = _cursor.getString(_cursorIndexOfSubjectName);
            final String _tmpTeacherId;
            _tmpTeacherId = _cursor.getString(_cursorIndexOfTeacherId);
            final String _tmpTeacherName;
            _tmpTeacherName = _cursor.getString(_cursorIndexOfTeacherName);
            final String _tmpPeriodType;
            _tmpPeriodType = _cursor.getString(_cursorIndexOfPeriodType);
            final Integer _tmpCreditHours;
            if (_cursor.isNull(_cursorIndexOfCreditHours)) {
              _tmpCreditHours = null;
            } else {
              _tmpCreditHours = _cursor.getInt(_cursorIndexOfCreditHours);
            }
            final String _tmpRoomNo;
            if (_cursor.isNull(_cursorIndexOfRoomNo)) {
              _tmpRoomNo = null;
            } else {
              _tmpRoomNo = _cursor.getString(_cursorIndexOfRoomNo);
            }
            final String _tmpBuilding;
            if (_cursor.isNull(_cursorIndexOfBuilding)) {
              _tmpBuilding = null;
            } else {
              _tmpBuilding = _cursor.getString(_cursorIndexOfBuilding);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final String _tmpEffectiveFrom;
            if (_cursor.isNull(_cursorIndexOfEffectiveFrom)) {
              _tmpEffectiveFrom = null;
            } else {
              _tmpEffectiveFrom = _cursor.getString(_cursorIndexOfEffectiveFrom);
            }
            final String _tmpEffectiveTo;
            if (_cursor.isNull(_cursorIndexOfEffectiveTo)) {
              _tmpEffectiveTo = null;
            } else {
              _tmpEffectiveTo = _cursor.getString(_cursorIndexOfEffectiveTo);
            }
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
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp != 0;
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
            _item = new SessionPeriodEntity(_tmpId,_tmpSessionId,_tmpDeptId,_tmpDay,_tmpStartTime,_tmpEndTime,_tmpCourseCode,_tmpSubjectName,_tmpTeacherId,_tmpTeacherName,_tmpPeriodType,_tmpCreditHours,_tmpRoomNo,_tmpBuilding,_tmpNotes,_tmpEffectiveFrom,_tmpEffectiveTo,_tmpEntityId,_tmpCreatedAt,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
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
  public Object deleteByIds(final List<String> ids, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("DELETE FROM session_periods WHERE id IN (");
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
