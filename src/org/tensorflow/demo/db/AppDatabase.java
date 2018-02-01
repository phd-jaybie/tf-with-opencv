package org.tensorflow.demo.db;

/**
 * Created by deg032 on 1/2/18.
 */


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import org.tensorflow.demo.db.dao.ExternalAppDao;
import org.tensorflow.demo.db.dao.ObjectDao;
import org.tensorflow.demo.db.dao.UserDao;
import org.tensorflow.demo.db.entity.ExternalAppEntity;
import org.tensorflow.demo.db.entity.ObjectEntity;
import org.tensorflow.demo.db.entity.UserEntity;

import java.util.List;

@Database(entities = {ObjectEntity.class, UserEntity.class, ExternalAppEntity.class}, version = 1)
//@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase sInstance;

    @VisibleForTesting
    public static final String DATABASE_NAME = "sample-db";

    public abstract ObjectDao objectDao();

    public abstract UserDao userDao();

    public abstract ExternalAppDao appDao();

    private final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();

    public static AppDatabase getInstance(final Context context) {
        if (sInstance == null) {
            synchronized (AppDatabase.class) {
                if (sInstance == null) {
                    sInstance = buildDatabase(context.getApplicationContext());
                    sInstance.updateDatabaseCreated(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    private static AppDatabase buildDatabase(final Context appContext) {
        return Room.databaseBuilder(appContext, AppDatabase.class, DATABASE_NAME)
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        AppDatabase database = AppDatabase.getInstance(appContext);
                        /**
                         * If you had some initial data/table, you may insert it to the database as
                         * follows.
                         */
                        List<ObjectEntity> objects = DataGenerator.generateObjects();
                        List<UserEntity> users =
                                DataGenerator.generateUsers(objects);
                        List<ExternalAppEntity> apps =
                                DataGenerator.generateApps(objects);

                        insertData(database, objects, users, apps);

                        // notify that the database was created and it's ready to be used
                        database.setDatabaseCreated();
                    }
                }).build();
    }

    private void updateDatabaseCreated(final Context context) {
        if (context.getDatabasePath(DATABASE_NAME).exists()) {
            setDatabaseCreated();
        }
    }

    private void setDatabaseCreated(){
        mIsDatabaseCreated.postValue(true);
    }

    private static void insertData(final AppDatabase database, final List<ObjectEntity> objects,
                                   final List<UserEntity> users, final List<ExternalAppEntity> apps) {
        database.runInTransaction(
                new Runnable() {
                    @Override
                    public void run() {
                        database.objectDao().insertAll(objects);
                        database.userDao().insertAll(users);
                        database.appDao().insertAll(apps);
                    }
        });
    }


    public LiveData<Boolean> getDatabaseCreated() {
        return mIsDatabaseCreated;
    }
}
