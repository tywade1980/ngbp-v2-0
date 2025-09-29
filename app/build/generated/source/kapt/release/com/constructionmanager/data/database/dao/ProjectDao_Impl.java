package com.constructionmanager.data.database.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.constructionmanager.data.database.entity.ProjectEntity;
import com.constructionmanager.domain.model.ConstructionPhase;
import com.constructionmanager.domain.model.ProjectStatus;
import com.constructionmanager.domain.model.ProjectType;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.Integer;
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
public final class ProjectDao_Impl implements ProjectDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ProjectEntity> __insertionAdapterOfProjectEntity;

  private final EntityDeletionOrUpdateAdapter<ProjectEntity> __deletionAdapterOfProjectEntity;

  private final EntityDeletionOrUpdateAdapter<ProjectEntity> __updateAdapterOfProjectEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteProjectById;

  public ProjectDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfProjectEntity = new EntityInsertionAdapter<ProjectEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `projects` (`id`,`name`,`address`,`city`,`state`,`zipCode`,`clientName`,`clientEmail`,`clientPhone`,`projectType`,`currentPhase`,`startDate`,`estimatedEndDate`,`actualEndDate`,`totalBudget`,`currentCost`,`status`,`notes`,`createdAt`,`updatedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @Nullable final ProjectEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
        if (entity.getAddress() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getAddress());
        }
        if (entity.getCity() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getCity());
        }
        if (entity.getState() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getState());
        }
        if (entity.getZipCode() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getZipCode());
        }
        if (entity.getClientName() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getClientName());
        }
        if (entity.getClientEmail() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getClientEmail());
        }
        if (entity.getClientPhone() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getClientPhone());
        }
        statement.bindString(10, __ProjectType_enumToString(entity.getProjectType()));
        statement.bindString(11, __ConstructionPhase_enumToString(entity.getCurrentPhase()));
        if (entity.getStartDate() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getStartDate());
        }
        if (entity.getEstimatedEndDate() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getEstimatedEndDate());
        }
        if (entity.getActualEndDate() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getActualEndDate());
        }
        if (entity.getTotalBudget() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getTotalBudget());
        }
        if (entity.getCurrentCost() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getCurrentCost());
        }
        statement.bindString(17, __ProjectStatus_enumToString(entity.getStatus()));
        if (entity.getNotes() == null) {
          statement.bindNull(18);
        } else {
          statement.bindString(18, entity.getNotes());
        }
        if (entity.getCreatedAt() == null) {
          statement.bindNull(19);
        } else {
          statement.bindString(19, entity.getCreatedAt());
        }
        if (entity.getUpdatedAt() == null) {
          statement.bindNull(20);
        } else {
          statement.bindString(20, entity.getUpdatedAt());
        }
      }
    };
    this.__deletionAdapterOfProjectEntity = new EntityDeletionOrUpdateAdapter<ProjectEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `projects` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @Nullable final ProjectEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
      }
    };
    this.__updateAdapterOfProjectEntity = new EntityDeletionOrUpdateAdapter<ProjectEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `projects` SET `id` = ?,`name` = ?,`address` = ?,`city` = ?,`state` = ?,`zipCode` = ?,`clientName` = ?,`clientEmail` = ?,`clientPhone` = ?,`projectType` = ?,`currentPhase` = ?,`startDate` = ?,`estimatedEndDate` = ?,`actualEndDate` = ?,`totalBudget` = ?,`currentCost` = ?,`status` = ?,`notes` = ?,`createdAt` = ?,`updatedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @Nullable final ProjectEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
        if (entity.getAddress() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getAddress());
        }
        if (entity.getCity() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getCity());
        }
        if (entity.getState() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getState());
        }
        if (entity.getZipCode() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getZipCode());
        }
        if (entity.getClientName() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getClientName());
        }
        if (entity.getClientEmail() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getClientEmail());
        }
        if (entity.getClientPhone() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getClientPhone());
        }
        statement.bindString(10, __ProjectType_enumToString(entity.getProjectType()));
        statement.bindString(11, __ConstructionPhase_enumToString(entity.getCurrentPhase()));
        if (entity.getStartDate() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getStartDate());
        }
        if (entity.getEstimatedEndDate() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getEstimatedEndDate());
        }
        if (entity.getActualEndDate() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getActualEndDate());
        }
        if (entity.getTotalBudget() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getTotalBudget());
        }
        if (entity.getCurrentCost() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getCurrentCost());
        }
        statement.bindString(17, __ProjectStatus_enumToString(entity.getStatus()));
        if (entity.getNotes() == null) {
          statement.bindNull(18);
        } else {
          statement.bindString(18, entity.getNotes());
        }
        if (entity.getCreatedAt() == null) {
          statement.bindNull(19);
        } else {
          statement.bindString(19, entity.getCreatedAt());
        }
        if (entity.getUpdatedAt() == null) {
          statement.bindNull(20);
        } else {
          statement.bindString(20, entity.getUpdatedAt());
        }
        if (entity.getId() == null) {
          statement.bindNull(21);
        } else {
          statement.bindString(21, entity.getId());
        }
      }
    };
    this.__preparedStmtOfDeleteProjectById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM projects WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertProject(final ProjectEntity project,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfProjectEntity.insert(project);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertProjects(final List<ProjectEntity> projects,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfProjectEntity.insert(projects);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteProject(final ProjectEntity project,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfProjectEntity.handle(project);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateProject(final ProjectEntity project,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfProjectEntity.handle(project);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteProjectById(final String projectId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteProjectById.acquire();
        int _argIndex = 1;
        if (projectId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, projectId);
        }
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
          __preparedStmtOfDeleteProjectById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ProjectEntity>> getAllProjects() {
    final String _sql = "SELECT * FROM projects ORDER BY updatedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"projects"}, new Callable<List<ProjectEntity>>() {
      @Override
      @NonNull
      public List<ProjectEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "address");
          final int _cursorIndexOfCity = CursorUtil.getColumnIndexOrThrow(_cursor, "city");
          final int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
          final int _cursorIndexOfZipCode = CursorUtil.getColumnIndexOrThrow(_cursor, "zipCode");
          final int _cursorIndexOfClientName = CursorUtil.getColumnIndexOrThrow(_cursor, "clientName");
          final int _cursorIndexOfClientEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "clientEmail");
          final int _cursorIndexOfClientPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "clientPhone");
          final int _cursorIndexOfProjectType = CursorUtil.getColumnIndexOrThrow(_cursor, "projectType");
          final int _cursorIndexOfCurrentPhase = CursorUtil.getColumnIndexOrThrow(_cursor, "currentPhase");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfEstimatedEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "estimatedEndDate");
          final int _cursorIndexOfActualEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "actualEndDate");
          final int _cursorIndexOfTotalBudget = CursorUtil.getColumnIndexOrThrow(_cursor, "totalBudget");
          final int _cursorIndexOfCurrentCost = CursorUtil.getColumnIndexOrThrow(_cursor, "currentCost");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<ProjectEntity> _result = new ArrayList<ProjectEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ProjectEntity _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpAddress;
            if (_cursor.isNull(_cursorIndexOfAddress)) {
              _tmpAddress = null;
            } else {
              _tmpAddress = _cursor.getString(_cursorIndexOfAddress);
            }
            final String _tmpCity;
            if (_cursor.isNull(_cursorIndexOfCity)) {
              _tmpCity = null;
            } else {
              _tmpCity = _cursor.getString(_cursorIndexOfCity);
            }
            final String _tmpState;
            if (_cursor.isNull(_cursorIndexOfState)) {
              _tmpState = null;
            } else {
              _tmpState = _cursor.getString(_cursorIndexOfState);
            }
            final String _tmpZipCode;
            if (_cursor.isNull(_cursorIndexOfZipCode)) {
              _tmpZipCode = null;
            } else {
              _tmpZipCode = _cursor.getString(_cursorIndexOfZipCode);
            }
            final String _tmpClientName;
            if (_cursor.isNull(_cursorIndexOfClientName)) {
              _tmpClientName = null;
            } else {
              _tmpClientName = _cursor.getString(_cursorIndexOfClientName);
            }
            final String _tmpClientEmail;
            if (_cursor.isNull(_cursorIndexOfClientEmail)) {
              _tmpClientEmail = null;
            } else {
              _tmpClientEmail = _cursor.getString(_cursorIndexOfClientEmail);
            }
            final String _tmpClientPhone;
            if (_cursor.isNull(_cursorIndexOfClientPhone)) {
              _tmpClientPhone = null;
            } else {
              _tmpClientPhone = _cursor.getString(_cursorIndexOfClientPhone);
            }
            final ProjectType _tmpProjectType;
            _tmpProjectType = __ProjectType_stringToEnum(_cursor.getString(_cursorIndexOfProjectType));
            final ConstructionPhase _tmpCurrentPhase;
            _tmpCurrentPhase = __ConstructionPhase_stringToEnum(_cursor.getString(_cursorIndexOfCurrentPhase));
            final String _tmpStartDate;
            if (_cursor.isNull(_cursorIndexOfStartDate)) {
              _tmpStartDate = null;
            } else {
              _tmpStartDate = _cursor.getString(_cursorIndexOfStartDate);
            }
            final String _tmpEstimatedEndDate;
            if (_cursor.isNull(_cursorIndexOfEstimatedEndDate)) {
              _tmpEstimatedEndDate = null;
            } else {
              _tmpEstimatedEndDate = _cursor.getString(_cursorIndexOfEstimatedEndDate);
            }
            final String _tmpActualEndDate;
            if (_cursor.isNull(_cursorIndexOfActualEndDate)) {
              _tmpActualEndDate = null;
            } else {
              _tmpActualEndDate = _cursor.getString(_cursorIndexOfActualEndDate);
            }
            final String _tmpTotalBudget;
            if (_cursor.isNull(_cursorIndexOfTotalBudget)) {
              _tmpTotalBudget = null;
            } else {
              _tmpTotalBudget = _cursor.getString(_cursorIndexOfTotalBudget);
            }
            final String _tmpCurrentCost;
            if (_cursor.isNull(_cursorIndexOfCurrentCost)) {
              _tmpCurrentCost = null;
            } else {
              _tmpCurrentCost = _cursor.getString(_cursorIndexOfCurrentCost);
            }
            final ProjectStatus _tmpStatus;
            _tmpStatus = __ProjectStatus_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final String _tmpCreatedAt;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreatedAt = null;
            } else {
              _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            final String _tmpUpdatedAt;
            if (_cursor.isNull(_cursorIndexOfUpdatedAt)) {
              _tmpUpdatedAt = null;
            } else {
              _tmpUpdatedAt = _cursor.getString(_cursorIndexOfUpdatedAt);
            }
            _item = new ProjectEntity(_tmpId,_tmpName,_tmpAddress,_tmpCity,_tmpState,_tmpZipCode,_tmpClientName,_tmpClientEmail,_tmpClientPhone,_tmpProjectType,_tmpCurrentPhase,_tmpStartDate,_tmpEstimatedEndDate,_tmpActualEndDate,_tmpTotalBudget,_tmpCurrentCost,_tmpStatus,_tmpNotes,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Flow<List<ProjectEntity>> getProjectsByStatus(final ProjectStatus status) {
    final String _sql = "SELECT * FROM projects WHERE status = ? ORDER BY updatedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, __ProjectStatus_enumToString(status));
    return CoroutinesRoom.createFlow(__db, false, new String[] {"projects"}, new Callable<List<ProjectEntity>>() {
      @Override
      @NonNull
      public List<ProjectEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "address");
          final int _cursorIndexOfCity = CursorUtil.getColumnIndexOrThrow(_cursor, "city");
          final int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
          final int _cursorIndexOfZipCode = CursorUtil.getColumnIndexOrThrow(_cursor, "zipCode");
          final int _cursorIndexOfClientName = CursorUtil.getColumnIndexOrThrow(_cursor, "clientName");
          final int _cursorIndexOfClientEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "clientEmail");
          final int _cursorIndexOfClientPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "clientPhone");
          final int _cursorIndexOfProjectType = CursorUtil.getColumnIndexOrThrow(_cursor, "projectType");
          final int _cursorIndexOfCurrentPhase = CursorUtil.getColumnIndexOrThrow(_cursor, "currentPhase");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfEstimatedEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "estimatedEndDate");
          final int _cursorIndexOfActualEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "actualEndDate");
          final int _cursorIndexOfTotalBudget = CursorUtil.getColumnIndexOrThrow(_cursor, "totalBudget");
          final int _cursorIndexOfCurrentCost = CursorUtil.getColumnIndexOrThrow(_cursor, "currentCost");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<ProjectEntity> _result = new ArrayList<ProjectEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ProjectEntity _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpAddress;
            if (_cursor.isNull(_cursorIndexOfAddress)) {
              _tmpAddress = null;
            } else {
              _tmpAddress = _cursor.getString(_cursorIndexOfAddress);
            }
            final String _tmpCity;
            if (_cursor.isNull(_cursorIndexOfCity)) {
              _tmpCity = null;
            } else {
              _tmpCity = _cursor.getString(_cursorIndexOfCity);
            }
            final String _tmpState;
            if (_cursor.isNull(_cursorIndexOfState)) {
              _tmpState = null;
            } else {
              _tmpState = _cursor.getString(_cursorIndexOfState);
            }
            final String _tmpZipCode;
            if (_cursor.isNull(_cursorIndexOfZipCode)) {
              _tmpZipCode = null;
            } else {
              _tmpZipCode = _cursor.getString(_cursorIndexOfZipCode);
            }
            final String _tmpClientName;
            if (_cursor.isNull(_cursorIndexOfClientName)) {
              _tmpClientName = null;
            } else {
              _tmpClientName = _cursor.getString(_cursorIndexOfClientName);
            }
            final String _tmpClientEmail;
            if (_cursor.isNull(_cursorIndexOfClientEmail)) {
              _tmpClientEmail = null;
            } else {
              _tmpClientEmail = _cursor.getString(_cursorIndexOfClientEmail);
            }
            final String _tmpClientPhone;
            if (_cursor.isNull(_cursorIndexOfClientPhone)) {
              _tmpClientPhone = null;
            } else {
              _tmpClientPhone = _cursor.getString(_cursorIndexOfClientPhone);
            }
            final ProjectType _tmpProjectType;
            _tmpProjectType = __ProjectType_stringToEnum(_cursor.getString(_cursorIndexOfProjectType));
            final ConstructionPhase _tmpCurrentPhase;
            _tmpCurrentPhase = __ConstructionPhase_stringToEnum(_cursor.getString(_cursorIndexOfCurrentPhase));
            final String _tmpStartDate;
            if (_cursor.isNull(_cursorIndexOfStartDate)) {
              _tmpStartDate = null;
            } else {
              _tmpStartDate = _cursor.getString(_cursorIndexOfStartDate);
            }
            final String _tmpEstimatedEndDate;
            if (_cursor.isNull(_cursorIndexOfEstimatedEndDate)) {
              _tmpEstimatedEndDate = null;
            } else {
              _tmpEstimatedEndDate = _cursor.getString(_cursorIndexOfEstimatedEndDate);
            }
            final String _tmpActualEndDate;
            if (_cursor.isNull(_cursorIndexOfActualEndDate)) {
              _tmpActualEndDate = null;
            } else {
              _tmpActualEndDate = _cursor.getString(_cursorIndexOfActualEndDate);
            }
            final String _tmpTotalBudget;
            if (_cursor.isNull(_cursorIndexOfTotalBudget)) {
              _tmpTotalBudget = null;
            } else {
              _tmpTotalBudget = _cursor.getString(_cursorIndexOfTotalBudget);
            }
            final String _tmpCurrentCost;
            if (_cursor.isNull(_cursorIndexOfCurrentCost)) {
              _tmpCurrentCost = null;
            } else {
              _tmpCurrentCost = _cursor.getString(_cursorIndexOfCurrentCost);
            }
            final ProjectStatus _tmpStatus;
            _tmpStatus = __ProjectStatus_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final String _tmpCreatedAt;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreatedAt = null;
            } else {
              _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            final String _tmpUpdatedAt;
            if (_cursor.isNull(_cursorIndexOfUpdatedAt)) {
              _tmpUpdatedAt = null;
            } else {
              _tmpUpdatedAt = _cursor.getString(_cursorIndexOfUpdatedAt);
            }
            _item = new ProjectEntity(_tmpId,_tmpName,_tmpAddress,_tmpCity,_tmpState,_tmpZipCode,_tmpClientName,_tmpClientEmail,_tmpClientPhone,_tmpProjectType,_tmpCurrentPhase,_tmpStartDate,_tmpEstimatedEndDate,_tmpActualEndDate,_tmpTotalBudget,_tmpCurrentCost,_tmpStatus,_tmpNotes,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object getProjectById(final String projectId,
      final Continuation<? super ProjectEntity> $completion) {
    final String _sql = "SELECT * FROM projects WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (projectId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, projectId);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ProjectEntity>() {
      @Override
      @Nullable
      public ProjectEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "address");
          final int _cursorIndexOfCity = CursorUtil.getColumnIndexOrThrow(_cursor, "city");
          final int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
          final int _cursorIndexOfZipCode = CursorUtil.getColumnIndexOrThrow(_cursor, "zipCode");
          final int _cursorIndexOfClientName = CursorUtil.getColumnIndexOrThrow(_cursor, "clientName");
          final int _cursorIndexOfClientEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "clientEmail");
          final int _cursorIndexOfClientPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "clientPhone");
          final int _cursorIndexOfProjectType = CursorUtil.getColumnIndexOrThrow(_cursor, "projectType");
          final int _cursorIndexOfCurrentPhase = CursorUtil.getColumnIndexOrThrow(_cursor, "currentPhase");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfEstimatedEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "estimatedEndDate");
          final int _cursorIndexOfActualEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "actualEndDate");
          final int _cursorIndexOfTotalBudget = CursorUtil.getColumnIndexOrThrow(_cursor, "totalBudget");
          final int _cursorIndexOfCurrentCost = CursorUtil.getColumnIndexOrThrow(_cursor, "currentCost");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final ProjectEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpAddress;
            if (_cursor.isNull(_cursorIndexOfAddress)) {
              _tmpAddress = null;
            } else {
              _tmpAddress = _cursor.getString(_cursorIndexOfAddress);
            }
            final String _tmpCity;
            if (_cursor.isNull(_cursorIndexOfCity)) {
              _tmpCity = null;
            } else {
              _tmpCity = _cursor.getString(_cursorIndexOfCity);
            }
            final String _tmpState;
            if (_cursor.isNull(_cursorIndexOfState)) {
              _tmpState = null;
            } else {
              _tmpState = _cursor.getString(_cursorIndexOfState);
            }
            final String _tmpZipCode;
            if (_cursor.isNull(_cursorIndexOfZipCode)) {
              _tmpZipCode = null;
            } else {
              _tmpZipCode = _cursor.getString(_cursorIndexOfZipCode);
            }
            final String _tmpClientName;
            if (_cursor.isNull(_cursorIndexOfClientName)) {
              _tmpClientName = null;
            } else {
              _tmpClientName = _cursor.getString(_cursorIndexOfClientName);
            }
            final String _tmpClientEmail;
            if (_cursor.isNull(_cursorIndexOfClientEmail)) {
              _tmpClientEmail = null;
            } else {
              _tmpClientEmail = _cursor.getString(_cursorIndexOfClientEmail);
            }
            final String _tmpClientPhone;
            if (_cursor.isNull(_cursorIndexOfClientPhone)) {
              _tmpClientPhone = null;
            } else {
              _tmpClientPhone = _cursor.getString(_cursorIndexOfClientPhone);
            }
            final ProjectType _tmpProjectType;
            _tmpProjectType = __ProjectType_stringToEnum(_cursor.getString(_cursorIndexOfProjectType));
            final ConstructionPhase _tmpCurrentPhase;
            _tmpCurrentPhase = __ConstructionPhase_stringToEnum(_cursor.getString(_cursorIndexOfCurrentPhase));
            final String _tmpStartDate;
            if (_cursor.isNull(_cursorIndexOfStartDate)) {
              _tmpStartDate = null;
            } else {
              _tmpStartDate = _cursor.getString(_cursorIndexOfStartDate);
            }
            final String _tmpEstimatedEndDate;
            if (_cursor.isNull(_cursorIndexOfEstimatedEndDate)) {
              _tmpEstimatedEndDate = null;
            } else {
              _tmpEstimatedEndDate = _cursor.getString(_cursorIndexOfEstimatedEndDate);
            }
            final String _tmpActualEndDate;
            if (_cursor.isNull(_cursorIndexOfActualEndDate)) {
              _tmpActualEndDate = null;
            } else {
              _tmpActualEndDate = _cursor.getString(_cursorIndexOfActualEndDate);
            }
            final String _tmpTotalBudget;
            if (_cursor.isNull(_cursorIndexOfTotalBudget)) {
              _tmpTotalBudget = null;
            } else {
              _tmpTotalBudget = _cursor.getString(_cursorIndexOfTotalBudget);
            }
            final String _tmpCurrentCost;
            if (_cursor.isNull(_cursorIndexOfCurrentCost)) {
              _tmpCurrentCost = null;
            } else {
              _tmpCurrentCost = _cursor.getString(_cursorIndexOfCurrentCost);
            }
            final ProjectStatus _tmpStatus;
            _tmpStatus = __ProjectStatus_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final String _tmpCreatedAt;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreatedAt = null;
            } else {
              _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            final String _tmpUpdatedAt;
            if (_cursor.isNull(_cursorIndexOfUpdatedAt)) {
              _tmpUpdatedAt = null;
            } else {
              _tmpUpdatedAt = _cursor.getString(_cursorIndexOfUpdatedAt);
            }
            _result = new ProjectEntity(_tmpId,_tmpName,_tmpAddress,_tmpCity,_tmpState,_tmpZipCode,_tmpClientName,_tmpClientEmail,_tmpClientPhone,_tmpProjectType,_tmpCurrentPhase,_tmpStartDate,_tmpEstimatedEndDate,_tmpActualEndDate,_tmpTotalBudget,_tmpCurrentCost,_tmpStatus,_tmpNotes,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Flow<List<ProjectEntity>> searchProjects(final String searchQuery) {
    final String _sql = "SELECT * FROM projects WHERE name LIKE '%' || ? || '%' OR address LIKE '%' || ? || '%' OR clientName LIKE '%' || ? || '%'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    if (searchQuery == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, searchQuery);
    }
    _argIndex = 2;
    if (searchQuery == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, searchQuery);
    }
    _argIndex = 3;
    if (searchQuery == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, searchQuery);
    }
    return CoroutinesRoom.createFlow(__db, false, new String[] {"projects"}, new Callable<List<ProjectEntity>>() {
      @Override
      @NonNull
      public List<ProjectEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "address");
          final int _cursorIndexOfCity = CursorUtil.getColumnIndexOrThrow(_cursor, "city");
          final int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
          final int _cursorIndexOfZipCode = CursorUtil.getColumnIndexOrThrow(_cursor, "zipCode");
          final int _cursorIndexOfClientName = CursorUtil.getColumnIndexOrThrow(_cursor, "clientName");
          final int _cursorIndexOfClientEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "clientEmail");
          final int _cursorIndexOfClientPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "clientPhone");
          final int _cursorIndexOfProjectType = CursorUtil.getColumnIndexOrThrow(_cursor, "projectType");
          final int _cursorIndexOfCurrentPhase = CursorUtil.getColumnIndexOrThrow(_cursor, "currentPhase");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfEstimatedEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "estimatedEndDate");
          final int _cursorIndexOfActualEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "actualEndDate");
          final int _cursorIndexOfTotalBudget = CursorUtil.getColumnIndexOrThrow(_cursor, "totalBudget");
          final int _cursorIndexOfCurrentCost = CursorUtil.getColumnIndexOrThrow(_cursor, "currentCost");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<ProjectEntity> _result = new ArrayList<ProjectEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ProjectEntity _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpAddress;
            if (_cursor.isNull(_cursorIndexOfAddress)) {
              _tmpAddress = null;
            } else {
              _tmpAddress = _cursor.getString(_cursorIndexOfAddress);
            }
            final String _tmpCity;
            if (_cursor.isNull(_cursorIndexOfCity)) {
              _tmpCity = null;
            } else {
              _tmpCity = _cursor.getString(_cursorIndexOfCity);
            }
            final String _tmpState;
            if (_cursor.isNull(_cursorIndexOfState)) {
              _tmpState = null;
            } else {
              _tmpState = _cursor.getString(_cursorIndexOfState);
            }
            final String _tmpZipCode;
            if (_cursor.isNull(_cursorIndexOfZipCode)) {
              _tmpZipCode = null;
            } else {
              _tmpZipCode = _cursor.getString(_cursorIndexOfZipCode);
            }
            final String _tmpClientName;
            if (_cursor.isNull(_cursorIndexOfClientName)) {
              _tmpClientName = null;
            } else {
              _tmpClientName = _cursor.getString(_cursorIndexOfClientName);
            }
            final String _tmpClientEmail;
            if (_cursor.isNull(_cursorIndexOfClientEmail)) {
              _tmpClientEmail = null;
            } else {
              _tmpClientEmail = _cursor.getString(_cursorIndexOfClientEmail);
            }
            final String _tmpClientPhone;
            if (_cursor.isNull(_cursorIndexOfClientPhone)) {
              _tmpClientPhone = null;
            } else {
              _tmpClientPhone = _cursor.getString(_cursorIndexOfClientPhone);
            }
            final ProjectType _tmpProjectType;
            _tmpProjectType = __ProjectType_stringToEnum(_cursor.getString(_cursorIndexOfProjectType));
            final ConstructionPhase _tmpCurrentPhase;
            _tmpCurrentPhase = __ConstructionPhase_stringToEnum(_cursor.getString(_cursorIndexOfCurrentPhase));
            final String _tmpStartDate;
            if (_cursor.isNull(_cursorIndexOfStartDate)) {
              _tmpStartDate = null;
            } else {
              _tmpStartDate = _cursor.getString(_cursorIndexOfStartDate);
            }
            final String _tmpEstimatedEndDate;
            if (_cursor.isNull(_cursorIndexOfEstimatedEndDate)) {
              _tmpEstimatedEndDate = null;
            } else {
              _tmpEstimatedEndDate = _cursor.getString(_cursorIndexOfEstimatedEndDate);
            }
            final String _tmpActualEndDate;
            if (_cursor.isNull(_cursorIndexOfActualEndDate)) {
              _tmpActualEndDate = null;
            } else {
              _tmpActualEndDate = _cursor.getString(_cursorIndexOfActualEndDate);
            }
            final String _tmpTotalBudget;
            if (_cursor.isNull(_cursorIndexOfTotalBudget)) {
              _tmpTotalBudget = null;
            } else {
              _tmpTotalBudget = _cursor.getString(_cursorIndexOfTotalBudget);
            }
            final String _tmpCurrentCost;
            if (_cursor.isNull(_cursorIndexOfCurrentCost)) {
              _tmpCurrentCost = null;
            } else {
              _tmpCurrentCost = _cursor.getString(_cursorIndexOfCurrentCost);
            }
            final ProjectStatus _tmpStatus;
            _tmpStatus = __ProjectStatus_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final String _tmpCreatedAt;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreatedAt = null;
            } else {
              _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            final String _tmpUpdatedAt;
            if (_cursor.isNull(_cursorIndexOfUpdatedAt)) {
              _tmpUpdatedAt = null;
            } else {
              _tmpUpdatedAt = _cursor.getString(_cursorIndexOfUpdatedAt);
            }
            _item = new ProjectEntity(_tmpId,_tmpName,_tmpAddress,_tmpCity,_tmpState,_tmpZipCode,_tmpClientName,_tmpClientEmail,_tmpClientPhone,_tmpProjectType,_tmpCurrentPhase,_tmpStartDate,_tmpEstimatedEndDate,_tmpActualEndDate,_tmpTotalBudget,_tmpCurrentCost,_tmpStatus,_tmpNotes,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object getProjectCountByStatus(final ProjectStatus status,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM projects WHERE status = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, __ProjectStatus_enumToString(status));
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp;
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
  public Object getTotalBudgetByStatuses(final List<? extends ProjectStatus> statuses,
      final Continuation<? super Double> $completion) {
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT SUM(CAST(totalBudget AS REAL)) FROM projects WHERE status IN (");
    final int _inputSize = statuses.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (ProjectStatus _item : statuses) {
      _statement.bindString(_argIndex, __ProjectStatus_enumToString(_item));
      _argIndex++;
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Double>() {
      @Override
      @Nullable
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final Double _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getDouble(0);
            }
            _result = _tmp;
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
  public Object getTotalCurrentCostByStatuses(final List<? extends ProjectStatus> statuses,
      final Continuation<? super Double> $completion) {
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT SUM(CAST(currentCost AS REAL)) FROM projects WHERE status IN (");
    final int _inputSize = statuses.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (ProjectStatus _item : statuses) {
      _statement.bindString(_argIndex, __ProjectStatus_enumToString(_item));
      _argIndex++;
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Double>() {
      @Override
      @Nullable
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final Double _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getDouble(0);
            }
            _result = _tmp;
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private String __ProjectType_enumToString(@NonNull final ProjectType _value) {
    switch (_value) {
      case NEW_CONSTRUCTION: return "NEW_CONSTRUCTION";
      case RENOVATION: return "RENOVATION";
      case ADDITION: return "ADDITION";
      case REPAIR: return "REPAIR";
      case COMMERCIAL: return "COMMERCIAL";
      case RESIDENTIAL: return "RESIDENTIAL";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private String __ConstructionPhase_enumToString(@NonNull final ConstructionPhase _value) {
    switch (_value) {
      case PRE_CONSTRUCTION: return "PRE_CONSTRUCTION";
      case SITE_PREPARATION: return "SITE_PREPARATION";
      case FOUNDATION: return "FOUNDATION";
      case FRAMING: return "FRAMING";
      case ROOFING: return "ROOFING";
      case SIDING_EXTERIOR: return "SIDING_EXTERIOR";
      case MEP_ROUGH_IN: return "MEP_ROUGH_IN";
      case INSULATION: return "INSULATION";
      case DRYWALL: return "DRYWALL";
      case FLOORING: return "FLOORING";
      case INTERIOR_FINISH: return "INTERIOR_FINISH";
      case FINAL_INSPECTION: return "FINAL_INSPECTION";
      case HANDOVER: return "HANDOVER";
      case COMPLETED: return "COMPLETED";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private String __ProjectStatus_enumToString(@NonNull final ProjectStatus _value) {
    switch (_value) {
      case PLANNING: return "PLANNING";
      case ACTIVE: return "ACTIVE";
      case ON_HOLD: return "ON_HOLD";
      case COMPLETED: return "COMPLETED";
      case CANCELLED: return "CANCELLED";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private ProjectType __ProjectType_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "NEW_CONSTRUCTION": return ProjectType.NEW_CONSTRUCTION;
      case "RENOVATION": return ProjectType.RENOVATION;
      case "ADDITION": return ProjectType.ADDITION;
      case "REPAIR": return ProjectType.REPAIR;
      case "COMMERCIAL": return ProjectType.COMMERCIAL;
      case "RESIDENTIAL": return ProjectType.RESIDENTIAL;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }

  private ConstructionPhase __ConstructionPhase_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "PRE_CONSTRUCTION": return ConstructionPhase.PRE_CONSTRUCTION;
      case "SITE_PREPARATION": return ConstructionPhase.SITE_PREPARATION;
      case "FOUNDATION": return ConstructionPhase.FOUNDATION;
      case "FRAMING": return ConstructionPhase.FRAMING;
      case "ROOFING": return ConstructionPhase.ROOFING;
      case "SIDING_EXTERIOR": return ConstructionPhase.SIDING_EXTERIOR;
      case "MEP_ROUGH_IN": return ConstructionPhase.MEP_ROUGH_IN;
      case "INSULATION": return ConstructionPhase.INSULATION;
      case "DRYWALL": return ConstructionPhase.DRYWALL;
      case "FLOORING": return ConstructionPhase.FLOORING;
      case "INTERIOR_FINISH": return ConstructionPhase.INTERIOR_FINISH;
      case "FINAL_INSPECTION": return ConstructionPhase.FINAL_INSPECTION;
      case "HANDOVER": return ConstructionPhase.HANDOVER;
      case "COMPLETED": return ConstructionPhase.COMPLETED;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }

  private ProjectStatus __ProjectStatus_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "PLANNING": return ProjectStatus.PLANNING;
      case "ACTIVE": return ProjectStatus.ACTIVE;
      case "ON_HOLD": return ProjectStatus.ON_HOLD;
      case "COMPLETED": return ProjectStatus.COMPLETED;
      case "CANCELLED": return ProjectStatus.CANCELLED;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
