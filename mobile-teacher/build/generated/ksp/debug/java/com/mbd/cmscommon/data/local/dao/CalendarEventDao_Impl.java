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
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.mbd.cmscommon.data.local.entity.CalendarEventEntity;
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
public final class CalendarEventDao_Impl implements CalendarEventDao {
  private final RoomDatabase __db;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  private final EntityUpsertionAdapter<CalendarEventEntity> __upsertionAdapterOfCalendarEventEntity;

  public CalendarEventDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM calendar_events WHERE eventId = ?";
        return _query;
      }
    };
    this.__upsertionAdapterOfCalendarEventEntity = new EntityUpsertionAdapter<CalendarEventEntity>(new EntityInsertionAdapter<CalendarEventEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `calendar_events` (`eventId`,`title`,`eventType`,`startDate`,`endDate`,`startTime`,`endTime`,`description`,`venue`,`audience`,`deptId`,`sessionId`,`entityId`,`createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CalendarEventEntity entity) {
        statement.bindString(1, entity.getEventId());
        statement.bindString(2, entity.getTitle());
        statement.bindString(3, entity.getEventType());
        statement.bindString(4, entity.getStartDate());
        if (entity.getEndDate() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getEndDate());
        }
        if (entity.getStartTime() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getStartTime());
        }
        if (entity.getEndTime() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getEndTime());
        }
        if (entity.getDescription() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getDescription());
        }
        if (entity.getVenue() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getVenue());
        }
        statement.bindString(10, entity.getAudience());
        if (entity.getDeptId() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getDeptId());
        }
        if (entity.getSessionId() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getSessionId());
        }
        statement.bindLong(13, entity.getEntityId());
        statement.bindLong(14, entity.getCreatedAt());
        if (entity.getCreatedBy() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getCreatedBy());
        }
        statement.bindLong(16, entity.getUpdatedAt());
        if (entity.getUpdatedBy() == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, entity.getUpdatedBy());
        }
        final int _tmp = entity.isDeleted() ? 1 : 0;
        statement.bindLong(18, _tmp);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(19);
        } else {
          statement.bindLong(19, entity.getDeletedAt());
        }
        if (entity.getDeletedBy() == null) {
          statement.bindNull(20);
        } else {
          statement.bindString(20, entity.getDeletedBy());
        }
      }
    }, new EntityDeletionOrUpdateAdapter<CalendarEventEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `calendar_events` SET `eventId` = ?,`title` = ?,`eventType` = ?,`startDate` = ?,`endDate` = ?,`startTime` = ?,`endTime` = ?,`description` = ?,`venue` = ?,`audience` = ?,`deptId` = ?,`sessionId` = ?,`entityId` = ?,`createdAt` = ?,`createdBy` = ?,`updatedAt` = ?,`updatedBy` = ?,`isDeleted` = ?,`deletedAt` = ?,`deletedBy` = ? WHERE `eventId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CalendarEventEntity entity) {
        statement.bindString(1, entity.getEventId());
        statement.bindString(2, entity.getTitle());
        statement.bindString(3, entity.getEventType());
        statement.bindString(4, entity.getStartDate());
        if (entity.getEndDate() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getEndDate());
        }
        if (entity.getStartTime() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getStartTime());
        }
        if (entity.getEndTime() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getEndTime());
        }
        if (entity.getDescription() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getDescription());
        }
        if (entity.getVenue() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getVenue());
        }
        statement.bindString(10, entity.getAudience());
        if (entity.getDeptId() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getDeptId());
        }
        if (entity.getSessionId() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getSessionId());
        }
        statement.bindLong(13, entity.getEntityId());
        statement.bindLong(14, entity.getCreatedAt());
        if (entity.getCreatedBy() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getCreatedBy());
        }
        statement.bindLong(16, entity.getUpdatedAt());
        if (entity.getUpdatedBy() == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, entity.getUpdatedBy());
        }
        final int _tmp = entity.isDeleted() ? 1 : 0;
        statement.bindLong(18, _tmp);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(19);
        } else {
          statement.bindLong(19, entity.getDeletedAt());
        }
        if (entity.getDeletedBy() == null) {
          statement.bindNull(20);
        } else {
          statement.bindString(20, entity.getDeletedBy());
        }
        statement.bindString(21, entity.getEventId());
      }
    });
  }

  @Override
  public Object applyDelta(final List<CalendarEventEntity> active, final List<String> tombstoneIds,
      final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> CalendarEventDao.DefaultImpls.applyDelta(CalendarEventDao_Impl.this, active, tombstoneIds, __cont), $completion);
  }

  @Override
  public Object deleteById(final String eventId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, eventId);
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
  public Object upsertAll(final List<CalendarEventEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfCalendarEventEntity.upsert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAll(final Continuation<? super List<CalendarEventEntity>> $completion) {
    final String _sql = "SELECT * FROM calendar_events ORDER BY startDate ASC, title ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<CalendarEventEntity>>() {
      @Override
      @NonNull
      public List<CalendarEventEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfEventId = CursorUtil.getColumnIndexOrThrow(_cursor, "eventId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfEventType = CursorUtil.getColumnIndexOrThrow(_cursor, "eventType");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "endDate");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfVenue = CursorUtil.getColumnIndexOrThrow(_cursor, "venue");
          final int _cursorIndexOfAudience = CursorUtil.getColumnIndexOrThrow(_cursor, "audience");
          final int _cursorIndexOfDeptId = CursorUtil.getColumnIndexOrThrow(_cursor, "deptId");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final List<CalendarEventEntity> _result = new ArrayList<CalendarEventEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CalendarEventEntity _item;
            final String _tmpEventId;
            _tmpEventId = _cursor.getString(_cursorIndexOfEventId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpEventType;
            _tmpEventType = _cursor.getString(_cursorIndexOfEventType);
            final String _tmpStartDate;
            _tmpStartDate = _cursor.getString(_cursorIndexOfStartDate);
            final String _tmpEndDate;
            if (_cursor.isNull(_cursorIndexOfEndDate)) {
              _tmpEndDate = null;
            } else {
              _tmpEndDate = _cursor.getString(_cursorIndexOfEndDate);
            }
            final String _tmpStartTime;
            if (_cursor.isNull(_cursorIndexOfStartTime)) {
              _tmpStartTime = null;
            } else {
              _tmpStartTime = _cursor.getString(_cursorIndexOfStartTime);
            }
            final String _tmpEndTime;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmpEndTime = null;
            } else {
              _tmpEndTime = _cursor.getString(_cursorIndexOfEndTime);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpVenue;
            if (_cursor.isNull(_cursorIndexOfVenue)) {
              _tmpVenue = null;
            } else {
              _tmpVenue = _cursor.getString(_cursorIndexOfVenue);
            }
            final String _tmpAudience;
            _tmpAudience = _cursor.getString(_cursorIndexOfAudience);
            final String _tmpDeptId;
            if (_cursor.isNull(_cursorIndexOfDeptId)) {
              _tmpDeptId = null;
            } else {
              _tmpDeptId = _cursor.getString(_cursorIndexOfDeptId);
            }
            final String _tmpSessionId;
            if (_cursor.isNull(_cursorIndexOfSessionId)) {
              _tmpSessionId = null;
            } else {
              _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
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
            _item = new CalendarEventEntity(_tmpEventId,_tmpTitle,_tmpEventType,_tmpStartDate,_tmpEndDate,_tmpStartTime,_tmpEndTime,_tmpDescription,_tmpVenue,_tmpAudience,_tmpDeptId,_tmpSessionId,_tmpEntityId,_tmpCreatedAt,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
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
        _stringBuilder.append("DELETE FROM calendar_events WHERE eventId IN (");
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
