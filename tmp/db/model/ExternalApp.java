package org.tensorflow.demo.db.model;

import java.util.List;

/**
 * Created by deg032 on 1/2/18.
 */

public interface ExternalApp {
    int getId();
    String getName();
    String getDescription();
    List<String> getObjectPermissions();
}
