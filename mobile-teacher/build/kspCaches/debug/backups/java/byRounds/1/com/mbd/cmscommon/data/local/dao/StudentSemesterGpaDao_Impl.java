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
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.mbd.cmscommon.data.local.entity.StudentSemesterGpaEntity;
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
public final class StudentSemesterGpaDao_Impl implements StudentSemesterGpaDao {
  private final RoomDatabase __db;

  private final EntityUpsertionAdapter<StudentSemesterGpaEntity> __upsertionAdapterOfStudentSemesterGpaEntity;

  public StudentSemesterGpaDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__upsertionAdapterOfStudentSemesterGpaEntity = new EntityUpsertionAdapter<StudentSemesterGpaEntity>(new EntityInsertionAdapter<StudentSemesterGpaEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `student_semester_gpa` (`id`,`sessionId`,`rollNumber`,`semester`,`gpa`,`cgpa`,`termLabel`,`resultStatus`,`classPosition`,`remarks`,`supplyCoursesJson`,`entityId`,`createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final StudentSemesterGpaEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getSessionId());
        statement.bindString(3, entity.getRollNumber());
        statement.bindLong(4, entity.getSemester());
        statement.bindDouble(5, entity.getGpa());
        statement.bindDouble(6, entity.getCgpa());
        if (entity.getTermLabel() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getTermLabel());
        }
        statement.bindString(8, entity.getResultStatus());
        if (entity.getClassPosition() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getClassPosition());
        }
        if (entity.getRemarks() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getRemarks());
        }
        statement.bindString(11, entity.getSupplyCoursesJson());
        statement.bindLong(12, entity.getEntityId());
        statement.bindLong(13, entity.getCreatedAt());
        if (entity.getCreatedBy() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getCreatedBy());
        }
        statement.bindLong(15, entity.getUpdatedAt());
        if (entity.getUpdatedBy() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getUpdatedBy());
        }
        final int _tmp = entity.isDeleted() ? 1 : 0;
        statement.bindLong(17, _tmp);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(18);
        } else {
          statement.bindLong(18, entity.getDeletedAt());
        }
        if (entity.getDeletedBy() == null) {
          statement.bindNull(19);
        } else {
          statement.bindString(19, entity.getDeletedBy());
        }
      }
    }, new EntityDeletionOrUpdateAdapter<StudentSemesterGpaEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `student_semester_gpa` SET `id` = ?,`sessionId` = ?,`rollNumber` = ?,`semester` = ?,`gpa` = ?,`cgpa` = ?,`termLabel` = ?,`resultStatus` = ?,`classPosition` = ?,`remarks` = ?,`supplyCoursesJson` = ?,`entityId` = ?,`createdAt` = ?,`createdBy` = ?,`updatedAt` = ?,`updatedBy` = ?,`isDeleted` = ?,`deletedAt` = ?,`deletedBy` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final StudentSemesterGpaEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getSessionId());
        statement.bindString(3, entity.getRollNumber());
        statement.bindLong(4, entity.getSemester());
        statement.bindDouble(5, entity.getGpa());
        statement.bindDouble(6, entity.getCgpa());
        if (entity.getTermLabel() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getTermLabel());
        }
        statement.bindString(8, entity.getResultStatus());
        if (entity.getClassPosition() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getClassPosition());
        }
        if (entity.getRemarks() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getRemarks());
        }
        statement.bindString(11, entity.getSupplyCoursesJson());
        statement.bindLong(12, entity.getEntityId());
        statement.bindLong(13, entity.getCreatedAt());
        if (entity.getCreatedBy() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getCreatedBy());
        }
        statement.bindLong(15, entity.getUpdatedAt());
        if (entity.getUpdatedBy() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getUpdatedBy());
        }
        final int _tmp = entity.isDeleted() ? 1 : 0;
        statement.bindLong(17, _tmp);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(18);
        } else {
          statement.bindLong(18, entity.getDeletedAt());
        }
        if (entity.getDeletedBy() == null) {
          statement.bindNull(19);
        } else {
          statement.bindString(19, entity.getDeletedBy());
        }
        statement.bindString(20, entity.getId());
      }
    });
  }

  @Override
  public Object applyDelta(final List<StudentSemesterGpaEntity> active,
      final List<String> tombstoneIds, final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> StudentSemesterGpaDao.DefaultImpls.applyDelta(StudentSemesterGpaDao_Impl.this, active, tombstoneIds, __cont), $completion);
  }

  @Override
  public Object upsertAll(final List<StudentSemesterGpaEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfStudentSemesterGpaEntity.upsert(items);
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
      final Continuation<? super List<StudentSemesterGpaEntity>> $completion) {
    final String _sql = "SELECT * FROM student_semester_gpa WHERE sessionId = ? AND rollNumber = ? ORDER BY semester";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    _argIndex = 2;
    _statement.bindString(_argIndex, rollNumber);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<StudentSemesterGpaEntity>>() {
      @Override
      @NonNull
      public List<StudentSemesterGpaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfRollNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNumber");
          final int _cursorIndexOfSemester = CursorUtil.getColumnIndexOrThrow(_cursor, "semester");
          final int _cursorIndexOfGpa = CursorUtil.getColumnIndexOrThrow(_cursor, "gpa");
          final int _cursorIndexOfCgpa = CursorUtil.getColumnIndexOrThrow(_cursor, "cgpa");
          final int _cursorIndexOfTermLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "termLabel");
          final int _cursorIndexOfResultStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "resultStatus");
          final int _cursorIndexOfClassPosition = CursorUtil.getColumnIndexOrThrow(_cursor, "classPosition");
          final int _cursorIndexOfRemarks = CursorUtil.getColumnIndexOrThrow(_cursor, "remarks");
          final int _cursorIndexOfSupplyCoursesJson = CursorUtil.getColumnIndexOrThrow(_cursor, "supplyCoursesJson");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final List<StudentSemesterGpaEntity> _result = new ArrayList<StudentSemesterGpaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StudentSemesterGpaEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpRollNumber;
            _tmpRollNumber = _cursor.getString(_cursorIndexOfRollNumber);
            final int _tmpSemester;
            _tmpSemester = _cursor.getInt(_cursorIndexOfSemester);
            final double _tmpGpa;
            _tmpGpa = _cursor.getDouble(_cursorIndexOfGpa);
            final double _tmpCgpa;
            _tmpCgpa = _cursor.getDouble(_cursorIndexOfCgpa);
            final String _tmpTermLabel;
            if (_cursor.isNull(_cursorIndexOfTermLabel)) {
              _tmpTermLabel = null;
            } else {
              _tmpTermLabel = _cursor.getString(_cursorIndexOfTermLabel);
            }
            final String _tmpResultStatus;
            _tmpResultStatus = _cursor.getString(_cursorIndexOfResultStatus);
            final Integer _tmpClassPosition;
            if (_cursor.isNull(_cursorIndexOfClassPosition)) {
              _tmpClassPosition = null;
            } else {
              _tmpClassPosition = _cursor.getInt(_cursorIndexOfClassPosition);
            }
            final String _tmpRemarks;
            if (_cursor.isNull(_cursorIndexOfRemarks)) {
              _tmpRemarks = null;
            } else {
              _tmpRemarks = _cursor.getString(_cursorIndexOfRemarks);
            }
            final String _tmpSupplyCoursesJson;
            _tmpSupplyCoursesJson = _cursor.getString(_cursorIndexOfSupplyCoursesJson);
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
            _item = new StudentSemesterGpaEntity(_tmpId,_tmpSessionId,_tmpRollNumber,_tmpSemester,_tmpGpa,_tmpCgpa,_tmpTermLabel,_tmpResultStatus,_tmpClassPosition,_tmpRemarks,_tmpSupplyCoursesJson,_tmpEntityId,_tmpCreatedAt,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
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
  public Object getForSemester(final String sessionId, final int semester,
      final Continuation<? super List<StudentSemesterGpaEntity>> $completion) {
    final String _sql = "SELECT * FROM student_semester_gpa WHERE sessionId = ? AND semester = ? ORDER BY rollNumber";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, semester);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<StudentSemesterGpaEntity>>() {
      @Override
      @NonNull
      public List<StudentSemesterGpaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfRollNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNumber");
          final int _cursorIndexOfSemester = CursorUtil.getColumnIndexOrThrow(_cursor, "semester");
          final int _cursorIndexOfGpa = CursorUtil.getColumnIndexOrThrow(_cursor, "gpa");
          final int _cursorIndexOfCgpa = CursorUtil.getColumnIndexOrThrow(_cursor, "cgpa");
          final int _cursorIndexOfTermLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "termLabel");
          final int _cursorIndexOfResultStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "resultStatus");
          final int _cursorIndexOfClassPosition = CursorUtil.getColumnIndexOrThrow(_cursor, "classPosition");
          final int _cursorIndexOfRemarks = CursorUtil.getColumnIndexOrThrow(_cursor, "remarks");
          final int _cursorIndexOfSupplyCoursesJson = CursorUtil.getColumnIndexOrThrow(_cursor, "supplyCoursesJson");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final List<StudentSemesterGpaEntity> _result = new ArrayList<StudentSemesterGpaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StudentSemesterGpaEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpRollNumber;
            _tmpRollNumber = _cursor.getString(_cursorIndexOfRollNumber);
            final int _tmpSemester;
            _tmpSemester = _cursor.getInt(_cursorIndexOfSemester);
            final double _tmpGpa;
            _tmpGpa = _cursor.getDouble(_cursorIndexOfGpa);
            final double _tmpCgpa;
            _tmpCgpa = _cursor.getDouble(_cursorIndexOfCgpa);
            final String _tmpTermLabel;
            if (_cursor.isNull(_cursorIndexOfTermLabel)) {
              _tmpTermLabel = null;
            } else {
              _tmpTermLabel = _cursor.getString(_cursorIndexOfTermLabel);
            }
            final String _tmpResultStatus;
            _tmpResultStatus = _cursor.getString(_cursorIndexOfResultStatus);
            final Integer _tmpClassPosition;
            if (_cursor.isNull(_cursorIndexOfClassPosition)) {
              _tmpClassPosition = null;
            } else {
              _tmpClassPosition = _cursor.getInt(_cursorIndexOfClassPosition);
            }
            final String _tmpRemarks;
            if (_cursor.isNull(_cursorIndexOfRemarks)) {
              _tmpRemarks = null;
            } else {
              _tmpRemarks = _cursor.getString(_cursorIndexOfRemarks);
            }
            final String _tmpSupplyCoursesJson;
            _tmpSupplyCoursesJson = _cursor.getString(_cursorIndexOfSupplyCoursesJson);
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
            _item = new StudentSemesterGpaEntity(_tmpId,_tmpSessionId,_tmpRollNumber,_tmpSemester,_tmpGpa,_tmpCgpa,_tmpTermLabel,_tmpResultStatus,_tmpClassPosition,_tmpRemarks,_tmpSupplyCoursesJson,_tmpEntityId,_tmpCreatedAt,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
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
        _stringBuilder.append("DELETE FROM student_semester_gpa WHERE id IN (");
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
