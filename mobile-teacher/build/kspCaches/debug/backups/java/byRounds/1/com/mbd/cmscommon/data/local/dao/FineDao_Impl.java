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
import com.mbd.cmscommon.data.local.entity.FineEntity;
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
public final class FineDao_Impl implements FineDao {
  private final RoomDatabase __db;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  private final EntityUpsertionAdapter<FineEntity> __upsertionAdapterOfFineEntity;

  public FineDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM fines WHERE fineId = ?";
        return _query;
      }
    };
    this.__upsertionAdapterOfFineEntity = new EntityUpsertionAdapter<FineEntity>(new EntityInsertionAdapter<FineEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `fines` (`fineId`,`sessionId`,`rollNumber`,`category`,`amount`,`reason`,`issuedBy`,`issuedAt`,`entityId`,`createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FineEntity entity) {
        statement.bindString(1, entity.getFineId());
        statement.bindString(2, entity.getSessionId());
        statement.bindString(3, entity.getRollNumber());
        statement.bindString(4, entity.getCategory());
        statement.bindDouble(5, entity.getAmount());
        statement.bindString(6, entity.getReason());
        if (entity.getIssuedBy() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getIssuedBy());
        }
        if (entity.getIssuedAt() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getIssuedAt());
        }
        statement.bindLong(9, entity.getEntityId());
        statement.bindLong(10, entity.getCreatedAt());
        if (entity.getCreatedBy() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getCreatedBy());
        }
        statement.bindLong(12, entity.getUpdatedAt());
        if (entity.getUpdatedBy() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getUpdatedBy());
        }
        final int _tmp = entity.isDeleted() ? 1 : 0;
        statement.bindLong(14, _tmp);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(15);
        } else {
          statement.bindLong(15, entity.getDeletedAt());
        }
        if (entity.getDeletedBy() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getDeletedBy());
        }
      }
    }, new EntityDeletionOrUpdateAdapter<FineEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `fines` SET `fineId` = ?,`sessionId` = ?,`rollNumber` = ?,`category` = ?,`amount` = ?,`reason` = ?,`issuedBy` = ?,`issuedAt` = ?,`entityId` = ?,`createdAt` = ?,`createdBy` = ?,`updatedAt` = ?,`updatedBy` = ?,`isDeleted` = ?,`deletedAt` = ?,`deletedBy` = ? WHERE `fineId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FineEntity entity) {
        statement.bindString(1, entity.getFineId());
        statement.bindString(2, entity.getSessionId());
        statement.bindString(3, entity.getRollNumber());
        statement.bindString(4, entity.getCategory());
        statement.bindDouble(5, entity.getAmount());
        statement.bindString(6, entity.getReason());
        if (entity.getIssuedBy() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getIssuedBy());
        }
        if (entity.getIssuedAt() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getIssuedAt());
        }
        statement.bindLong(9, entity.getEntityId());
        statement.bindLong(10, entity.getCreatedAt());
        if (entity.getCreatedBy() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getCreatedBy());
        }
        statement.bindLong(12, entity.getUpdatedAt());
        if (entity.getUpdatedBy() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getUpdatedBy());
        }
        final int _tmp = entity.isDeleted() ? 1 : 0;
        statement.bindLong(14, _tmp);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(15);
        } else {
          statement.bindLong(15, entity.getDeletedAt());
        }
        if (entity.getDeletedBy() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getDeletedBy());
        }
        statement.bindString(17, entity.getFineId());
      }
    });
  }

  @Override
  public Object applyDelta(final List<FineEntity> active, final List<String> tombstoneIds,
      final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> FineDao.DefaultImpls.applyDelta(FineDao_Impl.this, active, tombstoneIds, __cont), $completion);
  }

  @Override
  public Object deleteById(final String fineId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, fineId);
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
  public Object upsertAll(final List<FineEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfFineEntity.upsert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getForStudent(final String sessionId, final String rollNumber,
      final Continuation<? super List<FineEntity>> $completion) {
    final String _sql = "SELECT * FROM fines WHERE sessionId = ? AND rollNumber = ? ORDER BY issuedAt DESC, createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    _argIndex = 2;
    _statement.bindString(_argIndex, rollNumber);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<FineEntity>>() {
      @Override
      @NonNull
      public List<FineEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfFineId = CursorUtil.getColumnIndexOrThrow(_cursor, "fineId");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfRollNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNumber");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
          final int _cursorIndexOfIssuedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "issuedBy");
          final int _cursorIndexOfIssuedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "issuedAt");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final List<FineEntity> _result = new ArrayList<FineEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FineEntity _item;
            final String _tmpFineId;
            _tmpFineId = _cursor.getString(_cursorIndexOfFineId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpRollNumber;
            _tmpRollNumber = _cursor.getString(_cursorIndexOfRollNumber);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpReason;
            _tmpReason = _cursor.getString(_cursorIndexOfReason);
            final String _tmpIssuedBy;
            if (_cursor.isNull(_cursorIndexOfIssuedBy)) {
              _tmpIssuedBy = null;
            } else {
              _tmpIssuedBy = _cursor.getString(_cursorIndexOfIssuedBy);
            }
            final Long _tmpIssuedAt;
            if (_cursor.isNull(_cursorIndexOfIssuedAt)) {
              _tmpIssuedAt = null;
            } else {
              _tmpIssuedAt = _cursor.getLong(_cursorIndexOfIssuedAt);
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
            _item = new FineEntity(_tmpFineId,_tmpSessionId,_tmpRollNumber,_tmpCategory,_tmpAmount,_tmpReason,_tmpIssuedBy,_tmpIssuedAt,_tmpEntityId,_tmpCreatedAt,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
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
        _stringBuilder.append("DELETE FROM fines WHERE fineId IN (");
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
