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
import com.mbd.cmscommon.data.local.entity.AcademicSessionEntity;
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
public final class AcademicSessionDao_Impl implements AcademicSessionDao {
  private final RoomDatabase __db;

  private final SharedSQLiteStatement __preparedStmtOfSetCurrentSemester;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteForDept;

  private final EntityUpsertionAdapter<AcademicSessionEntity> __upsertionAdapterOfAcademicSessionEntity;

  public AcademicSessionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__preparedStmtOfSetCurrentSemester = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE academic_sessions SET currentSemester = ? WHERE sessionId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM academic_sessions WHERE sessionId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteForDept = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM academic_sessions WHERE deptId = ?";
        return _query;
      }
    };
    this.__upsertionAdapterOfAcademicSessionEntity = new EntityUpsertionAdapter<AcademicSessionEntity>(new EntityInsertionAdapter<AcademicSessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `academic_sessions` (`sessionId`,`deptId`,`startYear`,`endYear`,`shift`,`currentSemester`,`isActive`,`programName`,`inchargeEmail`,`maxStudents`,`archivedAt`,`entityId`,`createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AcademicSessionEntity entity) {
        statement.bindString(1, entity.getSessionId());
        statement.bindString(2, entity.getDeptId());
        statement.bindLong(3, entity.getStartYear());
        statement.bindLong(4, entity.getEndYear());
        statement.bindString(5, entity.getShift());
        statement.bindLong(6, entity.getCurrentSemester());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(7, _tmp);
        if (entity.getProgramName() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getProgramName());
        }
        if (entity.getInchargeEmail() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getInchargeEmail());
        }
        statement.bindLong(10, entity.getMaxStudents());
        if (entity.getArchivedAt() == null) {
          statement.bindNull(11);
        } else {
          statement.bindLong(11, entity.getArchivedAt());
        }
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
        final int _tmp_1 = entity.isDeleted() ? 1 : 0;
        statement.bindLong(17, _tmp_1);
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
    }, new EntityDeletionOrUpdateAdapter<AcademicSessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `academic_sessions` SET `sessionId` = ?,`deptId` = ?,`startYear` = ?,`endYear` = ?,`shift` = ?,`currentSemester` = ?,`isActive` = ?,`programName` = ?,`inchargeEmail` = ?,`maxStudents` = ?,`archivedAt` = ?,`entityId` = ?,`createdAt` = ?,`createdBy` = ?,`updatedAt` = ?,`updatedBy` = ?,`isDeleted` = ?,`deletedAt` = ?,`deletedBy` = ? WHERE `sessionId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AcademicSessionEntity entity) {
        statement.bindString(1, entity.getSessionId());
        statement.bindString(2, entity.getDeptId());
        statement.bindLong(3, entity.getStartYear());
        statement.bindLong(4, entity.getEndYear());
        statement.bindString(5, entity.getShift());
        statement.bindLong(6, entity.getCurrentSemester());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(7, _tmp);
        if (entity.getProgramName() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getProgramName());
        }
        if (entity.getInchargeEmail() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getInchargeEmail());
        }
        statement.bindLong(10, entity.getMaxStudents());
        if (entity.getArchivedAt() == null) {
          statement.bindNull(11);
        } else {
          statement.bindLong(11, entity.getArchivedAt());
        }
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
        final int _tmp_1 = entity.isDeleted() ? 1 : 0;
        statement.bindLong(17, _tmp_1);
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
        statement.bindString(20, entity.getSessionId());
      }
    });
  }

  @Override
  public Object applyDelta(final List<AcademicSessionEntity> active,
      final List<String> tombstoneIds, final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> AcademicSessionDao.DefaultImpls.applyDelta(AcademicSessionDao_Impl.this, active, tombstoneIds, __cont), $completion);
  }

  @Override
  public Object setCurrentSemester(final String sessionId, final int semester,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSetCurrentSemester.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, semester);
        _argIndex = 2;
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
          __preparedStmtOfSetCurrentSemester.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteById(final String sessionId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
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
          __preparedStmtOfDeleteById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteForDept(final String deptId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteForDept.acquire();
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
          __preparedStmtOfDeleteForDept.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object upsert(final AcademicSessionEntity item,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfAcademicSessionEntity.upsert(item);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertAll(final List<AcademicSessionEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfAcademicSessionEntity.upsert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<AcademicSessionEntity>> observeForDept(final String deptId) {
    final String _sql = "SELECT * FROM academic_sessions WHERE deptId = ? AND isActive = 1 ORDER BY startYear DESC, shift";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, deptId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"academic_sessions"}, new Callable<List<AcademicSessionEntity>>() {
      @Override
      @NonNull
      public List<AcademicSessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfDeptId = CursorUtil.getColumnIndexOrThrow(_cursor, "deptId");
          final int _cursorIndexOfStartYear = CursorUtil.getColumnIndexOrThrow(_cursor, "startYear");
          final int _cursorIndexOfEndYear = CursorUtil.getColumnIndexOrThrow(_cursor, "endYear");
          final int _cursorIndexOfShift = CursorUtil.getColumnIndexOrThrow(_cursor, "shift");
          final int _cursorIndexOfCurrentSemester = CursorUtil.getColumnIndexOrThrow(_cursor, "currentSemester");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfProgramName = CursorUtil.getColumnIndexOrThrow(_cursor, "programName");
          final int _cursorIndexOfInchargeEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "inchargeEmail");
          final int _cursorIndexOfMaxStudents = CursorUtil.getColumnIndexOrThrow(_cursor, "maxStudents");
          final int _cursorIndexOfArchivedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "archivedAt");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final List<AcademicSessionEntity> _result = new ArrayList<AcademicSessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AcademicSessionEntity _item;
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpDeptId;
            _tmpDeptId = _cursor.getString(_cursorIndexOfDeptId);
            final int _tmpStartYear;
            _tmpStartYear = _cursor.getInt(_cursorIndexOfStartYear);
            final int _tmpEndYear;
            _tmpEndYear = _cursor.getInt(_cursorIndexOfEndYear);
            final String _tmpShift;
            _tmpShift = _cursor.getString(_cursorIndexOfShift);
            final int _tmpCurrentSemester;
            _tmpCurrentSemester = _cursor.getInt(_cursorIndexOfCurrentSemester);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final String _tmpProgramName;
            if (_cursor.isNull(_cursorIndexOfProgramName)) {
              _tmpProgramName = null;
            } else {
              _tmpProgramName = _cursor.getString(_cursorIndexOfProgramName);
            }
            final String _tmpInchargeEmail;
            if (_cursor.isNull(_cursorIndexOfInchargeEmail)) {
              _tmpInchargeEmail = null;
            } else {
              _tmpInchargeEmail = _cursor.getString(_cursorIndexOfInchargeEmail);
            }
            final int _tmpMaxStudents;
            _tmpMaxStudents = _cursor.getInt(_cursorIndexOfMaxStudents);
            final Long _tmpArchivedAt;
            if (_cursor.isNull(_cursorIndexOfArchivedAt)) {
              _tmpArchivedAt = null;
            } else {
              _tmpArchivedAt = _cursor.getLong(_cursorIndexOfArchivedAt);
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
            _item = new AcademicSessionEntity(_tmpSessionId,_tmpDeptId,_tmpStartYear,_tmpEndYear,_tmpShift,_tmpCurrentSemester,_tmpIsActive,_tmpProgramName,_tmpInchargeEmail,_tmpMaxStudents,_tmpArchivedAt,_tmpEntityId,_tmpCreatedAt,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
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
  public Flow<List<AcademicSessionEntity>> observeAll() {
    final String _sql = "SELECT * FROM academic_sessions WHERE isActive = 1 ORDER BY deptId, startYear DESC, shift";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"academic_sessions"}, new Callable<List<AcademicSessionEntity>>() {
      @Override
      @NonNull
      public List<AcademicSessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfDeptId = CursorUtil.getColumnIndexOrThrow(_cursor, "deptId");
          final int _cursorIndexOfStartYear = CursorUtil.getColumnIndexOrThrow(_cursor, "startYear");
          final int _cursorIndexOfEndYear = CursorUtil.getColumnIndexOrThrow(_cursor, "endYear");
          final int _cursorIndexOfShift = CursorUtil.getColumnIndexOrThrow(_cursor, "shift");
          final int _cursorIndexOfCurrentSemester = CursorUtil.getColumnIndexOrThrow(_cursor, "currentSemester");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfProgramName = CursorUtil.getColumnIndexOrThrow(_cursor, "programName");
          final int _cursorIndexOfInchargeEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "inchargeEmail");
          final int _cursorIndexOfMaxStudents = CursorUtil.getColumnIndexOrThrow(_cursor, "maxStudents");
          final int _cursorIndexOfArchivedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "archivedAt");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final List<AcademicSessionEntity> _result = new ArrayList<AcademicSessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AcademicSessionEntity _item;
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpDeptId;
            _tmpDeptId = _cursor.getString(_cursorIndexOfDeptId);
            final int _tmpStartYear;
            _tmpStartYear = _cursor.getInt(_cursorIndexOfStartYear);
            final int _tmpEndYear;
            _tmpEndYear = _cursor.getInt(_cursorIndexOfEndYear);
            final String _tmpShift;
            _tmpShift = _cursor.getString(_cursorIndexOfShift);
            final int _tmpCurrentSemester;
            _tmpCurrentSemester = _cursor.getInt(_cursorIndexOfCurrentSemester);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final String _tmpProgramName;
            if (_cursor.isNull(_cursorIndexOfProgramName)) {
              _tmpProgramName = null;
            } else {
              _tmpProgramName = _cursor.getString(_cursorIndexOfProgramName);
            }
            final String _tmpInchargeEmail;
            if (_cursor.isNull(_cursorIndexOfInchargeEmail)) {
              _tmpInchargeEmail = null;
            } else {
              _tmpInchargeEmail = _cursor.getString(_cursorIndexOfInchargeEmail);
            }
            final int _tmpMaxStudents;
            _tmpMaxStudents = _cursor.getInt(_cursorIndexOfMaxStudents);
            final Long _tmpArchivedAt;
            if (_cursor.isNull(_cursorIndexOfArchivedAt)) {
              _tmpArchivedAt = null;
            } else {
              _tmpArchivedAt = _cursor.getLong(_cursorIndexOfArchivedAt);
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
            _item = new AcademicSessionEntity(_tmpSessionId,_tmpDeptId,_tmpStartYear,_tmpEndYear,_tmpShift,_tmpCurrentSemester,_tmpIsActive,_tmpProgramName,_tmpInchargeEmail,_tmpMaxStudents,_tmpArchivedAt,_tmpEntityId,_tmpCreatedAt,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
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
  public Flow<AcademicSessionEntity> observeById(final String sessionId) {
    final String _sql = "SELECT * FROM academic_sessions WHERE sessionId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"academic_sessions"}, new Callable<AcademicSessionEntity>() {
      @Override
      @Nullable
      public AcademicSessionEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfDeptId = CursorUtil.getColumnIndexOrThrow(_cursor, "deptId");
          final int _cursorIndexOfStartYear = CursorUtil.getColumnIndexOrThrow(_cursor, "startYear");
          final int _cursorIndexOfEndYear = CursorUtil.getColumnIndexOrThrow(_cursor, "endYear");
          final int _cursorIndexOfShift = CursorUtil.getColumnIndexOrThrow(_cursor, "shift");
          final int _cursorIndexOfCurrentSemester = CursorUtil.getColumnIndexOrThrow(_cursor, "currentSemester");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfProgramName = CursorUtil.getColumnIndexOrThrow(_cursor, "programName");
          final int _cursorIndexOfInchargeEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "inchargeEmail");
          final int _cursorIndexOfMaxStudents = CursorUtil.getColumnIndexOrThrow(_cursor, "maxStudents");
          final int _cursorIndexOfArchivedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "archivedAt");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final AcademicSessionEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpDeptId;
            _tmpDeptId = _cursor.getString(_cursorIndexOfDeptId);
            final int _tmpStartYear;
            _tmpStartYear = _cursor.getInt(_cursorIndexOfStartYear);
            final int _tmpEndYear;
            _tmpEndYear = _cursor.getInt(_cursorIndexOfEndYear);
            final String _tmpShift;
            _tmpShift = _cursor.getString(_cursorIndexOfShift);
            final int _tmpCurrentSemester;
            _tmpCurrentSemester = _cursor.getInt(_cursorIndexOfCurrentSemester);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final String _tmpProgramName;
            if (_cursor.isNull(_cursorIndexOfProgramName)) {
              _tmpProgramName = null;
            } else {
              _tmpProgramName = _cursor.getString(_cursorIndexOfProgramName);
            }
            final String _tmpInchargeEmail;
            if (_cursor.isNull(_cursorIndexOfInchargeEmail)) {
              _tmpInchargeEmail = null;
            } else {
              _tmpInchargeEmail = _cursor.getString(_cursorIndexOfInchargeEmail);
            }
            final int _tmpMaxStudents;
            _tmpMaxStudents = _cursor.getInt(_cursorIndexOfMaxStudents);
            final Long _tmpArchivedAt;
            if (_cursor.isNull(_cursorIndexOfArchivedAt)) {
              _tmpArchivedAt = null;
            } else {
              _tmpArchivedAt = _cursor.getLong(_cursorIndexOfArchivedAt);
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
            _result = new AcademicSessionEntity(_tmpSessionId,_tmpDeptId,_tmpStartYear,_tmpEndYear,_tmpShift,_tmpCurrentSemester,_tmpIsActive,_tmpProgramName,_tmpInchargeEmail,_tmpMaxStudents,_tmpArchivedAt,_tmpEntityId,_tmpCreatedAt,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
          } else {
            _result = null;
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
  public Object getById(final String sessionId,
      final Continuation<? super AcademicSessionEntity> $completion) {
    final String _sql = "SELECT * FROM academic_sessions WHERE sessionId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<AcademicSessionEntity>() {
      @Override
      @Nullable
      public AcademicSessionEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfDeptId = CursorUtil.getColumnIndexOrThrow(_cursor, "deptId");
          final int _cursorIndexOfStartYear = CursorUtil.getColumnIndexOrThrow(_cursor, "startYear");
          final int _cursorIndexOfEndYear = CursorUtil.getColumnIndexOrThrow(_cursor, "endYear");
          final int _cursorIndexOfShift = CursorUtil.getColumnIndexOrThrow(_cursor, "shift");
          final int _cursorIndexOfCurrentSemester = CursorUtil.getColumnIndexOrThrow(_cursor, "currentSemester");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfProgramName = CursorUtil.getColumnIndexOrThrow(_cursor, "programName");
          final int _cursorIndexOfInchargeEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "inchargeEmail");
          final int _cursorIndexOfMaxStudents = CursorUtil.getColumnIndexOrThrow(_cursor, "maxStudents");
          final int _cursorIndexOfArchivedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "archivedAt");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final AcademicSessionEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpDeptId;
            _tmpDeptId = _cursor.getString(_cursorIndexOfDeptId);
            final int _tmpStartYear;
            _tmpStartYear = _cursor.getInt(_cursorIndexOfStartYear);
            final int _tmpEndYear;
            _tmpEndYear = _cursor.getInt(_cursorIndexOfEndYear);
            final String _tmpShift;
            _tmpShift = _cursor.getString(_cursorIndexOfShift);
            final int _tmpCurrentSemester;
            _tmpCurrentSemester = _cursor.getInt(_cursorIndexOfCurrentSemester);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final String _tmpProgramName;
            if (_cursor.isNull(_cursorIndexOfProgramName)) {
              _tmpProgramName = null;
            } else {
              _tmpProgramName = _cursor.getString(_cursorIndexOfProgramName);
            }
            final String _tmpInchargeEmail;
            if (_cursor.isNull(_cursorIndexOfInchargeEmail)) {
              _tmpInchargeEmail = null;
            } else {
              _tmpInchargeEmail = _cursor.getString(_cursorIndexOfInchargeEmail);
            }
            final int _tmpMaxStudents;
            _tmpMaxStudents = _cursor.getInt(_cursorIndexOfMaxStudents);
            final Long _tmpArchivedAt;
            if (_cursor.isNull(_cursorIndexOfArchivedAt)) {
              _tmpArchivedAt = null;
            } else {
              _tmpArchivedAt = _cursor.getLong(_cursorIndexOfArchivedAt);
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
            _result = new AcademicSessionEntity(_tmpSessionId,_tmpDeptId,_tmpStartYear,_tmpEndYear,_tmpShift,_tmpCurrentSemester,_tmpIsActive,_tmpProgramName,_tmpInchargeEmail,_tmpMaxStudents,_tmpArchivedAt,_tmpEntityId,_tmpCreatedAt,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
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
        _stringBuilder.append("DELETE FROM academic_sessions WHERE sessionId IN (");
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
