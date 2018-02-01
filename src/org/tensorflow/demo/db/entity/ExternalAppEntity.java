package org.tensorflow.demo.db.entity;

/**
 * Created by deg032 on 1/2/18.
 */
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import org.tensorflow.demo.db.model.ExternalApp;

import java.util.List;

@Entity(tableName = "externalApps")
public class ExternalAppEntity implements ExternalApp{
    @PrimaryKey
    private int id;
    private String name;
    private String description;

    @Ignore
    private List<String> objectPermissions;

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

    @Override
    public List<String> getObjectPermissions() {
        return objectPermissions;
    }

    public void setObjectPermissions(List<String> objectPermissions) {
        this.objectPermissions = objectPermissions;
    }

    public ExternalAppEntity() {
    }

    public ExternalAppEntity(int id, String name, String description, List<String> objectPermissions) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.objectPermissions = objectPermissions;

    }

    public ExternalAppEntity(ExternalApp externalApp) {
        this.id = externalApp.getId();
        this.name = externalApp.getName();
        this.description = externalApp.getDescription();
        this.objectPermissions = externalApp.getObjectPermissions();
    }
}


