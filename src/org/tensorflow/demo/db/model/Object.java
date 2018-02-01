package org.tensorflow.demo.db.model;

/**
 * Created by deg032 on 1/2/18.
 */

public interface Object {
    int getId();
    String getName();
    String getDescription();
    String getPrivacyLabel();
    String getPermissions();
}
