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
import com.mbd.cmscommon.data.local.entity.DatesheetEntity;
import com.mbd.cmscommon.data.local.entity.DatesheetSlotEntity;
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
public final class DatesheetDao_Impl implements DatesheetDao {
  private final RoomDatabase __db;

  private final SharedSQLiteStatement __preparedStmtOfDeleteDatesheetById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteSlotById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteSlotsForDatesheet;

  private final EntityUpsertionAdapter<DatesheetEntity> __upsertionAdapterOfDatesheetEntity;

  private final EntityUpsertionAdapter<DatesheetSlotEntity> __upsertionAdapterOfDatesheetSlotEntity;

  public DatesheetDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__preparedStmtOfDeleteDatesheetById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM datesheets WHERE datesheetId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteSlotById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM datesheet_slots WHERE slotId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteSlotsForDatesheet = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM datesheet_slots WHERE datesheetId = ?";
        return _query;
      }
    };
    this.__upsertionAdapterOfDatesheetEntity = new EntityUpsertionAdapter<DatesheetEntity>(new EntityInsertionAdapter<DatesheetEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `datesheets` (`datesheetId`,`title`,`examType`,`sessionId`,`published`,`instructions`,`entityId`,`createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DatesheetEntity entity) {
        statement.bindString(1, entity.getDatesheetId());
        statement.bindString(2, entity.getTitle());
        if (entity.getExamType() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getExamType());
        }
        if (entity.getSessionId() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getSessionId());
        }
        final int _tmp = entity.getPublished() ? 1 : 0;
        statement.bindLong(5, _tmp);
        if (entity.getInstructions() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getInstructions());
        }
        statement.bindLong(7, entity.getEntityId());
        statement.bindLong(8, entity.getCreatedAt());
        if (entity.getCreatedBy() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getCreatedBy());
        }
        statement.bindLong(10, entity.getUpdatedAt());
        if (entity.getUpdatedBy() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getUpdatedBy());
        }
        final int _tmp_1 = entity.isDeleted() ? 1 : 0;
        statement.bindLong(12, _tmp_1);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(13);
        } else {
          statement.bindLong(13, entity.getDeletedAt());
        }
        if (entity.getDeletedBy() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getDeletedBy());
        }
      }
    }, new EntityDeletionOrUpdateAdapter<DatesheetEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `datesheets` SET `datesheetId` = ?,`title` = ?,`examType` = ?,`sessionId` = ?,`published` = ?,`instructions` = ?,`entityId` = ?,`createdAt` = ?,`createdBy` = ?,`updatedAt` = ?,`updatedBy` = ?,`isDeleted` = ?,`deletedAt` = ?,`deletedBy` = ? WHERE `datesheetId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DatesheetEntity entity) {
        statement.bindString(1, entity.getDatesheetId());
        statement.bindString(2, entity.getTitle());
        if (entity.getExamType() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getExamType());
        }
        if (entity.getSessionId() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getSessionId());
        }
        final int _tmp = entity.getPublished() ? 1 : 0;
        statement.bindLong(5, _tmp);
        if (entity.getInstructions() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getInstructions());
        }
        statement.bindLong(7, entity.getEntityId());
        statement.bindLong(8, entity.getCreatedAt());
        if (entity.getCreatedBy() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getCreatedBy());
        }
        statement.bindLong(10, entity.getUpdatedAt());
        if (entity.getUpdatedBy() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getUpdatedBy());
        }
        final int _tmp_1 = entity.isDeleted() ? 1 : 0;
        statement.bindLong(12, _tmp_1);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(13);
        } else {
          statement.bindLong(13, entity.getDeletedAt());
        }
        if (entity.getDeletedBy() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getDeletedBy());
        }
        statement.bindString(15, entity.getDatesheetId());
      }
    });
    this.__upsertionAdapterOfDatesheetSlotEntity = new EntityUpsertionAdapter<DatesheetSlotEntity>(new EntityInsertionAdapter<DatesheetSlotEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `datesheet_slots` (`slotId`,`datesheetId`,`examDate`,`startTime`,`endTime`,`durationMinutes`,`courseCode`,`subjectName`,`roomNo`,`building`,`invigilatorEmail`,`entityId`,`createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DatesheetSlotEntity entity) {
        statement.bindString(1, entity.getSlotId());
        statement.bindString(2, entity.getDatesheetId());
        statement.bindString(3, entity.getExamDate());
        if (entity.getStartTime() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getStartTime());
        }
        if (entity.getEndTime() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getEndTime());
        }
        if (entity.getDurationMinutes() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getDurationMinutes());
        }
        if (entity.getCourseCode() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getCourseCode());
        }
        if (entity.getSubjectName() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getSubjectName());
        }
        if (entity.getRoomNo() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getRoomNo());
        }
        if (entity.getBuilding() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getBuilding());
        }
        if (entity.getInvigilatorEmail() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getInvigilatorEmail());
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
    }, new EntityDeletionOrUpdateAdapter<DatesheetSlotEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `datesheet_slots` SET `slotId` = ?,`datesheetId` = ?,`examDate` = ?,`startTime` = ?,`endTime` = ?,`durationMinutes` = ?,`courseCode` = ?,`subjectName` = ?,`roomNo` = ?,`building` = ?,`invigilatorEmail` = ?,`entityId` = ?,`createdAt` = ?,`createdBy` = ?,`updatedAt` = ?,`updatedBy` = ?,`isDeleted` = ?,`deletedAt` = ?,`deletedBy` = ? WHERE `slotId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DatesheetSlotEntity entity) {
        statement.bindString(1, entity.getSlotId());
        statement.bindString(2, entity.getDatesheetId());
        statement.bindString(3, entity.getExamDate());
        if (entity.getStartTime() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getStartTime());
        }
        if (entity.getEndTime() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getEndTime());
        }
        if (entity.getDurationMinutes() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getDurationMinutes());
        }
        if (entity.getCourseCode() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getCourseCode());
        }
        if (entity.getSubjectName() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getSubjectName());
        }
        if (entity.getRoomNo() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getRoomNo());
        }
        if (entity.getBuilding() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getBuilding());
        }
        if (entity.getInvigilatorEmail() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getInvigilatorEmail());
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
        statement.bindString(20, entity.getSlotId());
      }
    });
  }

  @Override
  public Object applyDatesheetDelta(final List<DatesheetEntity> active,
      final List<String> tombstoneIds, final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> DatesheetDao.DefaultImpls.applyDatesheetDelta(DatesheetDao_Impl.this, active, tombstoneIds, __cont), $completion);
  }

  @Override
  public Object applySlotDelta(final List<DatesheetSlotEntity> active,
      final List<String> tombstoneIds, final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> DatesheetDao.DefaultImpls.applySlotDelta(DatesheetDao_Impl.this, active, tombstoneIds, __cont), $completion);
  }

  @Override
  public Object deleteDatesheetById(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteDatesheetById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfDeleteDatesheetById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSlotById(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteSlotById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfDeleteSlotById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSlotsForDatesheet(final String datesheetId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteSlotsForDatesheet.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, datesheetId);
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
          __preparedStmtOfDeleteSlotsForDatesheet.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertDatesheets(final List<DatesheetEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfDatesheetEntity.upsert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertSlots(final List<DatesheetSlotEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfDatesheetSlotEntity.upsert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getDatesheets(final Continuation<? super List<DatesheetEntity>> $completion) {
    final String _sql = "SELECT * FROM datesheets ORDER BY createdAt DESC, title";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DatesheetEntity>>() {
      @Override
      @NonNull
      public List<DatesheetEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDatesheetId = CursorUtil.getColumnIndexOrThrow(_cursor, "datesheetId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfExamType = CursorUtil.getColumnIndexOrThrow(_cursor, "examType");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfPublished = CursorUtil.getColumnIndexOrThrow(_cursor, "published");
          final int _cursorIndexOfInstructions = CursorUtil.getColumnIndexOrThrow(_cursor, "instructions");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final List<DatesheetEntity> _result = new ArrayList<DatesheetEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DatesheetEntity _item;
            final String _tmpDatesheetId;
            _tmpDatesheetId = _cursor.getString(_cursorIndexOfDatesheetId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpExamType;
            if (_cursor.isNull(_cursorIndexOfExamType)) {
              _tmpExamType = null;
            } else {
              _tmpExamType = _cursor.getString(_cursorIndexOfExamType);
            }
            final String _tmpSessionId;
            if (_cursor.isNull(_cursorIndexOfSessionId)) {
              _tmpSessionId = null;
            } else {
              _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            }
            final boolean _tmpPublished;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfPublished);
            _tmpPublished = _tmp != 0;
            final String _tmpInstructions;
            if (_cursor.isNull(_cursorIndexOfInstructions)) {
              _tmpInstructions = null;
            } else {
              _tmpInstructions = _cursor.getString(_cursorIndexOfInstructions);
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
            _item = new DatesheetEntity(_tmpDatesheetId,_tmpTitle,_tmpExamType,_tmpSessionId,_tmpPublished,_tmpInstructions,_tmpEntityId,_tmpCreatedAt,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
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
  public Object getSlots(final String datesheetId,
      final Continuation<? super List<DatesheetSlotEntity>> $completion) {
    final String _sql = "SELECT * FROM datesheet_slots WHERE datesheetId = ? ORDER BY examDate ASC, startTime ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, datesheetId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DatesheetSlotEntity>>() {
      @Override
      @NonNull
      public List<DatesheetSlotEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSlotId = CursorUtil.getColumnIndexOrThrow(_cursor, "slotId");
          final int _cursorIndexOfDatesheetId = CursorUtil.getColumnIndexOrThrow(_cursor, "datesheetId");
          final int _cursorIndexOfExamDate = CursorUtil.getColumnIndexOrThrow(_cursor, "examDate");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMinutes");
          final int _cursorIndexOfCourseCode = CursorUtil.getColumnIndexOrThrow(_cursor, "courseCode");
          final int _cursorIndexOfSubjectName = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectName");
          final int _cursorIndexOfRoomNo = CursorUtil.getColumnIndexOrThrow(_cursor, "roomNo");
          final int _cursorIndexOfBuilding = CursorUtil.getColumnIndexOrThrow(_cursor, "building");
          final int _cursorIndexOfInvigilatorEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "invigilatorEmail");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final List<DatesheetSlotEntity> _result = new ArrayList<DatesheetSlotEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DatesheetSlotEntity _item;
            final String _tmpSlotId;
            _tmpSlotId = _cursor.getString(_cursorIndexOfSlotId);
            final String _tmpDatesheetId;
            _tmpDatesheetId = _cursor.getString(_cursorIndexOfDatesheetId);
            final String _tmpExamDate;
            _tmpExamDate = _cursor.getString(_cursorIndexOfExamDate);
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
            final Integer _tmpDurationMinutes;
            if (_cursor.isNull(_cursorIndexOfDurationMinutes)) {
              _tmpDurationMinutes = null;
            } else {
              _tmpDurationMinutes = _cursor.getInt(_cursorIndexOfDurationMinutes);
            }
            final String _tmpCourseCode;
            if (_cursor.isNull(_cursorIndexOfCourseCode)) {
              _tmpCourseCode = null;
            } else {
              _tmpCourseCode = _cursor.getString(_cursorIndexOfCourseCode);
            }
            final String _tmpSubjectName;
            if (_cursor.isNull(_cursorIndexOfSubjectName)) {
              _tmpSubjectName = null;
            } else {
              _tmpSubjectName = _cursor.getString(_cursorIndexOfSubjectName);
            }
            final String _tmpRoomNo;
            if (_cursor.isNull(_cursorIndexOfRoomNo)) {
              _tmpRoomNo = null;
            } else {
              _tmpRoomNo = _cursor.getString(_cursorIndexOfRoomNo);
            }
            final String _tmpBuilding;
            if (_cursor.isNull(_cursorIndexOfBuilding)) {
              _tmpBuilding = null;
            } else {
              _tmpBuilding = _cursor.getString(_cursorIndexOfBuilding);
            }
            final String _tmpInvigilatorEmail;
            if (_cursor.isNull(_cursorIndexOfInvigilatorEmail)) {
              _tmpInvigilatorEmail = null;
            } else {
              _tmpInvigilatorEmail = _cursor.getString(_cursorIndexOfInvigilatorEmail);
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
            _item = new DatesheetSlotEntity(_tmpSlotId,_tmpDatesheetId,_tmpExamDate,_tmpStartTime,_tmpEndTime,_tmpDurationMinutes,_tmpCourseCode,_tmpSubjectName,_tmpRoomNo,_tmpBuilding,_tmpInvigilatorEmail,_tmpEntityId,_tmpCreatedAt,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
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
  public Object deleteDatesheetsByIds(final List<String> ids,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("DELETE FROM datesheets WHERE datesheetId IN (");
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
  public Object deleteSlotsByIds(final List<String> ids,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("DELETE FROM datesheet_slots WHERE slotId IN (");
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
