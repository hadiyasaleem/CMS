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
import com.mbd.cmscommon.data.local.entity.ExamPaperSubmissionEntity;
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
public final class ExamPaperSubmissionDao_Impl implements ExamPaperSubmissionDao {
  private final RoomDatabase __db;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllForOffering;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllForSubject;

  private final EntityUpsertionAdapter<ExamPaperSubmissionEntity> __upsertionAdapterOfExamPaperSubmissionEntity;

  public ExamPaperSubmissionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM exam_paper_submissions WHERE submissionId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAllForOffering = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM exam_paper_submissions WHERE offeringId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAllForSubject = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM exam_paper_submissions WHERE subjectId = ?";
        return _query;
      }
    };
    this.__upsertionAdapterOfExamPaperSubmissionEntity = new EntityUpsertionAdapter<ExamPaperSubmissionEntity>(new EntityInsertionAdapter<ExamPaperSubmissionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `exam_paper_submissions` (`submissionId`,`offeringId`,`subjectId`,`examType`,`teacherId`,`storagePath`,`fileName`,`uploadedAt`,`createdBy`,`entityId`,`createdAt`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ExamPaperSubmissionEntity entity) {
        statement.bindString(1, entity.getSubmissionId());
        statement.bindString(2, entity.getOfferingId());
        statement.bindString(3, entity.getSubjectId());
        statement.bindString(4, entity.getExamType());
        statement.bindString(5, entity.getTeacherId());
        statement.bindString(6, entity.getStoragePath());
        statement.bindString(7, entity.getFileName());
        statement.bindLong(8, entity.getUploadedAt());
        statement.bindString(9, entity.getCreatedBy());
        statement.bindLong(10, entity.getEntityId());
        statement.bindLong(11, entity.getCreatedAt());
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
    }, new EntityDeletionOrUpdateAdapter<ExamPaperSubmissionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `exam_paper_submissions` SET `submissionId` = ?,`offeringId` = ?,`subjectId` = ?,`examType` = ?,`teacherId` = ?,`storagePath` = ?,`fileName` = ?,`uploadedAt` = ?,`createdBy` = ?,`entityId` = ?,`createdAt` = ?,`updatedAt` = ?,`updatedBy` = ?,`isDeleted` = ?,`deletedAt` = ?,`deletedBy` = ? WHERE `submissionId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ExamPaperSubmissionEntity entity) {
        statement.bindString(1, entity.getSubmissionId());
        statement.bindString(2, entity.getOfferingId());
        statement.bindString(3, entity.getSubjectId());
        statement.bindString(4, entity.getExamType());
        statement.bindString(5, entity.getTeacherId());
        statement.bindString(6, entity.getStoragePath());
        statement.bindString(7, entity.getFileName());
        statement.bindLong(8, entity.getUploadedAt());
        statement.bindString(9, entity.getCreatedBy());
        statement.bindLong(10, entity.getEntityId());
        statement.bindLong(11, entity.getCreatedAt());
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
        statement.bindString(17, entity.getSubmissionId());
      }
    });
  }

  @Override
  public Object applyDelta(final List<ExamPaperSubmissionEntity> activeRows,
      final List<String> tombstoneIds, final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> ExamPaperSubmissionDao.DefaultImpls.applyDelta(ExamPaperSubmissionDao_Impl.this, activeRows, tombstoneIds, __cont), $completion);
  }

  @Override
  public Object deleteById(final String submissionId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, submissionId);
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
  public Object deleteAllForOffering(final String offeringId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllForOffering.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, offeringId);
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
          __preparedStmtOfDeleteAllForOffering.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllForSubject(final String subjectId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllForSubject.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, subjectId);
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
          __preparedStmtOfDeleteAllForSubject.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertAll(final List<ExamPaperSubmissionEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfExamPaperSubmissionEntity.upsert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ExamPaperSubmissionEntity>> observeForOffering(final String offeringId,
      final String subjectId) {
    final String _sql = "SELECT * FROM exam_paper_submissions WHERE offeringId = ? AND subjectId = ? ORDER BY uploadedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, offeringId);
    _argIndex = 2;
    _statement.bindString(_argIndex, subjectId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"exam_paper_submissions"}, new Callable<List<ExamPaperSubmissionEntity>>() {
      @Override
      @NonNull
      public List<ExamPaperSubmissionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSubmissionId = CursorUtil.getColumnIndexOrThrow(_cursor, "submissionId");
          final int _cursorIndexOfOfferingId = CursorUtil.getColumnIndexOrThrow(_cursor, "offeringId");
          final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
          final int _cursorIndexOfExamType = CursorUtil.getColumnIndexOrThrow(_cursor, "examType");
          final int _cursorIndexOfTeacherId = CursorUtil.getColumnIndexOrThrow(_cursor, "teacherId");
          final int _cursorIndexOfStoragePath = CursorUtil.getColumnIndexOrThrow(_cursor, "storagePath");
          final int _cursorIndexOfFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "fileName");
          final int _cursorIndexOfUploadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadedAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final List<ExamPaperSubmissionEntity> _result = new ArrayList<ExamPaperSubmissionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ExamPaperSubmissionEntity _item;
            final String _tmpSubmissionId;
            _tmpSubmissionId = _cursor.getString(_cursorIndexOfSubmissionId);
            final String _tmpOfferingId;
            _tmpOfferingId = _cursor.getString(_cursorIndexOfOfferingId);
            final String _tmpSubjectId;
            _tmpSubjectId = _cursor.getString(_cursorIndexOfSubjectId);
            final String _tmpExamType;
            _tmpExamType = _cursor.getString(_cursorIndexOfExamType);
            final String _tmpTeacherId;
            _tmpTeacherId = _cursor.getString(_cursorIndexOfTeacherId);
            final String _tmpStoragePath;
            _tmpStoragePath = _cursor.getString(_cursorIndexOfStoragePath);
            final String _tmpFileName;
            _tmpFileName = _cursor.getString(_cursorIndexOfFileName);
            final long _tmpUploadedAt;
            _tmpUploadedAt = _cursor.getLong(_cursorIndexOfUploadedAt);
            final String _tmpCreatedBy;
            _tmpCreatedBy = _cursor.getString(_cursorIndexOfCreatedBy);
            final long _tmpEntityId;
            _tmpEntityId = _cursor.getLong(_cursorIndexOfEntityId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
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
            _item = new ExamPaperSubmissionEntity(_tmpSubmissionId,_tmpOfferingId,_tmpSubjectId,_tmpExamType,_tmpTeacherId,_tmpStoragePath,_tmpFileName,_tmpUploadedAt,_tmpCreatedBy,_tmpEntityId,_tmpCreatedAt,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
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
  public Object getById(final String submissionId,
      final Continuation<? super ExamPaperSubmissionEntity> $completion) {
    final String _sql = "SELECT * FROM exam_paper_submissions WHERE submissionId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, submissionId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ExamPaperSubmissionEntity>() {
      @Override
      @Nullable
      public ExamPaperSubmissionEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSubmissionId = CursorUtil.getColumnIndexOrThrow(_cursor, "submissionId");
          final int _cursorIndexOfOfferingId = CursorUtil.getColumnIndexOrThrow(_cursor, "offeringId");
          final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
          final int _cursorIndexOfExamType = CursorUtil.getColumnIndexOrThrow(_cursor, "examType");
          final int _cursorIndexOfTeacherId = CursorUtil.getColumnIndexOrThrow(_cursor, "teacherId");
          final int _cursorIndexOfStoragePath = CursorUtil.getColumnIndexOrThrow(_cursor, "storagePath");
          final int _cursorIndexOfFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "fileName");
          final int _cursorIndexOfUploadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadedAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final ExamPaperSubmissionEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpSubmissionId;
            _tmpSubmissionId = _cursor.getString(_cursorIndexOfSubmissionId);
            final String _tmpOfferingId;
            _tmpOfferingId = _cursor.getString(_cursorIndexOfOfferingId);
            final String _tmpSubjectId;
            _tmpSubjectId = _cursor.getString(_cursorIndexOfSubjectId);
            final String _tmpExamType;
            _tmpExamType = _cursor.getString(_cursorIndexOfExamType);
            final String _tmpTeacherId;
            _tmpTeacherId = _cursor.getString(_cursorIndexOfTeacherId);
            final String _tmpStoragePath;
            _tmpStoragePath = _cursor.getString(_cursorIndexOfStoragePath);
            final String _tmpFileName;
            _tmpFileName = _cursor.getString(_cursorIndexOfFileName);
            final long _tmpUploadedAt;
            _tmpUploadedAt = _cursor.getLong(_cursorIndexOfUploadedAt);
            final String _tmpCreatedBy;
            _tmpCreatedBy = _cursor.getString(_cursorIndexOfCreatedBy);
            final long _tmpEntityId;
            _tmpEntityId = _cursor.getLong(_cursorIndexOfEntityId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
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
            _result = new ExamPaperSubmissionEntity(_tmpSubmissionId,_tmpOfferingId,_tmpSubjectId,_tmpExamType,_tmpTeacherId,_tmpStoragePath,_tmpFileName,_tmpUploadedAt,_tmpCreatedBy,_tmpEntityId,_tmpCreatedAt,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
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
  public Object deleteByIds(final List<String> submissionIds,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("DELETE FROM exam_paper_submissions WHERE submissionId IN (");
        final int _inputSize = submissionIds.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        for (String _item : submissionIds) {
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
