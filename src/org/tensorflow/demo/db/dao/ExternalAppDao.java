package org.tensorflow.demo.db.dao;

/**
 * Created by deg032 on 1/2/18.
 */
import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import org.tensorflow.demo.db.entity.ExternalAppEntity;

import java.util.List;

@Dao
public interface ExternalAppDao {
    @Query("SELECT * FROM externalApps")
    LiveData<List<ExternalAppEntity>> loadAllApps();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ExternalAppEntity> externalApps);

    @Query("select * from externalApps where id = :appId")
    LiveData<ExternalAppEntity> loadApp(int appId);

    @Query("select * from externalApps where id = :appId")
    ExternalAppEntity loadAppSync(int appId);
}

