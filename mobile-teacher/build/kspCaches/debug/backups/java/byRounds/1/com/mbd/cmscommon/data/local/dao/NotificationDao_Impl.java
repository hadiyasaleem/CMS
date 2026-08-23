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
import com.mbd.cmscommon.data.local.entity.NotificationEntity;
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
public final class NotificationDao_Impl implements NotificationDao {
  private final RoomDatabase __db;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  private final EntityUpsertionAdapter<NotificationEntity> __upsertionAdapterOfNotificationEntity;

  public NotificationDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM notifications WHERE notificationId = ?";
        return _query;
      }
    };
    this.__upsertionAdapterOfNotificationEntity = new EntityUpsertionAdapter<NotificationEntity>(new EntityInsertionAdapter<NotificationEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `notifications` (`notificationId`,`title`,`body`,`targetRole`,`targetOfferingId`,`createdByUid`,`priority`,`targetDeptId`,`attachmentPath`,`expiresAt`,`createdAt`,`entityId`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final NotificationEntity entity) {
        statement.bindString(1, entity.getNotificationId());
        statement.bindString(2, entity.getTitle());
        statement.bindString(3, entity.getBody());
        if (entity.getTargetRole() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getTargetRole());
        }
        if (entity.getTargetOfferingId() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getTargetOfferingId());
        }
        statement.bindString(6, entity.getCreatedByUid());
        statement.bindString(7, entity.getPriority());
        if (entity.getTargetDeptId() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getTargetDeptId());
        }
        if (entity.getAttachmentPath() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getAttachmentPath());
        }
        if (entity.getExpiresAt() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getExpiresAt());
        }
        statement.bindLong(11, entity.getCreatedAt());
        statement.bindLong(12, entity.getEntityId());
        if (entity.getCreatedBy() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getCreatedBy());
        }
        statement.bindLong(14, entity.getUpdatedAt());
        if (entity.getUpdatedBy() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getUpdatedBy());
        }
        final int _tmp = entity.isDeleted() ? 1 : 0;
        statement.bindLong(16, _tmp);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(17);
        } else {
          statement.bindLong(17, entity.getDeletedAt());
        }
        if (entity.getDeletedBy() == null) {
          statement.bindNull(18);
        } else {
          statement.bindString(18, entity.getDeletedBy());
        }
      }
    }, new EntityDeletionOrUpdateAdapter<NotificationEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `notifications` SET `notificationId` = ?,`title` = ?,`body` = ?,`targetRole` = ?,`targetOfferingId` = ?,`createdByUid` = ?,`priority` = ?,`targetDeptId` = ?,`attachmentPath` = ?,`expiresAt` = ?,`createdAt` = ?,`entityId` = ?,`createdBy` = ?,`updatedAt` = ?,`updatedBy` = ?,`isDeleted` = ?,`deletedAt` = ?,`deletedBy` = ? WHERE `notificationId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final NotificationEntity entity) {
        statement.bindString(1, entity.getNotificationId());
        statement.bindString(2, entity.getTitle());
        statement.bindString(3, entity.getBody());
        if (entity.getTargetRole() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getTargetRole());
        }
        if (entity.getTargetOfferingId() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getTargetOfferingId());
        }
        statement.bindString(6, entity.getCreatedByUid());
        statement.bindString(7, entity.getPriority());
        if (entity.getTargetDeptId() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getTargetDeptId());
        }
        if (entity.getAttachmentPath() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getAttachmentPath());
        }
        if (entity.getExpiresAt() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getExpiresAt());
        }
        statement.bindLong(11, entity.getCreatedAt());
        statement.bindLong(12, entity.getEntityId());
        if (entity.getCreatedBy() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getCreatedBy());
        }
        statement.bindLong(14, entity.getUpdatedAt());
        if (entity.getUpdatedBy() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getUpdatedBy());
        }
        final int _tmp = entity.isDeleted() ? 1 : 0;
        statement.bindLong(16, _tmp);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(17);
        } else {
          statement.bindLong(17, entity.getDeletedAt());
        }
        if (entity.getDeletedBy() == null) {
          statement.bindNull(18);
        } else {
          statement.bindString(18, entity.getDeletedBy());
        }
        statement.bindString(19, entity.getNotificationId());
      }
    });
  }

  @Override
  public Object applyDelta(final List<NotificationEntity> activeRows,
      final List<String> tombstoneIds, final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> NotificationDao.DefaultImpls.applyDelta(NotificationDao_Impl.this, activeRows, tombstoneIds, __cont), $completion);
  }

  @Override
  public Object deleteById(final String notificationId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, notificationId);
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
  public Object upsertAll(final List<NotificationEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfNotificationEntity.upsert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<NotificationEntity>> observeForRole(final String role, final String sessionId,
      final String departmentId, final boolean includeAllScopes, final long nowMillis) {
    final String _sql = "SELECT * FROM notifications WHERE (targetRole = ? OR targetRole = 'ALL' OR targetRole IS NULL) AND (? OR targetOfferingId IS NULL OR targetOfferingId = ?) AND (? OR targetDeptId IS NULL OR targetDeptId = ?) AND (expiresAt IS NULL OR expiresAt > ?) ORDER BY createdAt DESC LIMIT 50";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 6);
    int _argIndex = 1;
    _statement.bindString(_argIndex, role);
    _argIndex = 2;
    final int _tmp = includeAllScopes ? 1 : 0;
    _statement.bindLong(_argIndex, _tmp);
    _argIndex = 3;
    if (sessionId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, sessionId);
    }
    _argIndex = 4;
    final int _tmp_1 = includeAllScopes ? 1 : 0;
    _statement.bindLong(_argIndex, _tmp_1);
    _argIndex = 5;
    if (departmentId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, departmentId);
    }
    _argIndex = 6;
    _statement.bindLong(_argIndex, nowMillis);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"notifications"}, new Callable<List<NotificationEntity>>() {
      @Override
      @NonNull
      public List<NotificationEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfNotificationId = CursorUtil.getColumnIndexOrThrow(_cursor, "notificationId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfBody = CursorUtil.getColumnIndexOrThrow(_cursor, "body");
          final int _cursorIndexOfTargetRole = CursorUtil.getColumnIndexOrThrow(_cursor, "targetRole");
          final int _cursorIndexOfTargetOfferingId = CursorUtil.getColumnIndexOrThrow(_cursor, "targetOfferingId");
          final int _cursorIndexOfCreatedByUid = CursorUtil.getColumnIndexOrThrow(_cursor, "createdByUid");
          final int _cursorIndexOfPriority = CursorUtil.getColumnIndexOrThrow(_cursor, "priority");
          final int _cursorIndexOfTargetDeptId = CursorUtil.getColumnIndexOrThrow(_cursor, "targetDeptId");
          final int _cursorIndexOfAttachmentPath = CursorUtil.getColumnIndexOrThrow(_cursor, "attachmentPath");
          final int _cursorIndexOfExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "expiresAt");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final List<NotificationEntity> _result = new ArrayList<NotificationEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final NotificationEntity _item;
            final String _tmpNotificationId;
            _tmpNotificationId = _cursor.getString(_cursorIndexOfNotificationId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpBody;
            _tmpBody = _cursor.getString(_cursorIndexOfBody);
            final String _tmpTargetRole;
            if (_cursor.isNull(_cursorIndexOfTargetRole)) {
              _tmpTargetRole = null;
            } else {
              _tmpTargetRole = _cursor.getString(_cursorIndexOfTargetRole);
            }
            final String _tmpTargetOfferingId;
            if (_cursor.isNull(_cursorIndexOfTargetOfferingId)) {
              _tmpTargetOfferingId = null;
            } else {
              _tmpTargetOfferingId = _cursor.getString(_cursorIndexOfTargetOfferingId);
            }
            final String _tmpCreatedByUid;
            _tmpCreatedByUid = _cursor.getString(_cursorIndexOfCreatedByUid);
            final String _tmpPriority;
            _tmpPriority = _cursor.getString(_cursorIndexOfPriority);
            final String _tmpTargetDeptId;
            if (_cursor.isNull(_cursorIndexOfTargetDeptId)) {
              _tmpTargetDeptId = null;
            } else {
              _tmpTargetDeptId = _cursor.getString(_cursorIndexOfTargetDeptId);
            }
            final String _tmpAttachmentPath;
            if (_cursor.isNull(_cursorIndexOfAttachmentPath)) {
              _tmpAttachmentPath = null;
            } else {
              _tmpAttachmentPath = _cursor.getString(_cursorIndexOfAttachmentPath);
            }
            final Long _tmpExpiresAt;
            if (_cursor.isNull(_cursorIndexOfExpiresAt)) {
              _tmpExpiresAt = null;
            } else {
              _tmpExpiresAt = _cursor.getLong(_cursorIndexOfExpiresAt);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpEntityId;
            _tmpEntityId = _cursor.getLong(_cursorIndexOfEntityId);
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
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_2 != 0;
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
            _item = new NotificationEntity(_tmpNotificationId,_tmpTitle,_tmpBody,_tmpTargetRole,_tmpTargetOfferingId,_tmpCreatedByUid,_tmpPriority,_tmpTargetDeptId,_tmpAttachmentPath,_tmpExpiresAt,_tmpCreatedAt,_tmpEntityId,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
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
  public Flow<List<NotificationEntity>> observeAuthoredBy(final String uid) {
    final String _sql = "SELECT * FROM notifications WHERE createdByUid = ? ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, uid);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"notifications"}, new Callable<List<NotificationEntity>>() {
      @Override
      @NonNull
      public List<NotificationEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfNotificationId = CursorUtil.getColumnIndexOrThrow(_cursor, "notificationId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfBody = CursorUtil.getColumnIndexOrThrow(_cursor, "body");
          final int _cursorIndexOfTargetRole = CursorUtil.getColumnIndexOrThrow(_cursor, "targetRole");
          final int _cursorIndexOfTargetOfferingId = CursorUtil.getColumnIndexOrThrow(_cursor, "targetOfferingId");
          final int _cursorIndexOfCreatedByUid = CursorUtil.getColumnIndexOrThrow(_cursor, "createdByUid");
          final int _cursorIndexOfPriority = CursorUtil.getColumnIndexOrThrow(_cursor, "priority");
          final int _cursorIndexOfTargetDeptId = CursorUtil.getColumnIndexOrThrow(_cursor, "targetDeptId");
          final int _cursorIndexOfAttachmentPath = CursorUtil.getColumnIndexOrThrow(_cursor, "attachmentPath");
          final int _cursorIndexOfExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "expiresAt");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final List<NotificationEntity> _result = new ArrayList<NotificationEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final NotificationEntity _item;
            final String _tmpNotificationId;
            _tmpNotificationId = _cursor.getString(_cursorIndexOfNotificationId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpBody;
            _tmpBody = _cursor.getString(_cursorIndexOfBody);
            final String _tmpTargetRole;
            if (_cursor.isNull(_cursorIndexOfTargetRole)) {
              _tmpTargetRole = null;
            } else {
              _tmpTargetRole = _cursor.getString(_cursorIndexOfTargetRole);
            }
            final String _tmpTargetOfferingId;
            if (_cursor.isNull(_cursorIndexOfTargetOfferingId)) {
              _tmpTargetOfferingId = null;
            } else {
              _tmpTargetOfferingId = _cursor.getString(_cursorIndexOfTargetOfferingId);
            }
            final String _tmpCreatedByUid;
            _tmpCreatedByUid = _cursor.getString(_cursorIndexOfCreatedByUid);
            final String _tmpPriority;
            _tmpPriority = _cursor.getString(_cursorIndexOfPriority);
            final String _tmpTargetDeptId;
            if (_cursor.isNull(_cursorIndexOfTargetDeptId)) {
              _tmpTargetDeptId = null;
            } else {
              _tmpTargetDeptId = _cursor.getString(_cursorIndexOfTargetDeptId);
            }
            final String _tmpAttachmentPath;
            if (_cursor.isNull(_cursorIndexOfAttachmentPath)) {
              _tmpAttachmentPath = null;
            } else {
              _tmpAttachmentPath = _cursor.getString(_cursorIndexOfAttachmentPath);
            }
            final Long _tmpExpiresAt;
            if (_cursor.isNull(_cursorIndexOfExpiresAt)) {
              _tmpExpiresAt = null;
            } else {
              _tmpExpiresAt = _cursor.getLong(_cursorIndexOfExpiresAt);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpEntityId;
            _tmpEntityId = _cursor.getLong(_cursorIndexOfEntityId);
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
            _item = new NotificationEntity(_tmpNotificationId,_tmpTitle,_tmpBody,_tmpTargetRole,_tmpTargetOfferingId,_tmpCreatedByUid,_tmpPriority,_tmpTargetDeptId,_tmpAttachmentPath,_tmpExpiresAt,_tmpCreatedAt,_tmpEntityId,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
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
  public Flow<Integer> observeUnreadCount(final String role, final String sessionId,
      final String departmentId, final boolean includeAllScopes, final long nowMillis,
      final long sinceMillis) {
    final String _sql = "SELECT COUNT(*) FROM notifications WHERE (targetRole = ? OR targetRole = 'ALL' OR targetRole IS NULL) AND (? OR targetOfferingId IS NULL OR targetOfferingId = ?) AND (? OR targetDeptId IS NULL OR targetDeptId = ?) AND (expiresAt IS NULL OR expiresAt > ?) AND createdAt > ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 7);
    int _argIndex = 1;
    _statement.bindString(_argIndex, role);
    _argIndex = 2;
    final int _tmp = includeAllScopes ? 1 : 0;
    _statement.bindLong(_argIndex, _tmp);
    _argIndex = 3;
    if (sessionId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, sessionId);
    }
    _argIndex = 4;
    final int _tmp_1 = includeAllScopes ? 1 : 0;
    _statement.bindLong(_argIndex, _tmp_1);
    _argIndex = 5;
    if (departmentId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, departmentId);
    }
    _argIndex = 6;
    _statement.bindLong(_argIndex, nowMillis);
    _argIndex = 7;
    _statement.bindLong(_argIndex, sinceMillis);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"notifications"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(0);
            _result = _tmp_2;
          } else {
            _result = 0;
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
  public Object deleteByIds(final List<String> notificationIds,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("DELETE FROM notifications WHERE notificationId IN (");
        final int _inputSize = notificationIds.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        for (String _item : notificationIds) {
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
