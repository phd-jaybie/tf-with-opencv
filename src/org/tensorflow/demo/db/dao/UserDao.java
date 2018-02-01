package org.tensorflow.demo.db.dao;

/**
 * Created by deg032 on 1/2/18.
 */
import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import org.tensorflow.demo.db.entity.UserEntity;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users")
    LiveData<List<UserEntity>> loadAllUsers();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<UserEntity> users);

    @Query("select * from users where id = :userId")
    LiveData<UserEntity> loadUser(int userId);

    @Query("select * from users where id = :userId")
    UserEntity loadUserSync(int userId);
}
