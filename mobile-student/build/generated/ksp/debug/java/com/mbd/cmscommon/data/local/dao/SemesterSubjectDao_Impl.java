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
import com.mbd.cmscommon.data.local.entity.SemesterSubjectEntity;
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
public final class SemesterSubjectDao_Impl implements SemesterSubjectDao {
  private final RoomDatabase __db;

  private final SharedSQLiteStatement __preparedStmtOfDeleteForSemester;

  private final SharedSQLiteStatement __preparedStmtOfDeleteForSession;

  private final EntityUpsertionAdapter<SemesterSubjectEntity> __upsertionAdapterOfSemesterSubjectEntity;

  public SemesterSubjectDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__preparedStmtOfDeleteForSemester = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM semester_subjects WHERE sessionId = ? AND semester = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteForSession = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM semester_subjects WHERE sessionId = ?";
        return _query;
      }
    };
    this.__upsertionAdapterOfSemesterSubjectEntity = new EntityUpsertionAdapter<SemesterSubjectEntity>(new EntityInsertionAdapter<SemesterSubjectEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `semester_subjects` (`id`,`sessionId`,`semester`,`courseCode`,`name`,`creditHours`,`subjectType`,`isElective`,`outline`,`entityId`,`createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SemesterSubjectEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getSessionId());
        statement.bindLong(3, entity.getSemester());
        statement.bindString(4, entity.getCourseCode());
        statement.bindString(5, entity.getName());
        statement.bindLong(6, entity.getCreditHours());
        statement.bindString(7, entity.getSubjectType());
        final int _tmp = entity.isElective() ? 1 : 0;
        statement.bindLong(8, _tmp);
        if (entity.getOutline() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getOutline());
        }
        statement.bindLong(10, entity.getEntityId());
        statement.bindLong(11, entity.getCreatedAt());
        if (entity.getCreatedBy() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getCreatedBy());
        }
        statement.bindLong(13, entity.getUpdatedAt());
        if (entity.getUpdatedBy() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getUpdatedBy());
        }
        final int _tmp_1 = entity.isDeleted() ? 1 : 0;
        statement.bindLong(15, _tmp_1);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(16);
        } else {
          statement.bindLong(16, entity.getDeletedAt());
        }
        if (entity.getDeletedBy() == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, entity.getDeletedBy());
        }
      }
    }, new EntityDeletionOrUpdateAdapter<SemesterSubjectEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `semester_subjects` SET `id` = ?,`sessionId` = ?,`semester` = ?,`courseCode` = ?,`name` = ?,`creditHours` = ?,`subjectType` = ?,`isElective` = ?,`outline` = ?,`entityId` = ?,`createdAt` = ?,`createdBy` = ?,`updatedAt` = ?,`updatedBy` = ?,`isDeleted` = ?,`deletedAt` = ?,`deletedBy` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SemesterSubjectEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getSessionId());
        statement.bindLong(3, entity.getSemester());
        statement.bindString(4, entity.getCourseCode());
        statement.bindString(5, entity.getName());
        statement.bindLong(6, entity.getCreditHours());
        statement.bindString(7, entity.getSubjectType());
        final int _tmp = entity.isElective() ? 1 : 0;
        statement.bindLong(8, _tmp);
        if (entity.getOutline() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getOutline());
        }
        statement.bindLong(10, entity.getEntityId());
        statement.bindLong(11, entity.getCreatedAt());
        if (entity.getCreatedBy() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getCreatedBy());
        }
        statement.bindLong(13, entity.getUpdatedAt());
        if (entity.getUpdatedBy() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getUpdatedBy());
        }
        final int _tmp_1 = entity.isDeleted() ? 1 : 0;
        statement.bindLong(15, _tmp_1);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(16);
        } else {
          statement.bindLong(16, entity.getDeletedAt());
        }
        if (entity.getDeletedBy() == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, entity.getDeletedBy());
        }
        statement.bindString(18, entity.getId());
      }
    });
  }

  @Override
  public Object applyDelta(final List<SemesterSubjectEntity> active,
      final List<String> tombstoneIds, final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> SemesterSubjectDao.DefaultImpls.applyDelta(SemesterSubjectDao_Impl.this, active, tombstoneIds, __cont), $completion);
  }

  @Override
  public Object deleteForSemester(final String sessionId, final int semester,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteForSemester.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, sessionId);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, semester);
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
          __preparedStmtOfDeleteForSemester.release(_stmt);
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
  public Object upsertAll(final List<SemesterSubjectEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfSemesterSubjectEntity.upsert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<SemesterSubjectEntity>> observeForSemester(final String sessionId,
      final int semester) {
    final String _sql = "SELECT * FROM semester_subjects WHERE sessionId = ? AND semester = ? ORDER BY courseCode";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, semester);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"semester_subjects"}, new Callable<List<SemesterSubjectEntity>>() {
      @Override
      @NonNull
      public List<SemesterSubjectEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfSemester = CursorUtil.getColumnIndexOrThrow(_cursor, "semester");
          final int _cursorIndexOfCourseCode = CursorUtil.getColumnIndexOrThrow(_cursor, "courseCode");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCreditHours = CursorUtil.getColumnIndexOrThrow(_cursor, "creditHours");
          final int _cursorIndexOfSubjectType = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectType");
          final int _cursorIndexOfIsElective = CursorUtil.getColumnIndexOrThrow(_cursor, "isElective");
          final int _cursorIndexOfOutline = CursorUtil.getColumnIndexOrThrow(_cursor, "outline");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final List<SemesterSubjectEntity> _result = new ArrayList<SemesterSubjectEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SemesterSubjectEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final int _tmpSemester;
            _tmpSemester = _cursor.getInt(_cursorIndexOfSemester);
            final String _tmpCourseCode;
            _tmpCourseCode = _cursor.getString(_cursorIndexOfCourseCode);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpCreditHours;
            _tmpCreditHours = _cursor.getInt(_cursorIndexOfCreditHours);
            final String _tmpSubjectType;
            _tmpSubjectType = _cursor.getString(_cursorIndexOfSubjectType);
            final boolean _tmpIsElective;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsElective);
            _tmpIsElective = _tmp != 0;
            final String _tmpOutline;
            if (_cursor.isNull(_cursorIndexOfOutline)) {
              _tmpOutline = null;
            } else {
              _tmpOutline = _cursor.getString(_cursorIndexOfOutline);
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
            _item = new SemesterSubjectEntity(_tmpId,_tmpSessionId,_tmpSemester,_tmpCourseCode,_tmpName,_tmpCreditHours,_tmpSubjectType,_tmpIsElective,_tmpOutline,_tmpEntityId,_tmpCreatedAt,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
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
  public Flow<List<SemesterSubjectEntity>> observeForSession(final String sessionId) {
    final String _sql = "SELECT * FROM semester_subjects WHERE sessionId = ? ORDER BY semester, courseCode";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"semester_subjects"}, new Callable<List<SemesterSubjectEntity>>() {
      @Override
      @NonNull
      public List<SemesterSubjectEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfSemester = CursorUtil.getColumnIndexOrThrow(_cursor, "semester");
          final int _cursorIndexOfCourseCode = CursorUtil.getColumnIndexOrThrow(_cursor, "courseCode");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCreditHours = CursorUtil.getColumnIndexOrThrow(_cursor, "creditHours");
          final int _cursorIndexOfSubjectType = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectType");
          final int _cursorIndexOfIsElective = CursorUtil.getColumnIndexOrThrow(_cursor, "isElective");
          final int _cursorIndexOfOutline = CursorUtil.getColumnIndexOrThrow(_cursor, "outline");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final List<SemesterSubjectEntity> _result = new ArrayList<SemesterSubjectEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SemesterSubjectEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final int _tmpSemester;
            _tmpSemester = _cursor.getInt(_cursorIndexOfSemester);
            final String _tmpCourseCode;
            _tmpCourseCode = _cursor.getString(_cursorIndexOfCourseCode);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpCreditHours;
            _tmpCreditHours = _cursor.getInt(_cursorIndexOfCreditHours);
            final String _tmpSubjectType;
            _tmpSubjectType = _cursor.getString(_cursorIndexOfSubjectType);
            final boolean _tmpIsElective;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsElective);
            _tmpIsElective = _tmp != 0;
            final String _tmpOutline;
            if (_cursor.isNull(_cursorIndexOfOutline)) {
              _tmpOutline = null;
            } else {
              _tmpOutline = _cursor.getString(_cursorIndexOfOutline);
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
            _item = new SemesterSubjectEntity(_tmpId,_tmpSessionId,_tmpSemester,_tmpCourseCode,_tmpName,_tmpCreditHours,_tmpSubjectType,_tmpIsElective,_tmpOutline,_tmpEntityId,_tmpCreatedAt,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
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
        _stringBuilder.append("DELETE FROM semester_subjects WHERE id IN (");
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
