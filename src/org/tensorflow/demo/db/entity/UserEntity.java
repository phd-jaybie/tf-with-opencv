package org.tensorflow.demo.db.entity;

/**
 * Created by deg032 on 1/2/18.
 */

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import org.tensorflow.demo.db.model.User;

@Entity(tableName = "users")
public class UserEntity implements User{
    @PrimaryKey
    private int id;
    private String name;
    private String description;

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UserEntity() {
    }

    public UserEntity(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;

    }

    public UserEntity(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.description = user.getDescription();
    }
}

