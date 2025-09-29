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
import androidx.sqlite.db.SupportSQLiteStatement;
import com.constructionmanager.data.database.entity.MaterialEntity;
import com.constructionmanager.domain.model.MaterialCategory;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
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
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class MaterialDao_Impl implements MaterialDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<MaterialEntity> __insertionAdapterOfMaterialEntity;

  private final EntityDeletionOrUpdateAdapter<MaterialEntity> __deletionAdapterOfMaterialEntity;

  private final EntityDeletionOrUpdateAdapter<MaterialEntity> __updateAdapterOfMaterialEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeactivateMaterial;

  public MaterialDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMaterialEntity = new EntityInsertionAdapter<MaterialEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `materials` (`id`,`name`,`category`,`subcategory`,`unitOfMeasurement`,`currentPrice`,`supplier`,`supplierSku`,`description`,`specifications`,`isActive`,`lastPriceUpdate`,`regionalPricing`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @Nullable final MaterialEntity entity) {
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
        statement.bindString(3, __MaterialCategory_enumToString(entity.getCategory()));
        if (entity.getSubcategory() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getSubcategory());
        }
        if (entity.getUnitOfMeasurement() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getUnitOfMeasurement());
        }
        if (entity.getCurrentPrice() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getCurrentPrice());
        }
        if (entity.getSupplier() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getSupplier());
        }
        if (entity.getSupplierSku() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getSupplierSku());
        }
        if (entity.getDescription() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getDescription());
        }
        if (entity.getSpecifications() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getSpecifications());
        }
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(11, _tmp);
        if (entity.getLastPriceUpdate() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getLastPriceUpdate());
        }
        if (entity.getRegionalPricing() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getRegionalPricing());
        }
      }
    };
    this.__deletionAdapterOfMaterialEntity = new EntityDeletionOrUpdateAdapter<MaterialEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `materials` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @Nullable final MaterialEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
      }
    };
    this.__updateAdapterOfMaterialEntity = new EntityDeletionOrUpdateAdapter<MaterialEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `materials` SET `id` = ?,`name` = ?,`category` = ?,`subcategory` = ?,`unitOfMeasurement` = ?,`currentPrice` = ?,`supplier` = ?,`supplierSku` = ?,`description` = ?,`specifications` = ?,`isActive` = ?,`lastPriceUpdate` = ?,`regionalPricing` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @Nullable final MaterialEntity entity) {
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
        statement.bindString(3, __MaterialCategory_enumToString(entity.getCategory()));
        if (entity.getSubcategory() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getSubcategory());
        }
        if (entity.getUnitOfMeasurement() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getUnitOfMeasurement());
        }
        if (entity.getCurrentPrice() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getCurrentPrice());
        }
        if (entity.getSupplier() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getSupplier());
        }
        if (entity.getSupplierSku() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getSupplierSku());
        }
        if (entity.getDescription() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getDescription());
        }
        if (entity.getSpecifications() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getSpecifications());
        }
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(11, _tmp);
        if (entity.getLastPriceUpdate() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getLastPriceUpdate());
        }
        if (entity.getRegionalPricing() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getRegionalPricing());
        }
        if (entity.getId() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getId());
        }
      }
    };
    this.__preparedStmtOfDeactivateMaterial = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE materials SET isActive = 0 WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertMaterial(final MaterialEntity material,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMaterialEntity.insert(material);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertMaterials(final List<MaterialEntity> materials,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMaterialEntity.insert(materials);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMaterial(final MaterialEntity material,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfMaterialEntity.handle(material);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateMaterial(final MaterialEntity material,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfMaterialEntity.handle(material);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deactivateMaterial(final String materialId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeactivateMaterial.acquire();
        int _argIndex = 1;
        if (materialId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, materialId);
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
          __preparedStmtOfDeactivateMaterial.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<MaterialEntity>> getAllActiveMaterials() {
    final String _sql = "SELECT * FROM materials WHERE isActive = 1 ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"materials"}, new Callable<List<MaterialEntity>>() {
      @Override
      @NonNull
      public List<MaterialEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfSubcategory = CursorUtil.getColumnIndexOrThrow(_cursor, "subcategory");
          final int _cursorIndexOfUnitOfMeasurement = CursorUtil.getColumnIndexOrThrow(_cursor, "unitOfMeasurement");
          final int _cursorIndexOfCurrentPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "currentPrice");
          final int _cursorIndexOfSupplier = CursorUtil.getColumnIndexOrThrow(_cursor, "supplier");
          final int _cursorIndexOfSupplierSku = CursorUtil.getColumnIndexOrThrow(_cursor, "supplierSku");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfSpecifications = CursorUtil.getColumnIndexOrThrow(_cursor, "specifications");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfLastPriceUpdate = CursorUtil.getColumnIndexOrThrow(_cursor, "lastPriceUpdate");
          final int _cursorIndexOfRegionalPricing = CursorUtil.getColumnIndexOrThrow(_cursor, "regionalPricing");
          final List<MaterialEntity> _result = new ArrayList<MaterialEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MaterialEntity _item;
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
            final MaterialCategory _tmpCategory;
            _tmpCategory = __MaterialCategory_stringToEnum(_cursor.getString(_cursorIndexOfCategory));
            final String _tmpSubcategory;
            if (_cursor.isNull(_cursorIndexOfSubcategory)) {
              _tmpSubcategory = null;
            } else {
              _tmpSubcategory = _cursor.getString(_cursorIndexOfSubcategory);
            }
            final String _tmpUnitOfMeasurement;
            if (_cursor.isNull(_cursorIndexOfUnitOfMeasurement)) {
              _tmpUnitOfMeasurement = null;
            } else {
              _tmpUnitOfMeasurement = _cursor.getString(_cursorIndexOfUnitOfMeasurement);
            }
            final String _tmpCurrentPrice;
            if (_cursor.isNull(_cursorIndexOfCurrentPrice)) {
              _tmpCurrentPrice = null;
            } else {
              _tmpCurrentPrice = _cursor.getString(_cursorIndexOfCurrentPrice);
            }
            final String _tmpSupplier;
            if (_cursor.isNull(_cursorIndexOfSupplier)) {
              _tmpSupplier = null;
            } else {
              _tmpSupplier = _cursor.getString(_cursorIndexOfSupplier);
            }
            final String _tmpSupplierSku;
            if (_cursor.isNull(_cursorIndexOfSupplierSku)) {
              _tmpSupplierSku = null;
            } else {
              _tmpSupplierSku = _cursor.getString(_cursorIndexOfSupplierSku);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpSpecifications;
            if (_cursor.isNull(_cursorIndexOfSpecifications)) {
              _tmpSpecifications = null;
            } else {
              _tmpSpecifications = _cursor.getString(_cursorIndexOfSpecifications);
            }
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final String _tmpLastPriceUpdate;
            if (_cursor.isNull(_cursorIndexOfLastPriceUpdate)) {
              _tmpLastPriceUpdate = null;
            } else {
              _tmpLastPriceUpdate = _cursor.getString(_cursorIndexOfLastPriceUpdate);
            }
            final String _tmpRegionalPricing;
            if (_cursor.isNull(_cursorIndexOfRegionalPricing)) {
              _tmpRegionalPricing = null;
            } else {
              _tmpRegionalPricing = _cursor.getString(_cursorIndexOfRegionalPricing);
            }
            _item = new MaterialEntity(_tmpId,_tmpName,_tmpCategory,_tmpSubcategory,_tmpUnitOfMeasurement,_tmpCurrentPrice,_tmpSupplier,_tmpSupplierSku,_tmpDescription,_tmpSpecifications,_tmpIsActive,_tmpLastPriceUpdate,_tmpRegionalPricing);
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
  public Flow<List<MaterialEntity>> getMaterialsByCategory(final MaterialCategory category) {
    final String _sql = "SELECT * FROM materials WHERE category = ? AND isActive = 1 ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, __MaterialCategory_enumToString(category));
    return CoroutinesRoom.createFlow(__db, false, new String[] {"materials"}, new Callable<List<MaterialEntity>>() {
      @Override
      @NonNull
      public List<MaterialEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfSubcategory = CursorUtil.getColumnIndexOrThrow(_cursor, "subcategory");
          final int _cursorIndexOfUnitOfMeasurement = CursorUtil.getColumnIndexOrThrow(_cursor, "unitOfMeasurement");
          final int _cursorIndexOfCurrentPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "currentPrice");
          final int _cursorIndexOfSupplier = CursorUtil.getColumnIndexOrThrow(_cursor, "supplier");
          final int _cursorIndexOfSupplierSku = CursorUtil.getColumnIndexOrThrow(_cursor, "supplierSku");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfSpecifications = CursorUtil.getColumnIndexOrThrow(_cursor, "specifications");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfLastPriceUpdate = CursorUtil.getColumnIndexOrThrow(_cursor, "lastPriceUpdate");
          final int _cursorIndexOfRegionalPricing = CursorUtil.getColumnIndexOrThrow(_cursor, "regionalPricing");
          final List<MaterialEntity> _result = new ArrayList<MaterialEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MaterialEntity _item;
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
            final MaterialCategory _tmpCategory;
            _tmpCategory = __MaterialCategory_stringToEnum(_cursor.getString(_cursorIndexOfCategory));
            final String _tmpSubcategory;
            if (_cursor.isNull(_cursorIndexOfSubcategory)) {
              _tmpSubcategory = null;
            } else {
              _tmpSubcategory = _cursor.getString(_cursorIndexOfSubcategory);
            }
            final String _tmpUnitOfMeasurement;
            if (_cursor.isNull(_cursorIndexOfUnitOfMeasurement)) {
              _tmpUnitOfMeasurement = null;
            } else {
              _tmpUnitOfMeasurement = _cursor.getString(_cursorIndexOfUnitOfMeasurement);
            }
            final String _tmpCurrentPrice;
            if (_cursor.isNull(_cursorIndexOfCurrentPrice)) {
              _tmpCurrentPrice = null;
            } else {
              _tmpCurrentPrice = _cursor.getString(_cursorIndexOfCurrentPrice);
            }
            final String _tmpSupplier;
            if (_cursor.isNull(_cursorIndexOfSupplier)) {
              _tmpSupplier = null;
            } else {
              _tmpSupplier = _cursor.getString(_cursorIndexOfSupplier);
            }
            final String _tmpSupplierSku;
            if (_cursor.isNull(_cursorIndexOfSupplierSku)) {
              _tmpSupplierSku = null;
            } else {
              _tmpSupplierSku = _cursor.getString(_cursorIndexOfSupplierSku);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpSpecifications;
            if (_cursor.isNull(_cursorIndexOfSpecifications)) {
              _tmpSpecifications = null;
            } else {
              _tmpSpecifications = _cursor.getString(_cursorIndexOfSpecifications);
            }
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final String _tmpLastPriceUpdate;
            if (_cursor.isNull(_cursorIndexOfLastPriceUpdate)) {
              _tmpLastPriceUpdate = null;
            } else {
              _tmpLastPriceUpdate = _cursor.getString(_cursorIndexOfLastPriceUpdate);
            }
            final String _tmpRegionalPricing;
            if (_cursor.isNull(_cursorIndexOfRegionalPricing)) {
              _tmpRegionalPricing = null;
            } else {
              _tmpRegionalPricing = _cursor.getString(_cursorIndexOfRegionalPricing);
            }
            _item = new MaterialEntity(_tmpId,_tmpName,_tmpCategory,_tmpSubcategory,_tmpUnitOfMeasurement,_tmpCurrentPrice,_tmpSupplier,_tmpSupplierSku,_tmpDescription,_tmpSpecifications,_tmpIsActive,_tmpLastPriceUpdate,_tmpRegionalPricing);
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
  public Object getMaterialById(final String materialId,
      final Continuation<? super MaterialEntity> $completion) {
    final String _sql = "SELECT * FROM materials WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (materialId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, materialId);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<MaterialEntity>() {
      @Override
      @Nullable
      public MaterialEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfSubcategory = CursorUtil.getColumnIndexOrThrow(_cursor, "subcategory");
          final int _cursorIndexOfUnitOfMeasurement = CursorUtil.getColumnIndexOrThrow(_cursor, "unitOfMeasurement");
          final int _cursorIndexOfCurrentPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "currentPrice");
          final int _cursorIndexOfSupplier = CursorUtil.getColumnIndexOrThrow(_cursor, "supplier");
          final int _cursorIndexOfSupplierSku = CursorUtil.getColumnIndexOrThrow(_cursor, "supplierSku");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfSpecifications = CursorUtil.getColumnIndexOrThrow(_cursor, "specifications");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfLastPriceUpdate = CursorUtil.getColumnIndexOrThrow(_cursor, "lastPriceUpdate");
          final int _cursorIndexOfRegionalPricing = CursorUtil.getColumnIndexOrThrow(_cursor, "regionalPricing");
          final MaterialEntity _result;
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
            final MaterialCategory _tmpCategory;
            _tmpCategory = __MaterialCategory_stringToEnum(_cursor.getString(_cursorIndexOfCategory));
            final String _tmpSubcategory;
            if (_cursor.isNull(_cursorIndexOfSubcategory)) {
              _tmpSubcategory = null;
            } else {
              _tmpSubcategory = _cursor.getString(_cursorIndexOfSubcategory);
            }
            final String _tmpUnitOfMeasurement;
            if (_cursor.isNull(_cursorIndexOfUnitOfMeasurement)) {
              _tmpUnitOfMeasurement = null;
            } else {
              _tmpUnitOfMeasurement = _cursor.getString(_cursorIndexOfUnitOfMeasurement);
            }
            final String _tmpCurrentPrice;
            if (_cursor.isNull(_cursorIndexOfCurrentPrice)) {
              _tmpCurrentPrice = null;
            } else {
              _tmpCurrentPrice = _cursor.getString(_cursorIndexOfCurrentPrice);
            }
            final String _tmpSupplier;
            if (_cursor.isNull(_cursorIndexOfSupplier)) {
              _tmpSupplier = null;
            } else {
              _tmpSupplier = _cursor.getString(_cursorIndexOfSupplier);
            }
            final String _tmpSupplierSku;
            if (_cursor.isNull(_cursorIndexOfSupplierSku)) {
              _tmpSupplierSku = null;
            } else {
              _tmpSupplierSku = _cursor.getString(_cursorIndexOfSupplierSku);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpSpecifications;
            if (_cursor.isNull(_cursorIndexOfSpecifications)) {
              _tmpSpecifications = null;
            } else {
              _tmpSpecifications = _cursor.getString(_cursorIndexOfSpecifications);
            }
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final String _tmpLastPriceUpdate;
            if (_cursor.isNull(_cursorIndexOfLastPriceUpdate)) {
              _tmpLastPriceUpdate = null;
            } else {
              _tmpLastPriceUpdate = _cursor.getString(_cursorIndexOfLastPriceUpdate);
            }
            final String _tmpRegionalPricing;
            if (_cursor.isNull(_cursorIndexOfRegionalPricing)) {
              _tmpRegionalPricing = null;
            } else {
              _tmpRegionalPricing = _cursor.getString(_cursorIndexOfRegionalPricing);
            }
            _result = new MaterialEntity(_tmpId,_tmpName,_tmpCategory,_tmpSubcategory,_tmpUnitOfMeasurement,_tmpCurrentPrice,_tmpSupplier,_tmpSupplierSku,_tmpDescription,_tmpSpecifications,_tmpIsActive,_tmpLastPriceUpdate,_tmpRegionalPricing);
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
  public Flow<List<MaterialEntity>> searchMaterials(final String searchQuery) {
    final String _sql = "SELECT * FROM materials WHERE name LIKE '%' || ? || '%' OR description LIKE '%' || ? || '%' AND isActive = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
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
    return CoroutinesRoom.createFlow(__db, false, new String[] {"materials"}, new Callable<List<MaterialEntity>>() {
      @Override
      @NonNull
      public List<MaterialEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfSubcategory = CursorUtil.getColumnIndexOrThrow(_cursor, "subcategory");
          final int _cursorIndexOfUnitOfMeasurement = CursorUtil.getColumnIndexOrThrow(_cursor, "unitOfMeasurement");
          final int _cursorIndexOfCurrentPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "currentPrice");
          final int _cursorIndexOfSupplier = CursorUtil.getColumnIndexOrThrow(_cursor, "supplier");
          final int _cursorIndexOfSupplierSku = CursorUtil.getColumnIndexOrThrow(_cursor, "supplierSku");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfSpecifications = CursorUtil.getColumnIndexOrThrow(_cursor, "specifications");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfLastPriceUpdate = CursorUtil.getColumnIndexOrThrow(_cursor, "lastPriceUpdate");
          final int _cursorIndexOfRegionalPricing = CursorUtil.getColumnIndexOrThrow(_cursor, "regionalPricing");
          final List<MaterialEntity> _result = new ArrayList<MaterialEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MaterialEntity _item;
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
            final MaterialCategory _tmpCategory;
            _tmpCategory = __MaterialCategory_stringToEnum(_cursor.getString(_cursorIndexOfCategory));
            final String _tmpSubcategory;
            if (_cursor.isNull(_cursorIndexOfSubcategory)) {
              _tmpSubcategory = null;
            } else {
              _tmpSubcategory = _cursor.getString(_cursorIndexOfSubcategory);
            }
            final String _tmpUnitOfMeasurement;
            if (_cursor.isNull(_cursorIndexOfUnitOfMeasurement)) {
              _tmpUnitOfMeasurement = null;
            } else {
              _tmpUnitOfMeasurement = _cursor.getString(_cursorIndexOfUnitOfMeasurement);
            }
            final String _tmpCurrentPrice;
            if (_cursor.isNull(_cursorIndexOfCurrentPrice)) {
              _tmpCurrentPrice = null;
            } else {
              _tmpCurrentPrice = _cursor.getString(_cursorIndexOfCurrentPrice);
            }
            final String _tmpSupplier;
            if (_cursor.isNull(_cursorIndexOfSupplier)) {
              _tmpSupplier = null;
            } else {
              _tmpSupplier = _cursor.getString(_cursorIndexOfSupplier);
            }
            final String _tmpSupplierSku;
            if (_cursor.isNull(_cursorIndexOfSupplierSku)) {
              _tmpSupplierSku = null;
            } else {
              _tmpSupplierSku = _cursor.getString(_cursorIndexOfSupplierSku);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpSpecifications;
            if (_cursor.isNull(_cursorIndexOfSpecifications)) {
              _tmpSpecifications = null;
            } else {
              _tmpSpecifications = _cursor.getString(_cursorIndexOfSpecifications);
            }
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final String _tmpLastPriceUpdate;
            if (_cursor.isNull(_cursorIndexOfLastPriceUpdate)) {
              _tmpLastPriceUpdate = null;
            } else {
              _tmpLastPriceUpdate = _cursor.getString(_cursorIndexOfLastPriceUpdate);
            }
            final String _tmpRegionalPricing;
            if (_cursor.isNull(_cursorIndexOfRegionalPricing)) {
              _tmpRegionalPricing = null;
            } else {
              _tmpRegionalPricing = _cursor.getString(_cursorIndexOfRegionalPricing);
            }
            _item = new MaterialEntity(_tmpId,_tmpName,_tmpCategory,_tmpSubcategory,_tmpUnitOfMeasurement,_tmpCurrentPrice,_tmpSupplier,_tmpSupplierSku,_tmpDescription,_tmpSpecifications,_tmpIsActive,_tmpLastPriceUpdate,_tmpRegionalPricing);
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
  public Flow<List<MaterialEntity>> getMaterialsBySupplier(final String supplier) {
    final String _sql = "SELECT * FROM materials WHERE supplier = ? AND isActive = 1 ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (supplier == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, supplier);
    }
    return CoroutinesRoom.createFlow(__db, false, new String[] {"materials"}, new Callable<List<MaterialEntity>>() {
      @Override
      @NonNull
      public List<MaterialEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfSubcategory = CursorUtil.getColumnIndexOrThrow(_cursor, "subcategory");
          final int _cursorIndexOfUnitOfMeasurement = CursorUtil.getColumnIndexOrThrow(_cursor, "unitOfMeasurement");
          final int _cursorIndexOfCurrentPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "currentPrice");
          final int _cursorIndexOfSupplier = CursorUtil.getColumnIndexOrThrow(_cursor, "supplier");
          final int _cursorIndexOfSupplierSku = CursorUtil.getColumnIndexOrThrow(_cursor, "supplierSku");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfSpecifications = CursorUtil.getColumnIndexOrThrow(_cursor, "specifications");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfLastPriceUpdate = CursorUtil.getColumnIndexOrThrow(_cursor, "lastPriceUpdate");
          final int _cursorIndexOfRegionalPricing = CursorUtil.getColumnIndexOrThrow(_cursor, "regionalPricing");
          final List<MaterialEntity> _result = new ArrayList<MaterialEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MaterialEntity _item;
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
            final MaterialCategory _tmpCategory;
            _tmpCategory = __MaterialCategory_stringToEnum(_cursor.getString(_cursorIndexOfCategory));
            final String _tmpSubcategory;
            if (_cursor.isNull(_cursorIndexOfSubcategory)) {
              _tmpSubcategory = null;
            } else {
              _tmpSubcategory = _cursor.getString(_cursorIndexOfSubcategory);
            }
            final String _tmpUnitOfMeasurement;
            if (_cursor.isNull(_cursorIndexOfUnitOfMeasurement)) {
              _tmpUnitOfMeasurement = null;
            } else {
              _tmpUnitOfMeasurement = _cursor.getString(_cursorIndexOfUnitOfMeasurement);
            }
            final String _tmpCurrentPrice;
            if (_cursor.isNull(_cursorIndexOfCurrentPrice)) {
              _tmpCurrentPrice = null;
            } else {
              _tmpCurrentPrice = _cursor.getString(_cursorIndexOfCurrentPrice);
            }
            final String _tmpSupplier;
            if (_cursor.isNull(_cursorIndexOfSupplier)) {
              _tmpSupplier = null;
            } else {
              _tmpSupplier = _cursor.getString(_cursorIndexOfSupplier);
            }
            final String _tmpSupplierSku;
            if (_cursor.isNull(_cursorIndexOfSupplierSku)) {
              _tmpSupplierSku = null;
            } else {
              _tmpSupplierSku = _cursor.getString(_cursorIndexOfSupplierSku);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpSpecifications;
            if (_cursor.isNull(_cursorIndexOfSpecifications)) {
              _tmpSpecifications = null;
            } else {
              _tmpSpecifications = _cursor.getString(_cursorIndexOfSpecifications);
            }
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final String _tmpLastPriceUpdate;
            if (_cursor.isNull(_cursorIndexOfLastPriceUpdate)) {
              _tmpLastPriceUpdate = null;
            } else {
              _tmpLastPriceUpdate = _cursor.getString(_cursorIndexOfLastPriceUpdate);
            }
            final String _tmpRegionalPricing;
            if (_cursor.isNull(_cursorIndexOfRegionalPricing)) {
              _tmpRegionalPricing = null;
            } else {
              _tmpRegionalPricing = _cursor.getString(_cursorIndexOfRegionalPricing);
            }
            _item = new MaterialEntity(_tmpId,_tmpName,_tmpCategory,_tmpSubcategory,_tmpUnitOfMeasurement,_tmpCurrentPrice,_tmpSupplier,_tmpSupplierSku,_tmpDescription,_tmpSpecifications,_tmpIsActive,_tmpLastPriceUpdate,_tmpRegionalPricing);
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
  public Object getAllCategories(
      final Continuation<? super List<? extends MaterialCategory>> $completion) {
    final String _sql = "SELECT DISTINCT category FROM materials WHERE isActive = 1 ORDER BY category ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<? extends MaterialCategory>>() {
      @Override
      @NonNull
      public List<? extends MaterialCategory> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final List<MaterialCategory> _result = new ArrayList<MaterialCategory>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MaterialCategory _item;
            _item = __MaterialCategory_stringToEnum(_cursor.getString(0));
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
  public Object getAllSuppliers(final Continuation<? super List<String>> $completion) {
    final String _sql = "SELECT DISTINCT supplier FROM materials WHERE isActive = 1 ORDER BY supplier ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<String>>() {
      @Override
      @NonNull
      public List<String> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final List<String> _result = new ArrayList<String>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final String _item;
            if (_cursor.isNull(0)) {
              _item = null;
            } else {
              _item = _cursor.getString(0);
            }
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

  private String __MaterialCategory_enumToString(@NonNull final MaterialCategory _value) {
    switch (_value) {
      case LUMBER: return "LUMBER";
      case CONCRETE: return "CONCRETE";
      case STEEL: return "STEEL";
      case ROOFING: return "ROOFING";
      case INSULATION: return "INSULATION";
      case DRYWALL: return "DRYWALL";
      case FLOORING: return "FLOORING";
      case PLUMBING: return "PLUMBING";
      case ELECTRICAL: return "ELECTRICAL";
      case HVAC: return "HVAC";
      case WINDOWS: return "WINDOWS";
      case DOORS: return "DOORS";
      case HARDWARE: return "HARDWARE";
      case FIXTURES: return "FIXTURES";
      case FINISHES: return "FINISHES";
      case TOOLS: return "TOOLS";
      case SAFETY_EQUIPMENT: return "SAFETY_EQUIPMENT";
      case LANDSCAPING: return "LANDSCAPING";
      case OTHER: return "OTHER";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private MaterialCategory __MaterialCategory_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "LUMBER": return MaterialCategory.LUMBER;
      case "CONCRETE": return MaterialCategory.CONCRETE;
      case "STEEL": return MaterialCategory.STEEL;
      case "ROOFING": return MaterialCategory.ROOFING;
      case "INSULATION": return MaterialCategory.INSULATION;
      case "DRYWALL": return MaterialCategory.DRYWALL;
      case "FLOORING": return MaterialCategory.FLOORING;
      case "PLUMBING": return MaterialCategory.PLUMBING;
      case "ELECTRICAL": return MaterialCategory.ELECTRICAL;
      case "HVAC": return MaterialCategory.HVAC;
      case "WINDOWS": return MaterialCategory.WINDOWS;
      case "DOORS": return MaterialCategory.DOORS;
      case "HARDWARE": return MaterialCategory.HARDWARE;
      case "FIXTURES": return MaterialCategory.FIXTURES;
      case "FINISHES": return MaterialCategory.FINISHES;
      case "TOOLS": return MaterialCategory.TOOLS;
      case "SAFETY_EQUIPMENT": return MaterialCategory.SAFETY_EQUIPMENT;
      case "LANDSCAPING": return MaterialCategory.LANDSCAPING;
      case "OTHER": return MaterialCategory.OTHER;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
