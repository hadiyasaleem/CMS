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
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.mbd.cmscommon.data.local.entity.TableSyncStateEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TableSyncStateDao_Impl implements TableSyncStateDao {
  private final RoomDatabase __db;

  private final SharedSQLiteStatement __preparedStmtOfClear;

  private final EntityUpsertionAdapter<TableSyncStateEntity> __upsertionAdapterOfTableSyncStateEntity;

  public TableSyncStateDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__preparedStmtOfClear = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        DELETE FROM table_sync_state\n"
                + "        WHERE owner_key = ?\n"
                + "          AND table_name = ?\n"
                + "          AND scope_key = ?\n"
                + "        ";
        return _query;
      }
    };
    this.__upsertionAdapterOfTableSyncStateEntity = new EntityUpsertionAdapter<TableSyncStateEntity>(new EntityInsertionAdapter<TableSyncStateEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `table_sync_state` (`owner_key`,`table_name`,`scope_key`,`last_updated_at`,`last_successful_sync_at`,`created_at`,`created_by`,`updated_at`,`updated_by`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TableSyncStateEntity entity) {
        statement.bindString(1, entity.getOwnerKey());
        statement.bindString(2, entity.getTableName());
        statement.bindString(3, entity.getScopeKey());
        statement.bindString(4, entity.getLastUpdatedAt());
        statement.bindString(5, entity.getLastSuccessfulSyncAt());
        statement.bindLong(6, entity.getCreatedAt());
        if (entity.getCreatedBy() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getCreatedBy());
        }
        statement.bindLong(8, entity.getUpdatedAt());
        if (entity.getUpdatedBy() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getUpdatedBy());
        }
      }
    }, new EntityDeletionOrUpdateAdapter<TableSyncStateEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `table_sync_state` SET `owner_key` = ?,`table_name` = ?,`scope_key` = ?,`last_updated_at` = ?,`last_successful_sync_at` = ?,`created_at` = ?,`created_by` = ?,`updated_at` = ?,`updated_by` = ? WHERE `owner_key` = ? AND `table_name` = ? AND `scope_key` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TableSyncStateEntity entity) {
        statement.bindString(1, entity.getOwnerKey());
        statement.bindString(2, entity.getTableName());
        statement.bindString(3, entity.getScopeKey());
        statement.bindString(4, entity.getLastUpdatedAt());
        statement.bindString(5, entity.getLastSuccessfulSyncAt());
        statement.bindLong(6, entity.getCreatedAt());
        if (entity.getCreatedBy() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getCreatedBy());
        }
        statement.bindLong(8, entity.getUpdatedAt());
        if (entity.getUpdatedBy() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getUpdatedBy());
        }
        statement.bindString(10, entity.getOwnerKey());
        statement.bindString(11, entity.getTableName());
        statement.bindString(12, entity.getScopeKey());
      }
    });
  }

  @Override
  public Object clear(final String ownerKey, final String tableName, final String scopeKey,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClear.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, ownerKey);
        _argIndex = 2;
        _stmt.bindString(_argIndex, tableName);
        _argIndex = 3;
        _stmt.bindString(_argIndex, scopeKey);
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
          __preparedStmtOfClear.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object upsert(final TableSyncStateEntity item,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfTableSyncStateEntity.upsert(item);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object get(final String ownerKey, final String tableName, final String scopeKey,
      final Continuation<? super TableSyncStateEntity> $completion) {
    final String _sql = "\n"
            + "        SELECT * FROM table_sync_state\n"
            + "        WHERE owner_key = ?\n"
            + "          AND table_name = ?\n"
            + "          AND scope_key = ?\n"
            + "        LIMIT 1\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, ownerKey);
    _argIndex = 2;
    _statement.bindString(_argIndex, tableName);
    _argIndex = 3;
    _statement.bindString(_argIndex, scopeKey);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TableSyncStateEntity>() {
      @Override
      @Nullable
      public TableSyncStateEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfOwnerKey = CursorUtil.getColumnIndexOrThrow(_cursor, "owner_key");
          final int _cursorIndexOfTableName = CursorUtil.getColumnIndexOrThrow(_cursor, "table_name");
          final int _cursorIndexOfScopeKey = CursorUtil.getColumnIndexOrThrow(_cursor, "scope_key");
          final int _cursorIndexOfLastUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "last_updated_at");
          final int _cursorIndexOfLastSuccessfulSyncAt = CursorUtil.getColumnIndexOrThrow(_cursor, "last_successful_sync_at");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "created_by");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_by");
          final TableSyncStateEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpOwnerKey;
            _tmpOwnerKey = _cursor.getString(_cursorIndexOfOwnerKey);
            final String _tmpTableName;
            _tmpTableName = _cursor.getString(_cursorIndexOfTableName);
            final String _tmpScopeKey;
            _tmpScopeKey = _cursor.getString(_cursorIndexOfScopeKey);
            final String _tmpLastUpdatedAt;
            _tmpLastUpdatedAt = _cursor.getString(_cursorIndexOfLastUpdatedAt);
            final String _tmpLastSuccessfulSyncAt;
            _tmpLastSuccessfulSyncAt = _cursor.getString(_cursorIndexOfLastSuccessfulSyncAt);
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
            _result = new TableSyncStateEntity(_tmpOwnerKey,_tmpTableName,_tmpScopeKey,_tmpLastUpdatedAt,_tmpLastSuccessfulSyncAt,_tmpCreatedAt,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
