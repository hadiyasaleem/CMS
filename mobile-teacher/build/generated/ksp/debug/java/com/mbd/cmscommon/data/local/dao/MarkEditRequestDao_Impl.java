package com.mbd.cmscommon.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
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
import com.mbd.cmscommon.data.local.entity.MarkEditRequestEntity;
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

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class MarkEditRequestDao_Impl implements MarkEditRequestDao {
  private final RoomDatabase __db;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  private final EntityUpsertionAdapter<MarkEditRequestEntity> __upsertionAdapterOfMarkEditRequestEntity;

  public MarkEditRequestDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM mark_edit_requests WHERE requestId = ?";
        return _query;
      }
    };
    this.__upsertionAdapterOfMarkEditRequestEntity = new EntityUpsertionAdapter<MarkEditRequestEntity>(new EntityInsertionAdapter<MarkEditRequestEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `mark_edit_requests` (`requestId`,`sessionId`,`semester`,`courseCode`,`examType`,`rollNumber`,`currentScore`,`requestedScore`,`reason`,`status`,`requestedBy`,`reviewedBy`,`requestedAt`,`reviewedAt`,`entityId`,`createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MarkEditRequestEntity entity) {
        statement.bindString(1, entity.getRequestId());
        statement.bindString(2, entity.getSessionId());
        statement.bindLong(3, entity.getSemester());
        statement.bindString(4, entity.getCourseCode());
        statement.bindString(5, entity.getExamType());
        statement.bindString(6, entity.getRollNumber());
        if (entity.getCurrentScore() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getCurrentScore());
        }
        statement.bindLong(8, entity.getRequestedScore());
        if (entity.getReason() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getReason());
        }
        statement.bindString(10, entity.getStatus());
        statement.bindString(11, entity.getRequestedBy());
        if (entity.getReviewedBy() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getReviewedBy());
        }
        statement.bindLong(13, entity.getRequestedAt());
        if (entity.getReviewedAt() == null) {
          statement.bindNull(14);
        } else {
          statement.bindLong(14, entity.getReviewedAt());
        }
        statement.bindLong(15, entity.getEntityId());
        statement.bindLong(16, entity.getCreatedAt());
        if (entity.getCreatedBy() == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, entity.getCreatedBy());
        }
        statement.bindLong(18, entity.getUpdatedAt());
        if (entity.getUpdatedBy() == null) {
          statement.bindNull(19);
        } else {
          statement.bindString(19, entity.getUpdatedBy());
        }
        final int _tmp = entity.isDeleted() ? 1 : 0;
        statement.bindLong(20, _tmp);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(21);
        } else {
          statement.bindLong(21, entity.getDeletedAt());
        }
        if (entity.getDeletedBy() == null) {
          statement.bindNull(22);
        } else {
          statement.bindString(22, entity.getDeletedBy());
        }
      }
    }, new EntityDeletionOrUpdateAdapter<MarkEditRequestEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `mark_edit_requests` SET `requestId` = ?,`sessionId` = ?,`semester` = ?,`courseCode` = ?,`examType` = ?,`rollNumber` = ?,`currentScore` = ?,`requestedScore` = ?,`reason` = ?,`status` = ?,`requestedBy` = ?,`reviewedBy` = ?,`requestedAt` = ?,`reviewedAt` = ?,`entityId` = ?,`createdAt` = ?,`createdBy` = ?,`updatedAt` = ?,`updatedBy` = ?,`isDeleted` = ?,`deletedAt` = ?,`deletedBy` = ? WHERE `requestId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MarkEditRequestEntity entity) {
        statement.bindString(1, entity.getRequestId());
        statement.bindString(2, entity.getSessionId());
        statement.bindLong(3, entity.getSemester());
        statement.bindString(4, entity.getCourseCode());
        statement.bindString(5, entity.getExamType());
        statement.bindString(6, entity.getRollNumber());
        if (entity.getCurrentScore() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getCurrentScore());
        }
        statement.bindLong(8, entity.getRequestedScore());
        if (entity.getReason() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getReason());
        }
        statement.bindString(10, entity.getStatus());
        statement.bindString(11, entity.getRequestedBy());
        if (entity.getReviewedBy() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getReviewedBy());
        }
        statement.bindLong(13, entity.getRequestedAt());
        if (entity.getReviewedAt() == null) {
          statement.bindNull(14);
        } else {
          statement.bindLong(14, entity.getReviewedAt());
        }
        statement.bindLong(15, entity.getEntityId());
        statement.bindLong(16, entity.getCreatedAt());
        if (entity.getCreatedBy() == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, entity.getCreatedBy());
        }
        statement.bindLong(18, entity.getUpdatedAt());
        if (entity.getUpdatedBy() == null) {
          statement.bindNull(19);
        } else {
          statement.bindString(19, entity.getUpdatedBy());
        }
        final int _tmp = entity.isDeleted() ? 1 : 0;
        statement.bindLong(20, _tmp);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(21);
        } else {
          statement.bindLong(21, entity.getDeletedAt());
        }
        if (entity.getDeletedBy() == null) {
          statement.bindNull(22);
        } else {
          statement.bindString(22, entity.getDeletedBy());
        }
        statement.bindString(23, entity.getRequestId());
      }
    });
  }

  @Override
  public Object applyDelta(final List<MarkEditRequestEntity> active,
      final List<String> tombstoneIds, final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> MarkEditRequestDao.DefaultImpls.applyDelta(MarkEditRequestDao_Impl.this, active, tombstoneIds, __cont), $completion);
  }

  @Override
  public Object deleteById(final String requestId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, requestId);
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
  public Object upsertAll(final List<MarkEditRequestEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfMarkEditRequestEntity.upsert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getPendingForAssignment(final String sessionId, final String courseCode,
      final String examType, final Continuation<? super List<MarkEditRequestEntity>> $completion) {
    final String _sql = "\n"
            + "        SELECT * FROM mark_edit_requests\n"
            + "        WHERE sessionId = ? AND courseCode = ? AND examType = ? AND status = 'PENDING'\n"
            + "        ORDER BY rollNumber\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    _argIndex = 2;
    _statement.bindString(_argIndex, courseCode);
    _argIndex = 3;
    _statement.bindString(_argIndex, examType);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<MarkEditRequestEntity>>() {
      @Override
      @NonNull
      public List<MarkEditRequestEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfRequestId = CursorUtil.getColumnIndexOrThrow(_cursor, "requestId");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfSemester = CursorUtil.getColumnIndexOrThrow(_cursor, "semester");
          final int _cursorIndexOfCourseCode = CursorUtil.getColumnIndexOrThrow(_cursor, "courseCode");
          final int _cursorIndexOfExamType = CursorUtil.getColumnIndexOrThrow(_cursor, "examType");
          final int _cursorIndexOfRollNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNumber");
          final int _cursorIndexOfCurrentScore = CursorUtil.getColumnIndexOrThrow(_cursor, "currentScore");
          final int _cursorIndexOfRequestedScore = CursorUtil.getColumnIndexOrThrow(_cursor, "requestedScore");
          final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfRequestedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "requestedBy");
          final int _cursorIndexOfReviewedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewedBy");
          final int _cursorIndexOfRequestedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "requestedAt");
          final int _cursorIndexOfReviewedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewedAt");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final List<MarkEditRequestEntity> _result = new ArrayList<MarkEditRequestEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MarkEditRequestEntity _item;
            final String _tmpRequestId;
            _tmpRequestId = _cursor.getString(_cursorIndexOfRequestId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final int _tmpSemester;
            _tmpSemester = _cursor.getInt(_cursorIndexOfSemester);
            final String _tmpCourseCode;
            _tmpCourseCode = _cursor.getString(_cursorIndexOfCourseCode);
            final String _tmpExamType;
            _tmpExamType = _cursor.getString(_cursorIndexOfExamType);
            final String _tmpRollNumber;
            _tmpRollNumber = _cursor.getString(_cursorIndexOfRollNumber);
            final Integer _tmpCurrentScore;
            if (_cursor.isNull(_cursorIndexOfCurrentScore)) {
              _tmpCurrentScore = null;
            } else {
              _tmpCurrentScore = _cursor.getInt(_cursorIndexOfCurrentScore);
            }
            final int _tmpRequestedScore;
            _tmpRequestedScore = _cursor.getInt(_cursorIndexOfRequestedScore);
            final String _tmpReason;
            if (_cursor.isNull(_cursorIndexOfReason)) {
              _tmpReason = null;
            } else {
              _tmpReason = _cursor.getString(_cursorIndexOfReason);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpRequestedBy;
            _tmpRequestedBy = _cursor.getString(_cursorIndexOfRequestedBy);
            final String _tmpReviewedBy;
            if (_cursor.isNull(_cursorIndexOfReviewedBy)) {
              _tmpReviewedBy = null;
            } else {
              _tmpReviewedBy = _cursor.getString(_cursorIndexOfReviewedBy);
            }
            final long _tmpRequestedAt;
            _tmpRequestedAt = _cursor.getLong(_cursorIndexOfRequestedAt);
            final Long _tmpReviewedAt;
            if (_cursor.isNull(_cursorIndexOfReviewedAt)) {
              _tmpReviewedAt = null;
            } else {
              _tmpReviewedAt = _cursor.getLong(_cursorIndexOfReviewedAt);
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
            _item = new MarkEditRequestEntity(_tmpRequestId,_tmpSessionId,_tmpSemester,_tmpCourseCode,_tmpExamType,_tmpRollNumber,_tmpCurrentScore,_tmpRequestedScore,_tmpReason,_tmpStatus,_tmpRequestedBy,_tmpReviewedBy,_tmpRequestedAt,_tmpReviewedAt,_tmpEntityId,_tmpCreatedAt,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
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
  public Object getPendingRequests(
      final Continuation<? super List<MarkEditRequestEntity>> $completion) {
    final String _sql = "SELECT * FROM mark_edit_requests WHERE status = 'PENDING' ORDER BY requestedAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<MarkEditRequestEntity>>() {
      @Override
      @NonNull
      public List<MarkEditRequestEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfRequestId = CursorUtil.getColumnIndexOrThrow(_cursor, "requestId");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfSemester = CursorUtil.getColumnIndexOrThrow(_cursor, "semester");
          final int _cursorIndexOfCourseCode = CursorUtil.getColumnIndexOrThrow(_cursor, "courseCode");
          final int _cursorIndexOfExamType = CursorUtil.getColumnIndexOrThrow(_cursor, "examType");
          final int _cursorIndexOfRollNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNumber");
          final int _cursorIndexOfCurrentScore = CursorUtil.getColumnIndexOrThrow(_cursor, "currentScore");
          final int _cursorIndexOfRequestedScore = CursorUtil.getColumnIndexOrThrow(_cursor, "requestedScore");
          final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfRequestedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "requestedBy");
          final int _cursorIndexOfReviewedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewedBy");
          final int _cursorIndexOfRequestedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "requestedAt");
          final int _cursorIndexOfReviewedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewedAt");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final List<MarkEditRequestEntity> _result = new ArrayList<MarkEditRequestEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MarkEditRequestEntity _item;
            final String _tmpRequestId;
            _tmpRequestId = _cursor.getString(_cursorIndexOfRequestId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final int _tmpSemester;
            _tmpSemester = _cursor.getInt(_cursorIndexOfSemester);
            final String _tmpCourseCode;
            _tmpCourseCode = _cursor.getString(_cursorIndexOfCourseCode);
            final String _tmpExamType;
            _tmpExamType = _cursor.getString(_cursorIndexOfExamType);
            final String _tmpRollNumber;
            _tmpRollNumber = _cursor.getString(_cursorIndexOfRollNumber);
            final Integer _tmpCurrentScore;
            if (_cursor.isNull(_cursorIndexOfCurrentScore)) {
              _tmpCurrentScore = null;
            } else {
              _tmpCurrentScore = _cursor.getInt(_cursorIndexOfCurrentScore);
            }
            final int _tmpRequestedScore;
            _tmpRequestedScore = _cursor.getInt(_cursorIndexOfRequestedScore);
            final String _tmpReason;
            if (_cursor.isNull(_cursorIndexOfReason)) {
              _tmpReason = null;
            } else {
              _tmpReason = _cursor.getString(_cursorIndexOfReason);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpRequestedBy;
            _tmpRequestedBy = _cursor.getString(_cursorIndexOfRequestedBy);
            final String _tmpReviewedBy;
            if (_cursor.isNull(_cursorIndexOfReviewedBy)) {
              _tmpReviewedBy = null;
            } else {
              _tmpReviewedBy = _cursor.getString(_cursorIndexOfReviewedBy);
            }
            final long _tmpRequestedAt;
            _tmpRequestedAt = _cursor.getLong(_cursorIndexOfRequestedAt);
            final Long _tmpReviewedAt;
            if (_cursor.isNull(_cursorIndexOfReviewedAt)) {
              _tmpReviewedAt = null;
            } else {
              _tmpReviewedAt = _cursor.getLong(_cursorIndexOfReviewedAt);
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
            _item = new MarkEditRequestEntity(_tmpRequestId,_tmpSessionId,_tmpSemester,_tmpCourseCode,_tmpExamType,_tmpRollNumber,_tmpCurrentScore,_tmpRequestedScore,_tmpReason,_tmpStatus,_tmpRequestedBy,_tmpReviewedBy,_tmpRequestedAt,_tmpReviewedAt,_tmpEntityId,_tmpCreatedAt,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
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
  public Object deleteByIds(final List<String> ids, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("DELETE FROM mark_edit_requests WHERE requestId IN (");
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
