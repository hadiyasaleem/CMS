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
import androidx.sqlite.db.SupportSQLiteStatement;
import com.mbd.cmscommon.data.local.entity.InsightAtRiskStudentEntity;
import com.mbd.cmscommon.data.local.entity.InsightExamStatEntity;
import com.mbd.cmscommon.data.local.entity.InsightSessionOverviewEntity;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Integer;
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

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class InsightsDao_Impl implements InsightsDao {
  private final RoomDatabase __db;

  private final SharedSQLiteStatement __preparedStmtOfClearSessionOverviews;

  private final SharedSQLiteStatement __preparedStmtOfClearAtRiskStudents;

  private final SharedSQLiteStatement __preparedStmtOfClearExamStats;

  private final EntityUpsertionAdapter<InsightSessionOverviewEntity> __upsertionAdapterOfInsightSessionOverviewEntity;

  private final EntityUpsertionAdapter<InsightAtRiskStudentEntity> __upsertionAdapterOfInsightAtRiskStudentEntity;

  private final EntityUpsertionAdapter<InsightExamStatEntity> __upsertionAdapterOfInsightExamStatEntity;

  public InsightsDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__preparedStmtOfClearSessionOverviews = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM insight_session_overviews";
        return _query;
      }
    };
    this.__preparedStmtOfClearAtRiskStudents = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM insight_at_risk_students";
        return _query;
      }
    };
    this.__preparedStmtOfClearExamStats = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM insight_exam_stats";
        return _query;
      }
    };
    this.__upsertionAdapterOfInsightSessionOverviewEntity = new EntityUpsertionAdapter<InsightSessionOverviewEntity>(new EntityInsertionAdapter<InsightSessionOverviewEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `insight_session_overviews` (`sessionId`,`deptId`,`shift`,`currentSemester`,`students`,`avgCgpa`,`avgAttendance`,`cachedAt`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final InsightSessionOverviewEntity entity) {
        statement.bindString(1, entity.getSessionId());
        statement.bindString(2, entity.getDeptId());
        statement.bindString(3, entity.getShift());
        statement.bindLong(4, entity.getCurrentSemester());
        statement.bindLong(5, entity.getStudents());
        if (entity.getAvgCgpa() == null) {
          statement.bindNull(6);
        } else {
          statement.bindDouble(6, entity.getAvgCgpa());
        }
        if (entity.getAvgAttendance() == null) {
          statement.bindNull(7);
        } else {
          statement.bindDouble(7, entity.getAvgAttendance());
        }
        statement.bindLong(8, entity.getCachedAt());
      }
    }, new EntityDeletionOrUpdateAdapter<InsightSessionOverviewEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `insight_session_overviews` SET `sessionId` = ?,`deptId` = ?,`shift` = ?,`currentSemester` = ?,`students` = ?,`avgCgpa` = ?,`avgAttendance` = ?,`cachedAt` = ? WHERE `sessionId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final InsightSessionOverviewEntity entity) {
        statement.bindString(1, entity.getSessionId());
        statement.bindString(2, entity.getDeptId());
        statement.bindString(3, entity.getShift());
        statement.bindLong(4, entity.getCurrentSemester());
        statement.bindLong(5, entity.getStudents());
        if (entity.getAvgCgpa() == null) {
          statement.bindNull(6);
        } else {
          statement.bindDouble(6, entity.getAvgCgpa());
        }
        if (entity.getAvgAttendance() == null) {
          statement.bindNull(7);
        } else {
          statement.bindDouble(7, entity.getAvgAttendance());
        }
        statement.bindLong(8, entity.getCachedAt());
        statement.bindString(9, entity.getSessionId());
      }
    });
    this.__upsertionAdapterOfInsightAtRiskStudentEntity = new EntityUpsertionAdapter<InsightAtRiskStudentEntity>(new EntityInsertionAdapter<InsightAtRiskStudentEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `insight_at_risk_students` (`id`,`sessionId`,`rollNumber`,`name`,`cgpa`,`attendance`,`cachedAt`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final InsightAtRiskStudentEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getSessionId());
        statement.bindString(3, entity.getRollNumber());
        statement.bindString(4, entity.getName());
        if (entity.getCgpa() == null) {
          statement.bindNull(5);
        } else {
          statement.bindDouble(5, entity.getCgpa());
        }
        if (entity.getAttendance() == null) {
          statement.bindNull(6);
        } else {
          statement.bindDouble(6, entity.getAttendance());
        }
        statement.bindLong(7, entity.getCachedAt());
      }
    }, new EntityDeletionOrUpdateAdapter<InsightAtRiskStudentEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `insight_at_risk_students` SET `id` = ?,`sessionId` = ?,`rollNumber` = ?,`name` = ?,`cgpa` = ?,`attendance` = ?,`cachedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final InsightAtRiskStudentEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getSessionId());
        statement.bindString(3, entity.getRollNumber());
        statement.bindString(4, entity.getName());
        if (entity.getCgpa() == null) {
          statement.bindNull(5);
        } else {
          statement.bindDouble(5, entity.getCgpa());
        }
        if (entity.getAttendance() == null) {
          statement.bindNull(6);
        } else {
          statement.bindDouble(6, entity.getAttendance());
        }
        statement.bindLong(7, entity.getCachedAt());
        statement.bindString(8, entity.getId());
      }
    });
    this.__upsertionAdapterOfInsightExamStatEntity = new EntityUpsertionAdapter<InsightExamStatEntity>(new EntityInsertionAdapter<InsightExamStatEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `insight_exam_stats` (`id`,`sessionId`,`semester`,`courseCode`,`examType`,`entered`,`avgScore`,`minScore`,`maxScore`,`stddev`,`outOf`,`passRate`,`cachedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final InsightExamStatEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getSessionId());
        statement.bindLong(3, entity.getSemester());
        statement.bindString(4, entity.getCourseCode());
        statement.bindString(5, entity.getExamType());
        statement.bindLong(6, entity.getEntered());
        if (entity.getAvgScore() == null) {
          statement.bindNull(7);
        } else {
          statement.bindDouble(7, entity.getAvgScore());
        }
        if (entity.getMinScore() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getMinScore());
        }
        if (entity.getMaxScore() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getMaxScore());
        }
        if (entity.getStddev() == null) {
          statement.bindNull(10);
        } else {
          statement.bindDouble(10, entity.getStddev());
        }
        statement.bindLong(11, entity.getOutOf());
        if (entity.getPassRate() == null) {
          statement.bindNull(12);
        } else {
          statement.bindDouble(12, entity.getPassRate());
        }
        statement.bindLong(13, entity.getCachedAt());
      }
    }, new EntityDeletionOrUpdateAdapter<InsightExamStatEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `insight_exam_stats` SET `id` = ?,`sessionId` = ?,`semester` = ?,`courseCode` = ?,`examType` = ?,`entered` = ?,`avgScore` = ?,`minScore` = ?,`maxScore` = ?,`stddev` = ?,`outOf` = ?,`passRate` = ?,`cachedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final InsightExamStatEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getSessionId());
        statement.bindLong(3, entity.getSemester());
        statement.bindString(4, entity.getCourseCode());
        statement.bindString(5, entity.getExamType());
        statement.bindLong(6, entity.getEntered());
        if (entity.getAvgScore() == null) {
          statement.bindNull(7);
        } else {
          statement.bindDouble(7, entity.getAvgScore());
        }
        if (entity.getMinScore() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getMinScore());
        }
        if (entity.getMaxScore() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getMaxScore());
        }
        if (entity.getStddev() == null) {
          statement.bindNull(10);
        } else {
          statement.bindDouble(10, entity.getStddev());
        }
        statement.bindLong(11, entity.getOutOf());
        if (entity.getPassRate() == null) {
          statement.bindNull(12);
        } else {
          statement.bindDouble(12, entity.getPassRate());
        }
        statement.bindLong(13, entity.getCachedAt());
        statement.bindString(14, entity.getId());
      }
    });
  }

  @Override
  public Object replaceSessionOverviews(final List<InsightSessionOverviewEntity> items,
      final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> InsightsDao.DefaultImpls.replaceSessionOverviews(InsightsDao_Impl.this, items, __cont), $completion);
  }

  @Override
  public Object replaceAtRiskStudents(final List<InsightAtRiskStudentEntity> items,
      final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> InsightsDao.DefaultImpls.replaceAtRiskStudents(InsightsDao_Impl.this, items, __cont), $completion);
  }

  @Override
  public Object replaceExamStats(final List<InsightExamStatEntity> items,
      final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> InsightsDao.DefaultImpls.replaceExamStats(InsightsDao_Impl.this, items, __cont), $completion);
  }

  @Override
  public Object clearSessionOverviews(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearSessionOverviews.acquire();
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
          __preparedStmtOfClearSessionOverviews.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAtRiskStudents(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAtRiskStudents.acquire();
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
          __preparedStmtOfClearAtRiskStudents.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearExamStats(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearExamStats.acquire();
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
          __preparedStmtOfClearExamStats.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertSessionOverviews(final List<InsightSessionOverviewEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfInsightSessionOverviewEntity.upsert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertAtRiskStudents(final List<InsightAtRiskStudentEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfInsightAtRiskStudentEntity.upsert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertExamStats(final List<InsightExamStatEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfInsightExamStatEntity.upsert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getSessionOverviews(
      final Continuation<? super List<InsightSessionOverviewEntity>> $completion) {
    final String _sql = "SELECT * FROM insight_session_overviews ORDER BY deptId, sessionId";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<InsightSessionOverviewEntity>>() {
      @Override
      @NonNull
      public List<InsightSessionOverviewEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfDeptId = CursorUtil.getColumnIndexOrThrow(_cursor, "deptId");
          final int _cursorIndexOfShift = CursorUtil.getColumnIndexOrThrow(_cursor, "shift");
          final int _cursorIndexOfCurrentSemester = CursorUtil.getColumnIndexOrThrow(_cursor, "currentSemester");
          final int _cursorIndexOfStudents = CursorUtil.getColumnIndexOrThrow(_cursor, "students");
          final int _cursorIndexOfAvgCgpa = CursorUtil.getColumnIndexOrThrow(_cursor, "avgCgpa");
          final int _cursorIndexOfAvgAttendance = CursorUtil.getColumnIndexOrThrow(_cursor, "avgAttendance");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cachedAt");
          final List<InsightSessionOverviewEntity> _result = new ArrayList<InsightSessionOverviewEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final InsightSessionOverviewEntity _item;
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpDeptId;
            _tmpDeptId = _cursor.getString(_cursorIndexOfDeptId);
            final String _tmpShift;
            _tmpShift = _cursor.getString(_cursorIndexOfShift);
            final int _tmpCurrentSemester;
            _tmpCurrentSemester = _cursor.getInt(_cursorIndexOfCurrentSemester);
            final int _tmpStudents;
            _tmpStudents = _cursor.getInt(_cursorIndexOfStudents);
            final Double _tmpAvgCgpa;
            if (_cursor.isNull(_cursorIndexOfAvgCgpa)) {
              _tmpAvgCgpa = null;
            } else {
              _tmpAvgCgpa = _cursor.getDouble(_cursorIndexOfAvgCgpa);
            }
            final Double _tmpAvgAttendance;
            if (_cursor.isNull(_cursorIndexOfAvgAttendance)) {
              _tmpAvgAttendance = null;
            } else {
              _tmpAvgAttendance = _cursor.getDouble(_cursorIndexOfAvgAttendance);
            }
            final long _tmpCachedAt;
            _tmpCachedAt = _cursor.getLong(_cursorIndexOfCachedAt);
            _item = new InsightSessionOverviewEntity(_tmpSessionId,_tmpDeptId,_tmpShift,_tmpCurrentSemester,_tmpStudents,_tmpAvgCgpa,_tmpAvgAttendance,_tmpCachedAt);
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
  public Object getAtRiskStudents(
      final Continuation<? super List<InsightAtRiskStudentEntity>> $completion) {
    final String _sql = "SELECT * FROM insight_at_risk_students ORDER BY sessionId, rollNumber";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<InsightAtRiskStudentEntity>>() {
      @Override
      @NonNull
      public List<InsightAtRiskStudentEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfRollNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNumber");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCgpa = CursorUtil.getColumnIndexOrThrow(_cursor, "cgpa");
          final int _cursorIndexOfAttendance = CursorUtil.getColumnIndexOrThrow(_cursor, "attendance");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cachedAt");
          final List<InsightAtRiskStudentEntity> _result = new ArrayList<InsightAtRiskStudentEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final InsightAtRiskStudentEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpRollNumber;
            _tmpRollNumber = _cursor.getString(_cursorIndexOfRollNumber);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final Double _tmpCgpa;
            if (_cursor.isNull(_cursorIndexOfCgpa)) {
              _tmpCgpa = null;
            } else {
              _tmpCgpa = _cursor.getDouble(_cursorIndexOfCgpa);
            }
            final Double _tmpAttendance;
            if (_cursor.isNull(_cursorIndexOfAttendance)) {
              _tmpAttendance = null;
            } else {
              _tmpAttendance = _cursor.getDouble(_cursorIndexOfAttendance);
            }
            final long _tmpCachedAt;
            _tmpCachedAt = _cursor.getLong(_cursorIndexOfCachedAt);
            _item = new InsightAtRiskStudentEntity(_tmpId,_tmpSessionId,_tmpRollNumber,_tmpName,_tmpCgpa,_tmpAttendance,_tmpCachedAt);
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
  public Object getExamStats(final Continuation<? super List<InsightExamStatEntity>> $completion) {
    final String _sql = "SELECT * FROM insight_exam_stats ORDER BY sessionId, semester, courseCode, examType";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<InsightExamStatEntity>>() {
      @Override
      @NonNull
      public List<InsightExamStatEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfSemester = CursorUtil.getColumnIndexOrThrow(_cursor, "semester");
          final int _cursorIndexOfCourseCode = CursorUtil.getColumnIndexOrThrow(_cursor, "courseCode");
          final int _cursorIndexOfExamType = CursorUtil.getColumnIndexOrThrow(_cursor, "examType");
          final int _cursorIndexOfEntered = CursorUtil.getColumnIndexOrThrow(_cursor, "entered");
          final int _cursorIndexOfAvgScore = CursorUtil.getColumnIndexOrThrow(_cursor, "avgScore");
          final int _cursorIndexOfMinScore = CursorUtil.getColumnIndexOrThrow(_cursor, "minScore");
          final int _cursorIndexOfMaxScore = CursorUtil.getColumnIndexOrThrow(_cursor, "maxScore");
          final int _cursorIndexOfStddev = CursorUtil.getColumnIndexOrThrow(_cursor, "stddev");
          final int _cursorIndexOfOutOf = CursorUtil.getColumnIndexOrThrow(_cursor, "outOf");
          final int _cursorIndexOfPassRate = CursorUtil.getColumnIndexOrThrow(_cursor, "passRate");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cachedAt");
          final List<InsightExamStatEntity> _result = new ArrayList<InsightExamStatEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final InsightExamStatEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final int _tmpSemester;
            _tmpSemester = _cursor.getInt(_cursorIndexOfSemester);
            final String _tmpCourseCode;
            _tmpCourseCode = _cursor.getString(_cursorIndexOfCourseCode);
            final String _tmpExamType;
            _tmpExamType = _cursor.getString(_cursorIndexOfExamType);
            final int _tmpEntered;
            _tmpEntered = _cursor.getInt(_cursorIndexOfEntered);
            final Double _tmpAvgScore;
            if (_cursor.isNull(_cursorIndexOfAvgScore)) {
              _tmpAvgScore = null;
            } else {
              _tmpAvgScore = _cursor.getDouble(_cursorIndexOfAvgScore);
            }
            final Integer _tmpMinScore;
            if (_cursor.isNull(_cursorIndexOfMinScore)) {
              _tmpMinScore = null;
            } else {
              _tmpMinScore = _cursor.getInt(_cursorIndexOfMinScore);
            }
            final Integer _tmpMaxScore;
            if (_cursor.isNull(_cursorIndexOfMaxScore)) {
              _tmpMaxScore = null;
            } else {
              _tmpMaxScore = _cursor.getInt(_cursorIndexOfMaxScore);
            }
            final Double _tmpStddev;
            if (_cursor.isNull(_cursorIndexOfStddev)) {
              _tmpStddev = null;
            } else {
              _tmpStddev = _cursor.getDouble(_cursorIndexOfStddev);
            }
            final int _tmpOutOf;
            _tmpOutOf = _cursor.getInt(_cursorIndexOfOutOf);
            final Double _tmpPassRate;
            if (_cursor.isNull(_cursorIndexOfPassRate)) {
              _tmpPassRate = null;
            } else {
              _tmpPassRate = _cursor.getDouble(_cursorIndexOfPassRate);
            }
            final long _tmpCachedAt;
            _tmpCachedAt = _cursor.getLong(_cursorIndexOfCachedAt);
            _item = new InsightExamStatEntity(_tmpId,_tmpSessionId,_tmpSemester,_tmpCourseCode,_tmpExamType,_tmpEntered,_tmpAvgScore,_tmpMinScore,_tmpMaxScore,_tmpStddev,_tmpOutOf,_tmpPassRate,_tmpCachedAt);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
