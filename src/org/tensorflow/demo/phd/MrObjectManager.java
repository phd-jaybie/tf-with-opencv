package org.tensorflow.demo.phd;

import org.tensorflow.demo.Classifier;
import org.tensorflow.demo.cv.CvDetector;
import org.tensorflow.demo.simulator.AppRandomizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by deg032 on 6/2/18.
 *
 * Note: To maintain the privacy of object/s and user/s, please have these objects only created by
 * by the system-level functions and not by external or third-party applications and/or services.
 */

public class MrObjectManager {

    // List of live objects that are detected.
    protected static List<MrObject> MrObjects = new ArrayList<>();

    static String[] sensitiveObjects = new String[] //high sensitivity objects
            {"person", "bed", "toilet", "laptop", "mouse","keyboard", "cell phone"};

    public class MrObject {

        private int id;
        private String name;
        //private String description;
        private String[] permissions;
        private String privacyLabel;

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }

        public String[] getPermissions() {
            return permissions;
        }

        public String getPrivacyLabel() {
            return privacyLabel;
        }

        public MrObject(final int id, final String name, final String[] permissions,
                        final String privacyLabel) {
            this.id = id;
            this.name = name;
            this.permissions = permissions;
            this.privacyLabel = privacyLabel;
        }

        public MrObject(final MrObject object) {
            this.id = object.getId();
            this.name = object.getName();
            this.permissions = object.getPermissions();
            this.privacyLabel = object.getPrivacyLabel();
        }

    }

    private boolean userPermitted(String appName, String object){
        //if permitted: return false;
        // if not:
        return true;
    }

    private String getPrivacylabel(String object){
        // Below is a simplistic object sensitivity labelling.
        if (Arrays.asList(sensitiveObjects).contains(object)) {
            return "PRIVATE";
        } else {
            return "PUBLIC";
        }
    }

    public void generateList(){
        // This generates an initial list of MrObjects that is associated to this user.
        // Ideally, it also generates a list with objects associated with certain apps and other
        // users.
        MrObjects = new ArrayList<>();
    }

    private void addMrObject(final MrObject object) {
        // You can insert a code that prevents duplicate insertions.
        MrObjects.add(object);
    }

    public void refreshList() {
        // This refreshes the list of live MrObjects.
        // We can practically remove objects that have been added but has not been accessed for a
        // while or those that are past their time to live.
    }

    public void processDetection(AppRandomizer.App app, Classifier.Recognition object) {
        // check user preferences of what is the supposed sensitivity of this object
        if (!userPermitted(app.getName(),object.getTitle())){
            // if app is not allowed to see this object type, return
            return;
        }

        // check object if in 'live' list

        // if not, add to live list
        String[] permissions = {app.getName()};
        String privacyLabel = getPrivacylabel(object.getTitle());
        addMrObject(new MrObject(MrObjects.size() + 1,object.getTitle(),permissions,privacyLabel));
    }

    public void processDetection(AppRandomizer.App app, CvDetector.Recognition object) {
        // check user preferences of what is the supposed sensitivity of this object
        if (!userPermitted(app.getName(),object.getTitle())){
            // if app is not allowed to see this object type, return
            return;
        }

        // check object if in 'live' list

        // if not, add to live list
        String[] permissions = {app.getName()};
        String privacyLabel = getPrivacylabel(object.getTitle());
        addMrObject(new MrObject(MrObjects.size() + 1,object.getTitle(),permissions,privacyLabel));
    }

    public void storeList() {
        // when the app is closed, store the list before it is destroyed.
    }
}
