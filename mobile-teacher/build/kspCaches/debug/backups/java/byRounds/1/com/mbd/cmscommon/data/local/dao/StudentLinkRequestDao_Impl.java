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
import com.mbd.cmscommon.data.local.entity.StudentLinkRequestEntity;
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
public final class StudentLinkRequestDao_Impl implements StudentLinkRequestDao {
  private final RoomDatabase __db;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  private final EntityUpsertionAdapter<StudentLinkRequestEntity> __upsertionAdapterOfStudentLinkRequestEntity;

  public StudentLinkRequestDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM student_link_requests WHERE requestId = ?";
        return _query;
      }
    };
    this.__upsertionAdapterOfStudentLinkRequestEntity = new EntityUpsertionAdapter<StudentLinkRequestEntity>(new EntityInsertionAdapter<StudentLinkRequestEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `student_link_requests` (`requestId`,`requestedByUid`,`sessionIdClaimed`,`rollNumberClaimed`,`nameClaimed`,`cnicClaimed`,`dobClaimed`,`universityRollClaimed`,`registrationNoClaimed`,`message`,`status`,`reviewedBy`,`reviewedAt`,`rejectionReason`,`attemptCount`,`createdAt`,`entityId`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final StudentLinkRequestEntity entity) {
        statement.bindString(1, entity.getRequestId());
        statement.bindString(2, entity.getRequestedByUid());
        if (entity.getSessionIdClaimed() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getSessionIdClaimed());
        }
        statement.bindString(4, entity.getRollNumberClaimed());
        if (entity.getNameClaimed() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getNameClaimed());
        }
        if (entity.getCnicClaimed() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getCnicClaimed());
        }
        if (entity.getDobClaimed() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getDobClaimed());
        }
        if (entity.getUniversityRollClaimed() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getUniversityRollClaimed());
        }
        if (entity.getRegistrationNoClaimed() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getRegistrationNoClaimed());
        }
        if (entity.getMessage() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getMessage());
        }
        statement.bindString(11, entity.getStatus());
        if (entity.getReviewedBy() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getReviewedBy());
        }
        if (entity.getReviewedAt() == null) {
          statement.bindNull(13);
        } else {
          statement.bindLong(13, entity.getReviewedAt());
        }
        if (entity.getRejectionReason() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getRejectionReason());
        }
        statement.bindLong(15, entity.getAttemptCount());
        statement.bindLong(16, entity.getCreatedAt());
        statement.bindLong(17, entity.getEntityId());
        if (entity.getCreatedBy() == null) {
          statement.bindNull(18);
        } else {
          statement.bindString(18, entity.getCreatedBy());
        }
        statement.bindLong(19, entity.getUpdatedAt());
        if (entity.getUpdatedBy() == null) {
          statement.bindNull(20);
        } else {
          statement.bindString(20, entity.getUpdatedBy());
        }
        final int _tmp = entity.isDeleted() ? 1 : 0;
        statement.bindLong(21, _tmp);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(22);
        } else {
          statement.bindLong(22, entity.getDeletedAt());
        }
        if (entity.getDeletedBy() == null) {
          statement.bindNull(23);
        } else {
          statement.bindString(23, entity.getDeletedBy());
        }
      }
    }, new EntityDeletionOrUpdateAdapter<StudentLinkRequestEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `student_link_requests` SET `requestId` = ?,`requestedByUid` = ?,`sessionIdClaimed` = ?,`rollNumberClaimed` = ?,`nameClaimed` = ?,`cnicClaimed` = ?,`dobClaimed` = ?,`universityRollClaimed` = ?,`registrationNoClaimed` = ?,`message` = ?,`status` = ?,`reviewedBy` = ?,`reviewedAt` = ?,`rejectionReason` = ?,`attemptCount` = ?,`createdAt` = ?,`entityId` = ?,`createdBy` = ?,`updatedAt` = ?,`updatedBy` = ?,`isDeleted` = ?,`deletedAt` = ?,`deletedBy` = ? WHERE `requestId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final StudentLinkRequestEntity entity) {
        statement.bindString(1, entity.getRequestId());
        statement.bindString(2, entity.getRequestedByUid());
        if (entity.getSessionIdClaimed() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getSessionIdClaimed());
        }
        statement.bindString(4, entity.getRollNumberClaimed());
        if (entity.getNameClaimed() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getNameClaimed());
        }
        if (entity.getCnicClaimed() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getCnicClaimed());
        }
        if (entity.getDobClaimed() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getDobClaimed());
        }
        if (entity.getUniversityRollClaimed() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getUniversityRollClaimed());
        }
        if (entity.getRegistrationNoClaimed() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getRegistrationNoClaimed());
        }
        if (entity.getMessage() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getMessage());
        }
        statement.bindString(11, entity.getStatus());
        if (entity.getReviewedBy() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getReviewedBy());
        }
        if (entity.getReviewedAt() == null) {
          statement.bindNull(13);
        } else {
          statement.bindLong(13, entity.getReviewedAt());
        }
        if (entity.getRejectionReason() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getRejectionReason());
        }
        statement.bindLong(15, entity.getAttemptCount());
        statement.bindLong(16, entity.getCreatedAt());
        statement.bindLong(17, entity.getEntityId());
        if (entity.getCreatedBy() == null) {
          statement.bindNull(18);
        } else {
          statement.bindString(18, entity.getCreatedBy());
        }
        statement.bindLong(19, entity.getUpdatedAt());
        if (entity.getUpdatedBy() == null) {
          statement.bindNull(20);
        } else {
          statement.bindString(20, entity.getUpdatedBy());
        }
        final int _tmp = entity.isDeleted() ? 1 : 0;
        statement.bindLong(21, _tmp);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(22);
        } else {
          statement.bindLong(22, entity.getDeletedAt());
        }
        if (entity.getDeletedBy() == null) {
          statement.bindNull(23);
        } else {
          statement.bindString(23, entity.getDeletedBy());
        }
        statement.bindString(24, entity.getRequestId());
      }
    });
  }

  @Override
  public Object applyDelta(final List<StudentLinkRequestEntity> active,
      final List<String> tombstoneIds, final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> StudentLinkRequestDao.DefaultImpls.applyDelta(StudentLinkRequestDao_Impl.this, active, tombstoneIds, __cont), $completion);
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
  public Object upsertAll(final List<StudentLinkRequestEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfStudentLinkRequestEntity.upsert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsert(final StudentLinkRequestEntity item,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfStudentLinkRequestEntity.upsert(item);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<StudentLinkRequestEntity>> observePending() {
    final String _sql = "SELECT * FROM student_link_requests WHERE status = 'PENDING' ORDER BY createdAt";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"student_link_requests"}, new Callable<List<StudentLinkRequestEntity>>() {
      @Override
      @NonNull
      public List<StudentLinkRequestEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfRequestId = CursorUtil.getColumnIndexOrThrow(_cursor, "requestId");
          final int _cursorIndexOfRequestedByUid = CursorUtil.getColumnIndexOrThrow(_cursor, "requestedByUid");
          final int _cursorIndexOfSessionIdClaimed = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionIdClaimed");
          final int _cursorIndexOfRollNumberClaimed = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNumberClaimed");
          final int _cursorIndexOfNameClaimed = CursorUtil.getColumnIndexOrThrow(_cursor, "nameClaimed");
          final int _cursorIndexOfCnicClaimed = CursorUtil.getColumnIndexOrThrow(_cursor, "cnicClaimed");
          final int _cursorIndexOfDobClaimed = CursorUtil.getColumnIndexOrThrow(_cursor, "dobClaimed");
          final int _cursorIndexOfUniversityRollClaimed = CursorUtil.getColumnIndexOrThrow(_cursor, "universityRollClaimed");
          final int _cursorIndexOfRegistrationNoClaimed = CursorUtil.getColumnIndexOrThrow(_cursor, "registrationNoClaimed");
          final int _cursorIndexOfMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "message");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfReviewedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewedBy");
          final int _cursorIndexOfReviewedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewedAt");
          final int _cursorIndexOfRejectionReason = CursorUtil.getColumnIndexOrThrow(_cursor, "rejectionReason");
          final int _cursorIndexOfAttemptCount = CursorUtil.getColumnIndexOrThrow(_cursor, "attemptCount");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final List<StudentLinkRequestEntity> _result = new ArrayList<StudentLinkRequestEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StudentLinkRequestEntity _item;
            final String _tmpRequestId;
            _tmpRequestId = _cursor.getString(_cursorIndexOfRequestId);
            final String _tmpRequestedByUid;
            _tmpRequestedByUid = _cursor.getString(_cursorIndexOfRequestedByUid);
            final String _tmpSessionIdClaimed;
            if (_cursor.isNull(_cursorIndexOfSessionIdClaimed)) {
              _tmpSessionIdClaimed = null;
            } else {
              _tmpSessionIdClaimed = _cursor.getString(_cursorIndexOfSessionIdClaimed);
            }
            final String _tmpRollNumberClaimed;
            _tmpRollNumberClaimed = _cursor.getString(_cursorIndexOfRollNumberClaimed);
            final String _tmpNameClaimed;
            if (_cursor.isNull(_cursorIndexOfNameClaimed)) {
              _tmpNameClaimed = null;
            } else {
              _tmpNameClaimed = _cursor.getString(_cursorIndexOfNameClaimed);
            }
            final String _tmpCnicClaimed;
            if (_cursor.isNull(_cursorIndexOfCnicClaimed)) {
              _tmpCnicClaimed = null;
            } else {
              _tmpCnicClaimed = _cursor.getString(_cursorIndexOfCnicClaimed);
            }
            final String _tmpDobClaimed;
            if (_cursor.isNull(_cursorIndexOfDobClaimed)) {
              _tmpDobClaimed = null;
            } else {
              _tmpDobClaimed = _cursor.getString(_cursorIndexOfDobClaimed);
            }
            final String _tmpUniversityRollClaimed;
            if (_cursor.isNull(_cursorIndexOfUniversityRollClaimed)) {
              _tmpUniversityRollClaimed = null;
            } else {
              _tmpUniversityRollClaimed = _cursor.getString(_cursorIndexOfUniversityRollClaimed);
            }
            final String _tmpRegistrationNoClaimed;
            if (_cursor.isNull(_cursorIndexOfRegistrationNoClaimed)) {
              _tmpRegistrationNoClaimed = null;
            } else {
              _tmpRegistrationNoClaimed = _cursor.getString(_cursorIndexOfRegistrationNoClaimed);
            }
            final String _tmpMessage;
            if (_cursor.isNull(_cursorIndexOfMessage)) {
              _tmpMessage = null;
            } else {
              _tmpMessage = _cursor.getString(_cursorIndexOfMessage);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpReviewedBy;
            if (_cursor.isNull(_cursorIndexOfReviewedBy)) {
              _tmpReviewedBy = null;
            } else {
              _tmpReviewedBy = _cursor.getString(_cursorIndexOfReviewedBy);
            }
            final Long _tmpReviewedAt;
            if (_cursor.isNull(_cursorIndexOfReviewedAt)) {
              _tmpReviewedAt = null;
            } else {
              _tmpReviewedAt = _cursor.getLong(_cursorIndexOfReviewedAt);
            }
            final String _tmpRejectionReason;
            if (_cursor.isNull(_cursorIndexOfRejectionReason)) {
              _tmpRejectionReason = null;
            } else {
              _tmpRejectionReason = _cursor.getString(_cursorIndexOfRejectionReason);
            }
            final int _tmpAttemptCount;
            _tmpAttemptCount = _cursor.getInt(_cursorIndexOfAttemptCount);
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
            _item = new StudentLinkRequestEntity(_tmpRequestId,_tmpRequestedByUid,_tmpSessionIdClaimed,_tmpRollNumberClaimed,_tmpNameClaimed,_tmpCnicClaimed,_tmpDobClaimed,_tmpUniversityRollClaimed,_tmpRegistrationNoClaimed,_tmpMessage,_tmpStatus,_tmpReviewedBy,_tmpReviewedAt,_tmpRejectionReason,_tmpAttemptCount,_tmpCreatedAt,_tmpEntityId,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
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
  public Flow<List<StudentLinkRequestEntity>> observeForRequester(final String uid) {
    final String _sql = "SELECT * FROM student_link_requests WHERE requestedByUid = ? ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, uid);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"student_link_requests"}, new Callable<List<StudentLinkRequestEntity>>() {
      @Override
      @NonNull
      public List<StudentLinkRequestEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfRequestId = CursorUtil.getColumnIndexOrThrow(_cursor, "requestId");
          final int _cursorIndexOfRequestedByUid = CursorUtil.getColumnIndexOrThrow(_cursor, "requestedByUid");
          final int _cursorIndexOfSessionIdClaimed = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionIdClaimed");
          final int _cursorIndexOfRollNumberClaimed = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNumberClaimed");
          final int _cursorIndexOfNameClaimed = CursorUtil.getColumnIndexOrThrow(_cursor, "nameClaimed");
          final int _cursorIndexOfCnicClaimed = CursorUtil.getColumnIndexOrThrow(_cursor, "cnicClaimed");
          final int _cursorIndexOfDobClaimed = CursorUtil.getColumnIndexOrThrow(_cursor, "dobClaimed");
          final int _cursorIndexOfUniversityRollClaimed = CursorUtil.getColumnIndexOrThrow(_cursor, "universityRollClaimed");
          final int _cursorIndexOfRegistrationNoClaimed = CursorUtil.getColumnIndexOrThrow(_cursor, "registrationNoClaimed");
          final int _cursorIndexOfMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "message");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfReviewedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewedBy");
          final int _cursorIndexOfReviewedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewedAt");
          final int _cursorIndexOfRejectionReason = CursorUtil.getColumnIndexOrThrow(_cursor, "rejectionReason");
          final int _cursorIndexOfAttemptCount = CursorUtil.getColumnIndexOrThrow(_cursor, "attemptCount");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final List<StudentLinkRequestEntity> _result = new ArrayList<StudentLinkRequestEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StudentLinkRequestEntity _item;
            final String _tmpRequestId;
            _tmpRequestId = _cursor.getString(_cursorIndexOfRequestId);
            final String _tmpRequestedByUid;
            _tmpRequestedByUid = _cursor.getString(_cursorIndexOfRequestedByUid);
            final String _tmpSessionIdClaimed;
            if (_cursor.isNull(_cursorIndexOfSessionIdClaimed)) {
              _tmpSessionIdClaimed = null;
            } else {
              _tmpSessionIdClaimed = _cursor.getString(_cursorIndexOfSessionIdClaimed);
            }
            final String _tmpRollNumberClaimed;
            _tmpRollNumberClaimed = _cursor.getString(_cursorIndexOfRollNumberClaimed);
            final String _tmpNameClaimed;
            if (_cursor.isNull(_cursorIndexOfNameClaimed)) {
              _tmpNameClaimed = null;
            } else {
              _tmpNameClaimed = _cursor.getString(_cursorIndexOfNameClaimed);
            }
            final String _tmpCnicClaimed;
            if (_cursor.isNull(_cursorIndexOfCnicClaimed)) {
              _tmpCnicClaimed = null;
            } else {
              _tmpCnicClaimed = _cursor.getString(_cursorIndexOfCnicClaimed);
            }
            final String _tmpDobClaimed;
            if (_cursor.isNull(_cursorIndexOfDobClaimed)) {
              _tmpDobClaimed = null;
            } else {
              _tmpDobClaimed = _cursor.getString(_cursorIndexOfDobClaimed);
            }
            final String _tmpUniversityRollClaimed;
            if (_cursor.isNull(_cursorIndexOfUniversityRollClaimed)) {
              _tmpUniversityRollClaimed = null;
            } else {
              _tmpUniversityRollClaimed = _cursor.getString(_cursorIndexOfUniversityRollClaimed);
            }
            final String _tmpRegistrationNoClaimed;
            if (_cursor.isNull(_cursorIndexOfRegistrationNoClaimed)) {
              _tmpRegistrationNoClaimed = null;
            } else {
              _tmpRegistrationNoClaimed = _cursor.getString(_cursorIndexOfRegistrationNoClaimed);
            }
            final String _tmpMessage;
            if (_cursor.isNull(_cursorIndexOfMessage)) {
              _tmpMessage = null;
            } else {
              _tmpMessage = _cursor.getString(_cursorIndexOfMessage);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpReviewedBy;
            if (_cursor.isNull(_cursorIndexOfReviewedBy)) {
              _tmpReviewedBy = null;
            } else {
              _tmpReviewedBy = _cursor.getString(_cursorIndexOfReviewedBy);
            }
            final Long _tmpReviewedAt;
            if (_cursor.isNull(_cursorIndexOfReviewedAt)) {
              _tmpReviewedAt = null;
            } else {
              _tmpReviewedAt = _cursor.getLong(_cursorIndexOfReviewedAt);
            }
            final String _tmpRejectionReason;
            if (_cursor.isNull(_cursorIndexOfRejectionReason)) {
              _tmpRejectionReason = null;
            } else {
              _tmpRejectionReason = _cursor.getString(_cursorIndexOfRejectionReason);
            }
            final int _tmpAttemptCount;
            _tmpAttemptCount = _cursor.getInt(_cursorIndexOfAttemptCount);
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
            _item = new StudentLinkRequestEntity(_tmpRequestId,_tmpRequestedByUid,_tmpSessionIdClaimed,_tmpRollNumberClaimed,_tmpNameClaimed,_tmpCnicClaimed,_tmpDobClaimed,_tmpUniversityRollClaimed,_tmpRegistrationNoClaimed,_tmpMessage,_tmpStatus,_tmpReviewedBy,_tmpReviewedAt,_tmpRejectionReason,_tmpAttemptCount,_tmpCreatedAt,_tmpEntityId,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
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
  public Object getById(final String requestId,
      final Continuation<? super StudentLinkRequestEntity> $completion) {
    final String _sql = "SELECT * FROM student_link_requests WHERE requestId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, requestId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<StudentLinkRequestEntity>() {
      @Override
      @Nullable
      public StudentLinkRequestEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfRequestId = CursorUtil.getColumnIndexOrThrow(_cursor, "requestId");
          final int _cursorIndexOfRequestedByUid = CursorUtil.getColumnIndexOrThrow(_cursor, "requestedByUid");
          final int _cursorIndexOfSessionIdClaimed = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionIdClaimed");
          final int _cursorIndexOfRollNumberClaimed = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNumberClaimed");
          final int _cursorIndexOfNameClaimed = CursorUtil.getColumnIndexOrThrow(_cursor, "nameClaimed");
          final int _cursorIndexOfCnicClaimed = CursorUtil.getColumnIndexOrThrow(_cursor, "cnicClaimed");
          final int _cursorIndexOfDobClaimed = CursorUtil.getColumnIndexOrThrow(_cursor, "dobClaimed");
          final int _cursorIndexOfUniversityRollClaimed = CursorUtil.getColumnIndexOrThrow(_cursor, "universityRollClaimed");
          final int _cursorIndexOfRegistrationNoClaimed = CursorUtil.getColumnIndexOrThrow(_cursor, "registrationNoClaimed");
          final int _cursorIndexOfMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "message");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfReviewedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewedBy");
          final int _cursorIndexOfReviewedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewedAt");
          final int _cursorIndexOfRejectionReason = CursorUtil.getColumnIndexOrThrow(_cursor, "rejectionReason");
          final int _cursorIndexOfAttemptCount = CursorUtil.getColumnIndexOrThrow(_cursor, "attemptCount");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final StudentLinkRequestEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpRequestId;
            _tmpRequestId = _cursor.getString(_cursorIndexOfRequestId);
            final String _tmpRequestedByUid;
            _tmpRequestedByUid = _cursor.getString(_cursorIndexOfRequestedByUid);
            final String _tmpSessionIdClaimed;
            if (_cursor.isNull(_cursorIndexOfSessionIdClaimed)) {
              _tmpSessionIdClaimed = null;
            } else {
              _tmpSessionIdClaimed = _cursor.getString(_cursorIndexOfSessionIdClaimed);
            }
            final String _tmpRollNumberClaimed;
            _tmpRollNumberClaimed = _cursor.getString(_cursorIndexOfRollNumberClaimed);
            final String _tmpNameClaimed;
            if (_cursor.isNull(_cursorIndexOfNameClaimed)) {
              _tmpNameClaimed = null;
            } else {
              _tmpNameClaimed = _cursor.getString(_cursorIndexOfNameClaimed);
            }
            final String _tmpCnicClaimed;
            if (_cursor.isNull(_cursorIndexOfCnicClaimed)) {
              _tmpCnicClaimed = null;
            } else {
              _tmpCnicClaimed = _cursor.getString(_cursorIndexOfCnicClaimed);
            }
            final String _tmpDobClaimed;
            if (_cursor.isNull(_cursorIndexOfDobClaimed)) {
              _tmpDobClaimed = null;
            } else {
              _tmpDobClaimed = _cursor.getString(_cursorIndexOfDobClaimed);
            }
            final String _tmpUniversityRollClaimed;
            if (_cursor.isNull(_cursorIndexOfUniversityRollClaimed)) {
              _tmpUniversityRollClaimed = null;
            } else {
              _tmpUniversityRollClaimed = _cursor.getString(_cursorIndexOfUniversityRollClaimed);
            }
            final String _tmpRegistrationNoClaimed;
            if (_cursor.isNull(_cursorIndexOfRegistrationNoClaimed)) {
              _tmpRegistrationNoClaimed = null;
            } else {
              _tmpRegistrationNoClaimed = _cursor.getString(_cursorIndexOfRegistrationNoClaimed);
            }
            final String _tmpMessage;
            if (_cursor.isNull(_cursorIndexOfMessage)) {
              _tmpMessage = null;
            } else {
              _tmpMessage = _cursor.getString(_cursorIndexOfMessage);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpReviewedBy;
            if (_cursor.isNull(_cursorIndexOfReviewedBy)) {
              _tmpReviewedBy = null;
            } else {
              _tmpReviewedBy = _cursor.getString(_cursorIndexOfReviewedBy);
            }
            final Long _tmpReviewedAt;
            if (_cursor.isNull(_cursorIndexOfReviewedAt)) {
              _tmpReviewedAt = null;
            } else {
              _tmpReviewedAt = _cursor.getLong(_cursorIndexOfReviewedAt);
            }
            final String _tmpRejectionReason;
            if (_cursor.isNull(_cursorIndexOfRejectionReason)) {
              _tmpRejectionReason = null;
            } else {
              _tmpRejectionReason = _cursor.getString(_cursorIndexOfRejectionReason);
            }
            final int _tmpAttemptCount;
            _tmpAttemptCount = _cursor.getInt(_cursorIndexOfAttemptCount);
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
            _result = new StudentLinkRequestEntity(_tmpRequestId,_tmpRequestedByUid,_tmpSessionIdClaimed,_tmpRollNumberClaimed,_tmpNameClaimed,_tmpCnicClaimed,_tmpDobClaimed,_tmpUniversityRollClaimed,_tmpRegistrationNoClaimed,_tmpMessage,_tmpStatus,_tmpReviewedBy,_tmpReviewedAt,_tmpRejectionReason,_tmpAttemptCount,_tmpCreatedAt,_tmpEntityId,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
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
        _stringBuilder.append("DELETE FROM student_link_requests WHERE requestId IN (");
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
