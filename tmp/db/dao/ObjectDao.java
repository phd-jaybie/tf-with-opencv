package org.tensorflow.demo.db.dao;

/**
 * Created by deg032 on 1/2/18.
 */
import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import org.tensorflow.demo.db.entity.ObjectEntity;

import java.util.List;

@Dao
public interface ObjectDao {
    @Query("SELECT * FROM objects")
    LiveData<List<ObjectEntity>> loadAllObjects();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ObjectEntity> objects);

    @Query("select * from objects where id = :objectId")
    LiveData<ObjectEntity> loadObject(int objectId);

    @Query("select * from objects where id = :objectId")
    ObjectEntity loadObjectSync(int objectId);
}
