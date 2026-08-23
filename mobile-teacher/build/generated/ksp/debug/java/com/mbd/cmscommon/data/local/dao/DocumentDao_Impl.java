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
import com.mbd.cmscommon.data.local.entity.DocumentEntity;
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
public final class DocumentDao_Impl implements DocumentDao {
  private final RoomDatabase __db;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  private final EntityUpsertionAdapter<DocumentEntity> __upsertionAdapterOfDocumentEntity;

  public DocumentDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM documents WHERE documentId = ?";
        return _query;
      }
    };
    this.__upsertionAdapterOfDocumentEntity = new EntityUpsertionAdapter<DocumentEntity>(new EntityInsertionAdapter<DocumentEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `documents` (`documentId`,`kind`,`title`,`storagePath`,`body`,`deptId`,`audience`,`tagsJson`,`published`,`publishedBy`,`entityId`,`createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DocumentEntity entity) {
        statement.bindString(1, entity.getDocumentId());
        statement.bindString(2, entity.getKind());
        statement.bindString(3, entity.getTitle());
        if (entity.getStoragePath() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getStoragePath());
        }
        if (entity.getBody() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getBody());
        }
        if (entity.getDeptId() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getDeptId());
        }
        statement.bindString(7, entity.getAudience());
        statement.bindString(8, entity.getTagsJson());
        final int _tmp = entity.getPublished() ? 1 : 0;
        statement.bindLong(9, _tmp);
        if (entity.getPublishedBy() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getPublishedBy());
        }
        statement.bindLong(11, entity.getEntityId());
        statement.bindLong(12, entity.getCreatedAt());
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
        final int _tmp_1 = entity.isDeleted() ? 1 : 0;
        statement.bindLong(16, _tmp_1);
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
    }, new EntityDeletionOrUpdateAdapter<DocumentEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `documents` SET `documentId` = ?,`kind` = ?,`title` = ?,`storagePath` = ?,`body` = ?,`deptId` = ?,`audience` = ?,`tagsJson` = ?,`published` = ?,`publishedBy` = ?,`entityId` = ?,`createdAt` = ?,`createdBy` = ?,`updatedAt` = ?,`updatedBy` = ?,`isDeleted` = ?,`deletedAt` = ?,`deletedBy` = ? WHERE `documentId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DocumentEntity entity) {
        statement.bindString(1, entity.getDocumentId());
        statement.bindString(2, entity.getKind());
        statement.bindString(3, entity.getTitle());
        if (entity.getStoragePath() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getStoragePath());
        }
        if (entity.getBody() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getBody());
        }
        if (entity.getDeptId() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getDeptId());
        }
        statement.bindString(7, entity.getAudience());
        statement.bindString(8, entity.getTagsJson());
        final int _tmp = entity.getPublished() ? 1 : 0;
        statement.bindLong(9, _tmp);
        if (entity.getPublishedBy() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getPublishedBy());
        }
        statement.bindLong(11, entity.getEntityId());
        statement.bindLong(12, entity.getCreatedAt());
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
        final int _tmp_1 = entity.isDeleted() ? 1 : 0;
        statement.bindLong(16, _tmp_1);
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
        statement.bindString(19, entity.getDocumentId());
      }
    });
  }

  @Override
  public Object applyDelta(final List<DocumentEntity> activeRows, final List<String> tombstoneIds,
      final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> DocumentDao.DefaultImpls.applyDelta(DocumentDao_Impl.this, activeRows, tombstoneIds, __cont), $completion);
  }

  @Override
  public Object deleteById(final String documentId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, documentId);
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
  public Object upsertAll(final List<DocumentEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfDocumentEntity.upsert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAll(final Continuation<? super List<DocumentEntity>> $completion) {
    final String _sql = "SELECT * FROM documents ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DocumentEntity>>() {
      @Override
      @NonNull
      public List<DocumentEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDocumentId = CursorUtil.getColumnIndexOrThrow(_cursor, "documentId");
          final int _cursorIndexOfKind = CursorUtil.getColumnIndexOrThrow(_cursor, "kind");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfStoragePath = CursorUtil.getColumnIndexOrThrow(_cursor, "storagePath");
          final int _cursorIndexOfBody = CursorUtil.getColumnIndexOrThrow(_cursor, "body");
          final int _cursorIndexOfDeptId = CursorUtil.getColumnIndexOrThrow(_cursor, "deptId");
          final int _cursorIndexOfAudience = CursorUtil.getColumnIndexOrThrow(_cursor, "audience");
          final int _cursorIndexOfTagsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "tagsJson");
          final int _cursorIndexOfPublished = CursorUtil.getColumnIndexOrThrow(_cursor, "published");
          final int _cursorIndexOfPublishedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "publishedBy");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final List<DocumentEntity> _result = new ArrayList<DocumentEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DocumentEntity _item;
            final String _tmpDocumentId;
            _tmpDocumentId = _cursor.getString(_cursorIndexOfDocumentId);
            final String _tmpKind;
            _tmpKind = _cursor.getString(_cursorIndexOfKind);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpStoragePath;
            if (_cursor.isNull(_cursorIndexOfStoragePath)) {
              _tmpStoragePath = null;
            } else {
              _tmpStoragePath = _cursor.getString(_cursorIndexOfStoragePath);
            }
            final String _tmpBody;
            if (_cursor.isNull(_cursorIndexOfBody)) {
              _tmpBody = null;
            } else {
              _tmpBody = _cursor.getString(_cursorIndexOfBody);
            }
            final String _tmpDeptId;
            if (_cursor.isNull(_cursorIndexOfDeptId)) {
              _tmpDeptId = null;
            } else {
              _tmpDeptId = _cursor.getString(_cursorIndexOfDeptId);
            }
            final String _tmpAudience;
            _tmpAudience = _cursor.getString(_cursorIndexOfAudience);
            final String _tmpTagsJson;
            _tmpTagsJson = _cursor.getString(_cursorIndexOfTagsJson);
            final boolean _tmpPublished;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfPublished);
            _tmpPublished = _tmp != 0;
            final String _tmpPublishedBy;
            if (_cursor.isNull(_cursorIndexOfPublishedBy)) {
              _tmpPublishedBy = null;
            } else {
              _tmpPublishedBy = _cursor.getString(_cursorIndexOfPublishedBy);
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
            _item = new DocumentEntity(_tmpDocumentId,_tmpKind,_tmpTitle,_tmpStoragePath,_tmpBody,_tmpDeptId,_tmpAudience,_tmpTagsJson,_tmpPublished,_tmpPublishedBy,_tmpEntityId,_tmpCreatedAt,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
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
  public Object getById(final String documentId,
      final Continuation<? super DocumentEntity> $completion) {
    final String _sql = "SELECT * FROM documents WHERE documentId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, documentId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<DocumentEntity>() {
      @Override
      @Nullable
      public DocumentEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDocumentId = CursorUtil.getColumnIndexOrThrow(_cursor, "documentId");
          final int _cursorIndexOfKind = CursorUtil.getColumnIndexOrThrow(_cursor, "kind");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfStoragePath = CursorUtil.getColumnIndexOrThrow(_cursor, "storagePath");
          final int _cursorIndexOfBody = CursorUtil.getColumnIndexOrThrow(_cursor, "body");
          final int _cursorIndexOfDeptId = CursorUtil.getColumnIndexOrThrow(_cursor, "deptId");
          final int _cursorIndexOfAudience = CursorUtil.getColumnIndexOrThrow(_cursor, "audience");
          final int _cursorIndexOfTagsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "tagsJson");
          final int _cursorIndexOfPublished = CursorUtil.getColumnIndexOrThrow(_cursor, "published");
          final int _cursorIndexOfPublishedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "publishedBy");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final DocumentEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpDocumentId;
            _tmpDocumentId = _cursor.getString(_cursorIndexOfDocumentId);
            final String _tmpKind;
            _tmpKind = _cursor.getString(_cursorIndexOfKind);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpStoragePath;
            if (_cursor.isNull(_cursorIndexOfStoragePath)) {
              _tmpStoragePath = null;
            } else {
              _tmpStoragePath = _cursor.getString(_cursorIndexOfStoragePath);
            }
            final String _tmpBody;
            if (_cursor.isNull(_cursorIndexOfBody)) {
              _tmpBody = null;
            } else {
              _tmpBody = _cursor.getString(_cursorIndexOfBody);
            }
            final String _tmpDeptId;
            if (_cursor.isNull(_cursorIndexOfDeptId)) {
              _tmpDeptId = null;
            } else {
              _tmpDeptId = _cursor.getString(_cursorIndexOfDeptId);
            }
            final String _tmpAudience;
            _tmpAudience = _cursor.getString(_cursorIndexOfAudience);
            final String _tmpTagsJson;
            _tmpTagsJson = _cursor.getString(_cursorIndexOfTagsJson);
            final boolean _tmpPublished;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfPublished);
            _tmpPublished = _tmp != 0;
            final String _tmpPublishedBy;
            if (_cursor.isNull(_cursorIndexOfPublishedBy)) {
              _tmpPublishedBy = null;
            } else {
              _tmpPublishedBy = _cursor.getString(_cursorIndexOfPublishedBy);
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
            _result = new DocumentEntity(_tmpDocumentId,_tmpKind,_tmpTitle,_tmpStoragePath,_tmpBody,_tmpDeptId,_tmpAudience,_tmpTagsJson,_tmpPublished,_tmpPublishedBy,_tmpEntityId,_tmpCreatedAt,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
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
  public Object deleteByIds(final List<String> documentIds,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("DELETE FROM documents WHERE documentId IN (");
        final int _inputSize = documentIds.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        for (String _item : documentIds) {
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
