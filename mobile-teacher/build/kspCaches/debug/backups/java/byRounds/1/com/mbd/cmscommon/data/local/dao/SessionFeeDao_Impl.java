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
import com.mbd.cmscommon.data.local.entity.SessionFeeEntity;
import com.mbd.cmscommon.data.local.entity.SessionFeeHeadEntity;
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

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SessionFeeDao_Impl implements SessionFeeDao {
  private final RoomDatabase __db;

  private final SharedSQLiteStatement __preparedStmtOfDeleteHeadsForSession;

  private final EntityUpsertionAdapter<SessionFeeEntity> __upsertionAdapterOfSessionFeeEntity;

  private final EntityUpsertionAdapter<SessionFeeHeadEntity> __upsertionAdapterOfSessionFeeHeadEntity;

  public SessionFeeDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__preparedStmtOfDeleteHeadsForSession = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM session_fee_heads WHERE sessionId = ?";
        return _query;
      }
    };
    this.__upsertionAdapterOfSessionFeeEntity = new EntityUpsertionAdapter<SessionFeeEntity>(new EntityInsertionAdapter<SessionFeeEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `session_fees` (`sessionId`,`cadence`,`academicYear`,`dueDate`,`lateFineNote`,`paymentNote`,`entityId`,`createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SessionFeeEntity entity) {
        statement.bindString(1, entity.getSessionId());
        statement.bindString(2, entity.getCadence());
        if (entity.getAcademicYear() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getAcademicYear());
        }
        if (entity.getDueDate() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getDueDate());
        }
        if (entity.getLateFineNote() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getLateFineNote());
        }
        if (entity.getPaymentNote() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getPaymentNote());
        }
        statement.bindLong(7, entity.getEntityId());
        statement.bindLong(8, entity.getCreatedAt());
        if (entity.getCreatedBy() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getCreatedBy());
        }
        statement.bindLong(10, entity.getUpdatedAt());
        if (entity.getUpdatedBy() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getUpdatedBy());
        }
        final int _tmp = entity.isDeleted() ? 1 : 0;
        statement.bindLong(12, _tmp);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(13);
        } else {
          statement.bindLong(13, entity.getDeletedAt());
        }
        if (entity.getDeletedBy() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getDeletedBy());
        }
      }
    }, new EntityDeletionOrUpdateAdapter<SessionFeeEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `session_fees` SET `sessionId` = ?,`cadence` = ?,`academicYear` = ?,`dueDate` = ?,`lateFineNote` = ?,`paymentNote` = ?,`entityId` = ?,`createdAt` = ?,`createdBy` = ?,`updatedAt` = ?,`updatedBy` = ?,`isDeleted` = ?,`deletedAt` = ?,`deletedBy` = ? WHERE `sessionId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SessionFeeEntity entity) {
        statement.bindString(1, entity.getSessionId());
        statement.bindString(2, entity.getCadence());
        if (entity.getAcademicYear() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getAcademicYear());
        }
        if (entity.getDueDate() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getDueDate());
        }
        if (entity.getLateFineNote() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getLateFineNote());
        }
        if (entity.getPaymentNote() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getPaymentNote());
        }
        statement.bindLong(7, entity.getEntityId());
        statement.bindLong(8, entity.getCreatedAt());
        if (entity.getCreatedBy() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getCreatedBy());
        }
        statement.bindLong(10, entity.getUpdatedAt());
        if (entity.getUpdatedBy() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getUpdatedBy());
        }
        final int _tmp = entity.isDeleted() ? 1 : 0;
        statement.bindLong(12, _tmp);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(13);
        } else {
          statement.bindLong(13, entity.getDeletedAt());
        }
        if (entity.getDeletedBy() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getDeletedBy());
        }
        statement.bindString(15, entity.getSessionId());
      }
    });
    this.__upsertionAdapterOfSessionFeeHeadEntity = new EntityUpsertionAdapter<SessionFeeHeadEntity>(new EntityInsertionAdapter<SessionFeeHeadEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `session_fee_heads` (`id`,`sessionId`,`label`,`amount`,`position`,`entityId`,`createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SessionFeeHeadEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getSessionId());
        statement.bindString(3, entity.getLabel());
        statement.bindDouble(4, entity.getAmount());
        statement.bindLong(5, entity.getPosition());
        statement.bindLong(6, entity.getEntityId());
        statement.bindLong(7, entity.getCreatedAt());
        if (entity.getCreatedBy() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getCreatedBy());
        }
        statement.bindLong(9, entity.getUpdatedAt());
        if (entity.getUpdatedBy() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getUpdatedBy());
        }
        final int _tmp = entity.isDeleted() ? 1 : 0;
        statement.bindLong(11, _tmp);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getDeletedAt());
        }
        if (entity.getDeletedBy() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getDeletedBy());
        }
      }
    }, new EntityDeletionOrUpdateAdapter<SessionFeeHeadEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `session_fee_heads` SET `id` = ?,`sessionId` = ?,`label` = ?,`amount` = ?,`position` = ?,`entityId` = ?,`createdAt` = ?,`createdBy` = ?,`updatedAt` = ?,`updatedBy` = ?,`isDeleted` = ?,`deletedAt` = ?,`deletedBy` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SessionFeeHeadEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getSessionId());
        statement.bindString(3, entity.getLabel());
        statement.bindDouble(4, entity.getAmount());
        statement.bindLong(5, entity.getPosition());
        statement.bindLong(6, entity.getEntityId());
        statement.bindLong(7, entity.getCreatedAt());
        if (entity.getCreatedBy() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getCreatedBy());
        }
        statement.bindLong(9, entity.getUpdatedAt());
        if (entity.getUpdatedBy() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getUpdatedBy());
        }
        final int _tmp = entity.isDeleted() ? 1 : 0;
        statement.bindLong(11, _tmp);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getDeletedAt());
        }
        if (entity.getDeletedBy() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getDeletedBy());
        }
        statement.bindString(14, entity.getId());
      }
    });
  }

  @Override
  public Object applyFeeDelta(final List<SessionFeeEntity> active, final List<String> tombstoneIds,
      final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> SessionFeeDao.DefaultImpls.applyFeeDelta(SessionFeeDao_Impl.this, active, tombstoneIds, __cont), $completion);
  }

  @Override
  public Object applyHeadDelta(final List<SessionFeeHeadEntity> active,
      final List<String> tombstoneIds, final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> SessionFeeDao.DefaultImpls.applyHeadDelta(SessionFeeDao_Impl.this, active, tombstoneIds, __cont), $completion);
  }

  @Override
  public Object deleteHeadsForSession(final String sessionId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteHeadsForSession.acquire();
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
          __preparedStmtOfDeleteHeadsForSession.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertFees(final List<SessionFeeEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfSessionFeeEntity.upsert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertHeads(final List<SessionFeeHeadEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfSessionFeeHeadEntity.upsert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getFee(final String sessionId,
      final Continuation<? super SessionFeeEntity> $completion) {
    final String _sql = "SELECT * FROM session_fees WHERE sessionId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SessionFeeEntity>() {
      @Override
      @Nullable
      public SessionFeeEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfCadence = CursorUtil.getColumnIndexOrThrow(_cursor, "cadence");
          final int _cursorIndexOfAcademicYear = CursorUtil.getColumnIndexOrThrow(_cursor, "academicYear");
          final int _cursorIndexOfDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDate");
          final int _cursorIndexOfLateFineNote = CursorUtil.getColumnIndexOrThrow(_cursor, "lateFineNote");
          final int _cursorIndexOfPaymentNote = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentNote");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final SessionFeeEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpCadence;
            _tmpCadence = _cursor.getString(_cursorIndexOfCadence);
            final String _tmpAcademicYear;
            if (_cursor.isNull(_cursorIndexOfAcademicYear)) {
              _tmpAcademicYear = null;
            } else {
              _tmpAcademicYear = _cursor.getString(_cursorIndexOfAcademicYear);
            }
            final String _tmpDueDate;
            if (_cursor.isNull(_cursorIndexOfDueDate)) {
              _tmpDueDate = null;
            } else {
              _tmpDueDate = _cursor.getString(_cursorIndexOfDueDate);
            }
            final String _tmpLateFineNote;
            if (_cursor.isNull(_cursorIndexOfLateFineNote)) {
              _tmpLateFineNote = null;
            } else {
              _tmpLateFineNote = _cursor.getString(_cursorIndexOfLateFineNote);
            }
            final String _tmpPaymentNote;
            if (_cursor.isNull(_cursorIndexOfPaymentNote)) {
              _tmpPaymentNote = null;
            } else {
              _tmpPaymentNote = _cursor.getString(_cursorIndexOfPaymentNote);
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
            _result = new SessionFeeEntity(_tmpSessionId,_tmpCadence,_tmpAcademicYear,_tmpDueDate,_tmpLateFineNote,_tmpPaymentNote,_tmpEntityId,_tmpCreatedAt,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
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
  public Object getHeads(final String sessionId,
      final Continuation<? super List<SessionFeeHeadEntity>> $completion) {
    final String _sql = "SELECT * FROM session_fee_heads WHERE sessionId = ? ORDER BY position, label";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SessionFeeHeadEntity>>() {
      @Override
      @NonNull
      public List<SessionFeeHeadEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "label");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfPosition = CursorUtil.getColumnIndexOrThrow(_cursor, "position");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final List<SessionFeeHeadEntity> _result = new ArrayList<SessionFeeHeadEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SessionFeeHeadEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpLabel;
            _tmpLabel = _cursor.getString(_cursorIndexOfLabel);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final int _tmpPosition;
            _tmpPosition = _cursor.getInt(_cursorIndexOfPosition);
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
            _item = new SessionFeeHeadEntity(_tmpId,_tmpSessionId,_tmpLabel,_tmpAmount,_tmpPosition,_tmpEntityId,_tmpCreatedAt,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
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
  public Object deleteFeesByIds(final List<String> sessionIds,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("DELETE FROM session_fees WHERE sessionId IN (");
        final int _inputSize = sessionIds.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        for (String _item : sessionIds) {
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

  @Override
  public Object deleteHeadsByIds(final List<String> ids,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("DELETE FROM session_fee_heads WHERE id IN (");
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
