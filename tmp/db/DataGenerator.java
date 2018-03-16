package org.tensorflow.demo.db;

/**
 * Created by deg032 on 1/2/18.
 */

import org.tensorflow.demo.db.entity.ExternalAppEntity;
import org.tensorflow.demo.db.entity.ObjectEntity;
import org.tensorflow.demo.db.entity.UserEntity;
import org.tensorflow.demo.db.model.Object;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates data to pre-populate the database
 */
public class DataGenerator {

    /**
     * These are just some sample randomly methods for populating the database.
     * Obviously, we want to populate the users, apps, and, most importantly, objects.
     */
    private static final String[] FIRST = new String[]{
            "Special edition", "New", "Cheap", "Quality", "Used"};
    private static final String[] SECOND = new String[]{
            "Three-headed Monkey", "Rubber Chicken", "Pint of Grog", "Monocle"};
    private static final String[] DESCRIPTION = new String[]{
            "is finally here", "is recommended by Stan S. Stanman",
            "is the best sold product on Mêlée Island", "is \uD83D\uDCAF", "is ❤️", "is fine"};
    private static final String[] COMMENTS = new String[]{
            "Comment 1", "Comment 2", "Comment 3", "Comment 4", "Comment 5", "Comment 6"};

    public static List<ObjectEntity> generateObjects() {
        List<ObjectEntity> objects = new ArrayList<>(FIRST.length * SECOND.length);
        //Random rnd = new Random();
        for (int i = 0; i < FIRST.length; i++) {
            for (int j = 0; j < SECOND.length; j++) {
                ObjectEntity object = new ObjectEntity();
                object.setName(FIRST[i] + " " + SECOND[j]);
                object.setDescription(object.getName() + " " + DESCRIPTION[j]);
                object.setPrivacyLabel("public");
                object.setId(FIRST.length * i + j + 1);
                objects.add(object);
            }
        }
        return objects;
    }

    public static List<UserEntity> generateUsers(
            final List<ObjectEntity> objects) {
        List<UserEntity> users = new ArrayList<>();
        Random rnd = new Random();

        for (Object object: objects) {
            UserEntity user = new UserEntity();
            user.setName("");
            user.setDescription("");
            user.setId(object.getId());
            users.add(user);
        }

        return users;
    }

    public static List<ExternalAppEntity> generateApps(
            final List<ObjectEntity> objects) {
        List<ExternalAppEntity> apps = new ArrayList<>();
        Random rnd = new Random();

        for (Object object: objects) {
            List<String> associatedObjects = new ArrayList<>();
            ExternalAppEntity app = new ExternalAppEntity();
            app.setName("" + Integer.toString(rnd.nextInt()));
            app.setDescription("");
            app.setObjectPermissions(associatedObjects);
            app.setId(object.getId()*rnd.nextInt(object.getId()));
            apps.add(app);
        }

        return apps;
    }
}