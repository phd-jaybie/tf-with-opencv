package org.tensorflow.demo.db.entity;

/**
 * Created by deg032 on 1/2/18.
 */

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import org.tensorflow.demo.db.model.Object;

@Entity(tableName = "objects")
public class ObjectEntity implements Object {
    @PrimaryKey
    private int id;
    private String name;
    private String description;
    private String permissions;
    private String privacyLabel;

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
    public String getPrivacyLabel() {
        return privacyLabel;
    }

    public void setPrivacyLabel(String privacyLabel) {
        this.privacyLabel = privacyLabel;
    }

    @Override
    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    public ObjectEntity() {
    }

    public ObjectEntity(int id, String name, String description, String permissions, String privacyLabel) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.permissions = permissions;
        this.privacyLabel = privacyLabel;
    }

    public ObjectEntity(Object object) {
        this.id = object.getId();
        this.name = object.getName();
        this.description = object.getDescription();
        this.permissions = object.getPermissions();
        this.privacyLabel = object.getPrivacyLabel();
    }
}
