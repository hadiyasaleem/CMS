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
import com.mbd.cmscommon.data.local.entity.TeacherEntity;
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
public final class TeacherDao_Impl implements TeacherDao {
  private final RoomDatabase __db;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  private final SharedSQLiteStatement __preparedStmtOfClearDeptReference;

  private final EntityUpsertionAdapter<TeacherEntity> __upsertionAdapterOfTeacherEntity;

  public TeacherDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM teachers WHERE teacherId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM teachers";
        return _query;
      }
    };
    this.__preparedStmtOfClearDeptReference = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE teachers SET deptId = NULL WHERE deptId = ?";
        return _query;
      }
    };
    this.__upsertionAdapterOfTeacherEntity = new EntityUpsertionAdapter<TeacherEntity>(new EntityInsertionAdapter<TeacherEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `teachers` (`teacherId`,`entityId`,`name`,`email`,`phone`,`deptId`,`designation`,`qualification`,`specialization`,`officeRoom`,`gender`,`canApproveLinkRequests`,`canEditTimetable`,`canSendNotifications`,`canManageDatesheets`,`status`,`isActive`,`archivedAt`,`createdAt`,`createdBy`,`updatedAt`,`updatedBy`,`isDeleted`,`deletedAt`,`deletedBy`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TeacherEntity entity) {
        statement.bindString(1, entity.getTeacherId());
        statement.bindLong(2, entity.getEntityId());
        statement.bindString(3, entity.getName());
        statement.bindString(4, entity.getEmail());
        if (entity.getPhone() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getPhone());
        }
        if (entity.getDeptId() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getDeptId());
        }
        if (entity.getDesignation() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getDesignation());
        }
        if (entity.getQualification() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getQualification());
        }
        if (entity.getSpecialization() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getSpecialization());
        }
        if (entity.getOfficeRoom() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getOfficeRoom());
        }
        if (entity.getGender() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getGender());
        }
        final int _tmp = entity.getCanApproveLinkRequests() ? 1 : 0;
        statement.bindLong(12, _tmp);
        final int _tmp_1 = entity.getCanEditTimetable() ? 1 : 0;
        statement.bindLong(13, _tmp_1);
        final int _tmp_2 = entity.getCanSendNotifications() ? 1 : 0;
        statement.bindLong(14, _tmp_2);
        final int _tmp_3 = entity.getCanManageDatesheets() ? 1 : 0;
        statement.bindLong(15, _tmp_3);
        statement.bindString(16, entity.getStatus());
        final int _tmp_4 = entity.isActive() ? 1 : 0;
        statement.bindLong(17, _tmp_4);
        if (entity.getArchivedAt() == null) {
          statement.bindNull(18);
        } else {
          statement.bindLong(18, entity.getArchivedAt());
        }
        statement.bindLong(19, entity.getCreatedAt());
        statement.bindString(20, entity.getCreatedBy());
        statement.bindLong(21, entity.getUpdatedAt());
        statement.bindString(22, entity.getUpdatedBy());
        final int _tmp_5 = entity.isDeleted() ? 1 : 0;
        statement.bindLong(23, _tmp_5);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(24);
        } else {
          statement.bindLong(24, entity.getDeletedAt());
        }
        if (entity.getDeletedBy() == null) {
          statement.bindNull(25);
        } else {
          statement.bindString(25, entity.getDeletedBy());
        }
      }
    }, new EntityDeletionOrUpdateAdapter<TeacherEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `teachers` SET `teacherId` = ?,`entityId` = ?,`name` = ?,`email` = ?,`phone` = ?,`deptId` = ?,`designation` = ?,`qualification` = ?,`specialization` = ?,`officeRoom` = ?,`gender` = ?,`canApproveLinkRequests` = ?,`canEditTimetable` = ?,`canSendNotifications` = ?,`canManageDatesheets` = ?,`status` = ?,`isActive` = ?,`archivedAt` = ?,`createdAt` = ?,`createdBy` = ?,`updatedAt` = ?,`updatedBy` = ?,`isDeleted` = ?,`deletedAt` = ?,`deletedBy` = ? WHERE `teacherId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TeacherEntity entity) {
        statement.bindString(1, entity.getTeacherId());
        statement.bindLong(2, entity.getEntityId());
        statement.bindString(3, entity.getName());
        statement.bindString(4, entity.getEmail());
        if (entity.getPhone() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getPhone());
        }
        if (entity.getDeptId() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getDeptId());
        }
        if (entity.getDesignation() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getDesignation());
        }
        if (entity.getQualification() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getQualification());
        }
        if (entity.getSpecialization() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getSpecialization());
        }
        if (entity.getOfficeRoom() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getOfficeRoom());
        }
        if (entity.getGender() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getGender());
        }
        final int _tmp = entity.getCanApproveLinkRequests() ? 1 : 0;
        statement.bindLong(12, _tmp);
        final int _tmp_1 = entity.getCanEditTimetable() ? 1 : 0;
        statement.bindLong(13, _tmp_1);
        final int _tmp_2 = entity.getCanSendNotifications() ? 1 : 0;
        statement.bindLong(14, _tmp_2);
        final int _tmp_3 = entity.getCanManageDatesheets() ? 1 : 0;
        statement.bindLong(15, _tmp_3);
        statement.bindString(16, entity.getStatus());
        final int _tmp_4 = entity.isActive() ? 1 : 0;
        statement.bindLong(17, _tmp_4);
        if (entity.getArchivedAt() == null) {
          statement.bindNull(18);
        } else {
          statement.bindLong(18, entity.getArchivedAt());
        }
        statement.bindLong(19, entity.getCreatedAt());
        statement.bindString(20, entity.getCreatedBy());
        statement.bindLong(21, entity.getUpdatedAt());
        statement.bindString(22, entity.getUpdatedBy());
        final int _tmp_5 = entity.isDeleted() ? 1 : 0;
        statement.bindLong(23, _tmp_5);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(24);
        } else {
          statement.bindLong(24, entity.getDeletedAt());
        }
        if (entity.getDeletedBy() == null) {
          statement.bindNull(25);
        } else {
          statement.bindString(25, entity.getDeletedBy());
        }
        statement.bindString(26, entity.getTeacherId());
      }
    });
  }

  @Override
  public Object applyDelta(final List<TeacherEntity> active, final List<String> tombstoneIds,
      final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> TeacherDao.DefaultImpls.applyDelta(TeacherDao_Impl.this, active, tombstoneIds, __cont), $completion);
  }

  @Override
  public Object deleteById(final String teacherId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, teacherId);
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
  public Object clearDeptReference(final String deptId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearDeptReference.acquire();
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
          __preparedStmtOfClearDeptReference.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertAll(final List<TeacherEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfTeacherEntity.upsert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsert(final TeacherEntity item, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfTeacherEntity.upsert(item);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<TeacherEntity> observe(final String teacherId) {
    final String _sql = "SELECT * FROM teachers WHERE teacherId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, teacherId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"teachers"}, new Callable<TeacherEntity>() {
      @Override
      @Nullable
      public TeacherEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfTeacherId = CursorUtil.getColumnIndexOrThrow(_cursor, "teacherId");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
          final int _cursorIndexOfDeptId = CursorUtil.getColumnIndexOrThrow(_cursor, "deptId");
          final int _cursorIndexOfDesignation = CursorUtil.getColumnIndexOrThrow(_cursor, "designation");
          final int _cursorIndexOfQualification = CursorUtil.getColumnIndexOrThrow(_cursor, "qualification");
          final int _cursorIndexOfSpecialization = CursorUtil.getColumnIndexOrThrow(_cursor, "specialization");
          final int _cursorIndexOfOfficeRoom = CursorUtil.getColumnIndexOrThrow(_cursor, "officeRoom");
          final int _cursorIndexOfGender = CursorUtil.getColumnIndexOrThrow(_cursor, "gender");
          final int _cursorIndexOfCanApproveLinkRequests = CursorUtil.getColumnIndexOrThrow(_cursor, "canApproveLinkRequests");
          final int _cursorIndexOfCanEditTimetable = CursorUtil.getColumnIndexOrThrow(_cursor, "canEditTimetable");
          final int _cursorIndexOfCanSendNotifications = CursorUtil.getColumnIndexOrThrow(_cursor, "canSendNotifications");
          final int _cursorIndexOfCanManageDatesheets = CursorUtil.getColumnIndexOrThrow(_cursor, "canManageDatesheets");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfArchivedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "archivedAt");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final TeacherEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpTeacherId;
            _tmpTeacherId = _cursor.getString(_cursorIndexOfTeacherId);
            final long _tmpEntityId;
            _tmpEntityId = _cursor.getLong(_cursorIndexOfEntityId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpEmail;
            _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            final String _tmpPhone;
            if (_cursor.isNull(_cursorIndexOfPhone)) {
              _tmpPhone = null;
            } else {
              _tmpPhone = _cursor.getString(_cursorIndexOfPhone);
            }
            final String _tmpDeptId;
            if (_cursor.isNull(_cursorIndexOfDeptId)) {
              _tmpDeptId = null;
            } else {
              _tmpDeptId = _cursor.getString(_cursorIndexOfDeptId);
            }
            final String _tmpDesignation;
            if (_cursor.isNull(_cursorIndexOfDesignation)) {
              _tmpDesignation = null;
            } else {
              _tmpDesignation = _cursor.getString(_cursorIndexOfDesignation);
            }
            final String _tmpQualification;
            if (_cursor.isNull(_cursorIndexOfQualification)) {
              _tmpQualification = null;
            } else {
              _tmpQualification = _cursor.getString(_cursorIndexOfQualification);
            }
            final String _tmpSpecialization;
            if (_cursor.isNull(_cursorIndexOfSpecialization)) {
              _tmpSpecialization = null;
            } else {
              _tmpSpecialization = _cursor.getString(_cursorIndexOfSpecialization);
            }
            final String _tmpOfficeRoom;
            if (_cursor.isNull(_cursorIndexOfOfficeRoom)) {
              _tmpOfficeRoom = null;
            } else {
              _tmpOfficeRoom = _cursor.getString(_cursorIndexOfOfficeRoom);
            }
            final String _tmpGender;
            if (_cursor.isNull(_cursorIndexOfGender)) {
              _tmpGender = null;
            } else {
              _tmpGender = _cursor.getString(_cursorIndexOfGender);
            }
            final boolean _tmpCanApproveLinkRequests;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfCanApproveLinkRequests);
            _tmpCanApproveLinkRequests = _tmp != 0;
            final boolean _tmpCanEditTimetable;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfCanEditTimetable);
            _tmpCanEditTimetable = _tmp_1 != 0;
            final boolean _tmpCanSendNotifications;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfCanSendNotifications);
            _tmpCanSendNotifications = _tmp_2 != 0;
            final boolean _tmpCanManageDatesheets;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfCanManageDatesheets);
            _tmpCanManageDatesheets = _tmp_3 != 0;
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final boolean _tmpIsActive;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp_4 != 0;
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
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_5 != 0;
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
            _result = new TeacherEntity(_tmpTeacherId,_tmpEntityId,_tmpName,_tmpEmail,_tmpPhone,_tmpDeptId,_tmpDesignation,_tmpQualification,_tmpSpecialization,_tmpOfficeRoom,_tmpGender,_tmpCanApproveLinkRequests,_tmpCanEditTimetable,_tmpCanSendNotifications,_tmpCanManageDatesheets,_tmpStatus,_tmpIsActive,_tmpArchivedAt,_tmpCreatedAt,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
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
  public Flow<List<TeacherEntity>> observeActive() {
    final String _sql = "SELECT * FROM teachers WHERE isActive = 1 ORDER BY name";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"teachers"}, new Callable<List<TeacherEntity>>() {
      @Override
      @NonNull
      public List<TeacherEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfTeacherId = CursorUtil.getColumnIndexOrThrow(_cursor, "teacherId");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
          final int _cursorIndexOfDeptId = CursorUtil.getColumnIndexOrThrow(_cursor, "deptId");
          final int _cursorIndexOfDesignation = CursorUtil.getColumnIndexOrThrow(_cursor, "designation");
          final int _cursorIndexOfQualification = CursorUtil.getColumnIndexOrThrow(_cursor, "qualification");
          final int _cursorIndexOfSpecialization = CursorUtil.getColumnIndexOrThrow(_cursor, "specialization");
          final int _cursorIndexOfOfficeRoom = CursorUtil.getColumnIndexOrThrow(_cursor, "officeRoom");
          final int _cursorIndexOfGender = CursorUtil.getColumnIndexOrThrow(_cursor, "gender");
          final int _cursorIndexOfCanApproveLinkRequests = CursorUtil.getColumnIndexOrThrow(_cursor, "canApproveLinkRequests");
          final int _cursorIndexOfCanEditTimetable = CursorUtil.getColumnIndexOrThrow(_cursor, "canEditTimetable");
          final int _cursorIndexOfCanSendNotifications = CursorUtil.getColumnIndexOrThrow(_cursor, "canSendNotifications");
          final int _cursorIndexOfCanManageDatesheets = CursorUtil.getColumnIndexOrThrow(_cursor, "canManageDatesheets");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfArchivedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "archivedAt");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final List<TeacherEntity> _result = new ArrayList<TeacherEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TeacherEntity _item;
            final String _tmpTeacherId;
            _tmpTeacherId = _cursor.getString(_cursorIndexOfTeacherId);
            final long _tmpEntityId;
            _tmpEntityId = _cursor.getLong(_cursorIndexOfEntityId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpEmail;
            _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            final String _tmpPhone;
            if (_cursor.isNull(_cursorIndexOfPhone)) {
              _tmpPhone = null;
            } else {
              _tmpPhone = _cursor.getString(_cursorIndexOfPhone);
            }
            final String _tmpDeptId;
            if (_cursor.isNull(_cursorIndexOfDeptId)) {
              _tmpDeptId = null;
            } else {
              _tmpDeptId = _cursor.getString(_cursorIndexOfDeptId);
            }
            final String _tmpDesignation;
            if (_cursor.isNull(_cursorIndexOfDesignation)) {
              _tmpDesignation = null;
            } else {
              _tmpDesignation = _cursor.getString(_cursorIndexOfDesignation);
            }
            final String _tmpQualification;
            if (_cursor.isNull(_cursorIndexOfQualification)) {
              _tmpQualification = null;
            } else {
              _tmpQualification = _cursor.getString(_cursorIndexOfQualification);
            }
            final String _tmpSpecialization;
            if (_cursor.isNull(_cursorIndexOfSpecialization)) {
              _tmpSpecialization = null;
            } else {
              _tmpSpecialization = _cursor.getString(_cursorIndexOfSpecialization);
            }
            final String _tmpOfficeRoom;
            if (_cursor.isNull(_cursorIndexOfOfficeRoom)) {
              _tmpOfficeRoom = null;
            } else {
              _tmpOfficeRoom = _cursor.getString(_cursorIndexOfOfficeRoom);
            }
            final String _tmpGender;
            if (_cursor.isNull(_cursorIndexOfGender)) {
              _tmpGender = null;
            } else {
              _tmpGender = _cursor.getString(_cursorIndexOfGender);
            }
            final boolean _tmpCanApproveLinkRequests;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfCanApproveLinkRequests);
            _tmpCanApproveLinkRequests = _tmp != 0;
            final boolean _tmpCanEditTimetable;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfCanEditTimetable);
            _tmpCanEditTimetable = _tmp_1 != 0;
            final boolean _tmpCanSendNotifications;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfCanSendNotifications);
            _tmpCanSendNotifications = _tmp_2 != 0;
            final boolean _tmpCanManageDatesheets;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfCanManageDatesheets);
            _tmpCanManageDatesheets = _tmp_3 != 0;
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final boolean _tmpIsActive;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp_4 != 0;
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
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_5 != 0;
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
            _item = new TeacherEntity(_tmpTeacherId,_tmpEntityId,_tmpName,_tmpEmail,_tmpPhone,_tmpDeptId,_tmpDesignation,_tmpQualification,_tmpSpecialization,_tmpOfficeRoom,_tmpGender,_tmpCanApproveLinkRequests,_tmpCanEditTimetable,_tmpCanSendNotifications,_tmpCanManageDatesheets,_tmpStatus,_tmpIsActive,_tmpArchivedAt,_tmpCreatedAt,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
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
  public Object getById(final String teacherId,
      final Continuation<? super TeacherEntity> $completion) {
    final String _sql = "SELECT * FROM teachers WHERE teacherId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, teacherId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TeacherEntity>() {
      @Override
      @Nullable
      public TeacherEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfTeacherId = CursorUtil.getColumnIndexOrThrow(_cursor, "teacherId");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entityId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
          final int _cursorIndexOfDeptId = CursorUtil.getColumnIndexOrThrow(_cursor, "deptId");
          final int _cursorIndexOfDesignation = CursorUtil.getColumnIndexOrThrow(_cursor, "designation");
          final int _cursorIndexOfQualification = CursorUtil.getColumnIndexOrThrow(_cursor, "qualification");
          final int _cursorIndexOfSpecialization = CursorUtil.getColumnIndexOrThrow(_cursor, "specialization");
          final int _cursorIndexOfOfficeRoom = CursorUtil.getColumnIndexOrThrow(_cursor, "officeRoom");
          final int _cursorIndexOfGender = CursorUtil.getColumnIndexOrThrow(_cursor, "gender");
          final int _cursorIndexOfCanApproveLinkRequests = CursorUtil.getColumnIndexOrThrow(_cursor, "canApproveLinkRequests");
          final int _cursorIndexOfCanEditTimetable = CursorUtil.getColumnIndexOrThrow(_cursor, "canEditTimetable");
          final int _cursorIndexOfCanSendNotifications = CursorUtil.getColumnIndexOrThrow(_cursor, "canSendNotifications");
          final int _cursorIndexOfCanManageDatesheets = CursorUtil.getColumnIndexOrThrow(_cursor, "canManageDatesheets");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfArchivedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "archivedAt");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfUpdatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedBy");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfDeletedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedBy");
          final TeacherEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpTeacherId;
            _tmpTeacherId = _cursor.getString(_cursorIndexOfTeacherId);
            final long _tmpEntityId;
            _tmpEntityId = _cursor.getLong(_cursorIndexOfEntityId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpEmail;
            _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            final String _tmpPhone;
            if (_cursor.isNull(_cursorIndexOfPhone)) {
              _tmpPhone = null;
            } else {
              _tmpPhone = _cursor.getString(_cursorIndexOfPhone);
            }
            final String _tmpDeptId;
            if (_cursor.isNull(_cursorIndexOfDeptId)) {
              _tmpDeptId = null;
            } else {
              _tmpDeptId = _cursor.getString(_cursorIndexOfDeptId);
            }
            final String _tmpDesignation;
            if (_cursor.isNull(_cursorIndexOfDesignation)) {
              _tmpDesignation = null;
            } else {
              _tmpDesignation = _cursor.getString(_cursorIndexOfDesignation);
            }
            final String _tmpQualification;
            if (_cursor.isNull(_cursorIndexOfQualification)) {
              _tmpQualification = null;
            } else {
              _tmpQualification = _cursor.getString(_cursorIndexOfQualification);
            }
            final String _tmpSpecialization;
            if (_cursor.isNull(_cursorIndexOfSpecialization)) {
              _tmpSpecialization = null;
            } else {
              _tmpSpecialization = _cursor.getString(_cursorIndexOfSpecialization);
            }
            final String _tmpOfficeRoom;
            if (_cursor.isNull(_cursorIndexOfOfficeRoom)) {
              _tmpOfficeRoom = null;
            } else {
              _tmpOfficeRoom = _cursor.getString(_cursorIndexOfOfficeRoom);
            }
            final String _tmpGender;
            if (_cursor.isNull(_cursorIndexOfGender)) {
              _tmpGender = null;
            } else {
              _tmpGender = _cursor.getString(_cursorIndexOfGender);
            }
            final boolean _tmpCanApproveLinkRequests;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfCanApproveLinkRequests);
            _tmpCanApproveLinkRequests = _tmp != 0;
            final boolean _tmpCanEditTimetable;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfCanEditTimetable);
            _tmpCanEditTimetable = _tmp_1 != 0;
            final boolean _tmpCanSendNotifications;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfCanSendNotifications);
            _tmpCanSendNotifications = _tmp_2 != 0;
            final boolean _tmpCanManageDatesheets;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfCanManageDatesheets);
            _tmpCanManageDatesheets = _tmp_3 != 0;
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final boolean _tmpIsActive;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp_4 != 0;
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
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_5 != 0;
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
            _result = new TeacherEntity(_tmpTeacherId,_tmpEntityId,_tmpName,_tmpEmail,_tmpPhone,_tmpDeptId,_tmpDesignation,_tmpQualification,_tmpSpecialization,_tmpOfficeRoom,_tmpGender,_tmpCanApproveLinkRequests,_tmpCanEditTimetable,_tmpCanSendNotifications,_tmpCanManageDatesheets,_tmpStatus,_tmpIsActive,_tmpArchivedAt,_tmpCreatedAt,_tmpCreatedBy,_tmpUpdatedAt,_tmpUpdatedBy,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletedBy);
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
        _stringBuilder.append("DELETE FROM teachers WHERE teacherId IN (");
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
        _stringBuilder.append("DELETE FROM teachers WHERE teacherId NOT IN (");
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
