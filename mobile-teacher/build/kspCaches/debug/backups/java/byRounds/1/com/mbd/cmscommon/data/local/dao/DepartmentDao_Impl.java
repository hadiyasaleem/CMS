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
import com.mbd.cmscommon.data.local.entity.DepartmentEntity;
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
public final class DepartmentDao_Impl implements DepartmentDao {
  private final RoomDatabase __db;

  private final EntityDeletionOrUpdateAdapter<DepartmentEntity> __deletionAdapterOfDepartmentEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  private final EntityUpsertionAdapter<DepartmentEntity> __upsertionAdapterOfDepartmentEntity;

  public DepartmentDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__deletionAdapterOfDepartmentEntity = new EntityDeletionOrUpdateAdapter<DepartmentEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `departments` WHERE `deptId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DepartmentEntity entity) {
        statement.bindString(1, entity.getDeptId());
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM departments WHERE deptId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM departments";
        return _query;
      }
    };
    this.__upsertionAdapterOfDepartmentEntity = new EntityUpsertionAdapter<DepartmentEntity>(new EntityInsertionAdapter<DepartmentEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `departments` (`deptId`,`entityId`,`name`,`code`,`hodEmail`,`description`,`isActive`,`archivedAt`,`createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DepartmentEntity entity) {
        statement.bindString(1, entity.getDeptId());
        statement.bindLong(2, entity.getEntityId());
        statement.bindString(3, entity.getName());
        statement.bindString(4, entity.getCode());
        if (entity.getHodEmail() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getHodEmail());
        }
        if (entity.getDescription() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getDescription());
        }
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(7, _tmp);
        if (entity.getArchivedAt() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getArchivedAt());
        }
        statement.bindLong(9, entity.getCreatedAt());
        statement.bindString(10, entity.getCreatedBy());
        statement.bindLong(11, entity.getUpdatedAt());
        statement.bindString(12, entity.getUpdatedBy());
        final int _tmp_1 = entity.isDeleted() ? 1 : 0;
        statement.bindLong(13, _tmp_1);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(14);
        } else {
          statement.bindLong(14, entity.getDeletedAt());
        }
        if (entity.getDeletedBy() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getDeletedBy());
        }
      }
    }, new EntityDeletionOrUpdateAdapter<DepartmentEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `departments` SET `deptId` = ?,`entityId` = ?,`name` = ?,`code` = ?,`hodEmail` = ?,`description` = ?,`isActive` = ?,`archivedAt` = ?,`createdAt` = ?,`createdBy` = ?,`updatedAt` = ?,`updatedBy` = ?,`isDeleted` = ?,`deletedAt` = ?,`deletedBy` = ? WHERE `deptId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DepartmentEntity entity) {
        statement.bindString(1, entity.getDeptId());
        statement.bindLong(2, entity.getEntityId());
        statement.bindString(3, entity.getName());
        statement.bindString(4, entity.getCode());
        if (entity.getHodEmail() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getHodEmail());
        }
        if (entity.getDescription() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getDescription());
        }
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(7, _tmp);
        if (entity.getArchivedAt() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getArchivedAt());
        }
        statement.bindLong(9, entity.getCreatedAt());
        statement.bindString(10, entity.getCreatedBy());
        statement.bindLong(11, entity.getUpdatedAt());
        statement.bindString(12, entity.getUpdatedBy());
        final int _tmp_1 = entity.isDeleted() ? 1 : 0;
        statement.bindLong(13, _tmp_1);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(14);
        } else {
          statement.bindLong(14, entity.getDeletedAt());
        }
        if (entity.getDeletedBy() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getDeletedBy());
        }
        statement.bindString(16, entity.getDeptId());
      }
    });
  }

  @Override
  public Object delete(final DepartmentEntity item, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfDepartmentEntity.handle(item);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object applyDelta(final List<DepartmentEntity> active, final List<String> tombstoneIds,
      final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> DepartmentDao.DefaultImpls.applyDelta(DepartmentDao_Impl.this, active, tombstoneIds, __cont), $completion);
  }

  @Override
  public Object deleteById(final String deptId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, deptId);
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
  public Object deleteAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
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
          __preparedStmtOfDeleteAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertAll(final List<DepartmentEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfDepartmentEntity.upsert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsert(final DepartmentEntity item, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfDepartmentEntity.upsert(item);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<DepartmentEntity>> observeActive() {
    final String _sql = "SELECT * FROM departments WHERE isActive = 1 ORDER BY name";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"departments"}, new Callable<List<DepartmentEntity>>() {
      @Override
      @NonNull
      public List<DepartmentEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDeptId = CursorUtil.getColumnIndexOrThrow(_cursor, "deptId");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCode = CursorUtil.getColumnIndexOrThrow(_cursor, "code");
          final int _cursorIndexOfHodEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "hodEmail");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfArchivedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "archivedAt");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final List<DepartmentEntity> _result = new ArrayList<DepartmentEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DepartmentEntity _item;
            final String _tmpDeptId;
            _tmpDeptId = _cursor.getString(_cursorIndexOfDeptId);
            final long _tmpEntityId;
            _tmpEntityId = _cursor.getLong(_cursorIndexOfEntityId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpCode;
            _tmpCode = _cursor.getString(_cursorIndexOfCode);
            final String _tmpHodEmail;
            if (_cursor.isNull(_cursorIndexOfHodEmail)) {
              _tmpHodEmail = null;
            } else {
              _tmpHodEmail = _cursor.getString(_cursorIndexOfHodEmail);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final Long _tmpArchivedAt;
            if (_cursor.isNull(_cursorIndexOfArchivedAt)) {
              _tmpArchivedAt = null;
            } else {
              _tmpArchivedAt = _cursor.getLong(_cursorIndexOfArchivedAt);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpCreatedBy;
            _tmpCreatedBy = _cursor.getString(_cursorIndexOfCreatedBy);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final String _tmpUpdatedBy;
            _tmpUpdatedBy = _cursor.getString(_cursorIndexOfUpdatedBy);
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
            _item = new DepartmentEntity(_tmpDeptId,_tmpEntityId,_tmpName,_tmpCode,_tmpHodEmail,_tmpDescription,_tmpIsActive,_tmpArchivedAt,_tmpCreatedAt,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
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
  public Object getById(final String deptId,
      final Continuation<? super DepartmentEntity> $completion) {
    final String _sql = "SELECT * FROM departments WHERE deptId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, deptId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<DepartmentEntity>() {
      @Override
      @Nullable
      public DepartmentEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDeptId = CursorUtil.getColumnIndexOrThrow(_cursor, "deptId");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCode = CursorUtil.getColumnIndexOrThrow(_cursor, "code");
          final int _cursorIndexOfHodEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "hodEmail");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfArchivedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "archivedAt");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final DepartmentEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpDeptId;
            _tmpDeptId = _cursor.getString(_cursorIndexOfDeptId);
            final long _tmpEntityId;
            _tmpEntityId = _cursor.getLong(_cursorIndexOfEntityId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpCode;
            _tmpCode = _cursor.getString(_cursorIndexOfCode);
            final String _tmpHodEmail;
            if (_cursor.isNull(_cursorIndexOfHodEmail)) {
              _tmpHodEmail = null;
            } else {
              _tmpHodEmail = _cursor.getString(_cursorIndexOfHodEmail);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final Long _tmpArchivedAt;
            if (_cursor.isNull(_cursorIndexOfArchivedAt)) {
              _tmpArchivedAt = null;
            } else {
              _tmpArchivedAt = _cursor.getLong(_cursorIndexOfArchivedAt);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpCreatedBy;
            _tmpCreatedBy = _cursor.getString(_cursorIndexOfCreatedBy);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final String _tmpUpdatedBy;
            _tmpUpdatedBy = _cursor.getString(_cursorIndexOfUpdatedBy);
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
            _result = new DepartmentEntity(_tmpDeptId,_tmpEntityId,_tmpName,_tmpCode,_tmpHodEmail,_tmpDescription,_tmpIsActive,_tmpArchivedAt,_tmpCreatedAt,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
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
  public Object deleteByIds(final List<String> ids, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("DELETE FROM departments WHERE deptId IN (");
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

  @Override
  public Object deleteNotIn(final List<String> ids, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("DELETE FROM departments WHERE deptId NOT IN (");
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
