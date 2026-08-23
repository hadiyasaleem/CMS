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
import androidx.sqlite.db.SupportSQLiteStatement;
import com.mbd.cmscommon.data.local.entity.AdministratorAccountEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
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
public final class AdministratorAccountDao_Impl implements AdministratorAccountDao {
  private final RoomDatabase __db;

  private final SharedSQLiteStatement __preparedStmtOfClear;

  private final EntityUpsertionAdapter<AdministratorAccountEntity> __upsertionAdapterOfAdministratorAccountEntity;

  public AdministratorAccountDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__preparedStmtOfClear = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM administrator_accounts";
        return _query;
      }
    };
    this.__upsertionAdapterOfAdministratorAccountEntity = new EntityUpsertionAdapter<AdministratorAccountEntity>(new EntityInsertionAdapter<AdministratorAccountEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `administrator_accounts` (`id`,`email`,`status`,`lastLoginAt`,`entityId`,`createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AdministratorAccountEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getEmail());
        statement.bindString(3, entity.getStatus());
        if (entity.getLastLoginAt() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getLastLoginAt());
        }
        statement.bindLong(5, entity.getEntityId());
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
        final int _tmp = entity.isDeleted() ? 1 : 0;
        statement.bindLong(10, _tmp);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(11);
        } else {
          statement.bindLong(11, entity.getDeletedAt());
        }
        if (entity.getDeletedBy() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getDeletedBy());
        }
      }
    }, new EntityDeletionOrUpdateAdapter<AdministratorAccountEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `administrator_accounts` SET `id` = ?,`email` = ?,`status` = ?,`lastLoginAt` = ?,`entityId` = ?,`createdAt` = ?,`createdBy` = ?,`updatedAt` = ?,`updatedBy` = ?,`isDeleted` = ?,`deletedAt` = ?,`deletedBy` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AdministratorAccountEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getEmail());
        statement.bindString(3, entity.getStatus());
        if (entity.getLastLoginAt() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getLastLoginAt());
        }
        statement.bindLong(5, entity.getEntityId());
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
        final int _tmp = entity.isDeleted() ? 1 : 0;
        statement.bindLong(10, _tmp);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(11);
        } else {
          statement.bindLong(11, entity.getDeletedAt());
        }
        if (entity.getDeletedBy() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getDeletedBy());
        }
        statement.bindString(13, entity.getId());
      }
    });
  }

  @Override
  public Object applyDelta(final List<AdministratorAccountEntity> rows,
      final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> AdministratorAccountDao.DefaultImpls.applyDelta(AdministratorAccountDao_Impl.this, rows, __cont), $completion);
  }

  @Override
  public Object clear(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClear.acquire();
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
  public Object upsertAll(final List<AdministratorAccountEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfAdministratorAccountEntity.upsert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<AdministratorAccountEntity>> observeAll() {
    final String _sql = "SELECT * FROM administrator_accounts WHERE isDeleted = 0 ORDER BY email COLLATE NOCASE";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"administrator_accounts"}, new Callable<List<AdministratorAccountEntity>>() {
      @Override
      @NonNull
      public List<AdministratorAccountEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfLastLoginAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastLoginAt");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final List<AdministratorAccountEntity> _result = new ArrayList<AdministratorAccountEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AdministratorAccountEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpEmail;
            _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final Long _tmpLastLoginAt;
            if (_cursor.isNull(_cursorIndexOfLastLoginAt)) {
              _tmpLastLoginAt = null;
            } else {
              _tmpLastLoginAt = _cursor.getLong(_cursorIndexOfLastLoginAt);
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
            _item = new AdministratorAccountEntity(_tmpId,_tmpEmail,_tmpStatus,_tmpLastLoginAt,_tmpEntityId,_tmpCreatedAt,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
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
