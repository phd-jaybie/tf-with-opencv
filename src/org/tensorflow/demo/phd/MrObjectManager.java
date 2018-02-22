package org.tensorflow.demo.phd;

import android.util.Xml;

import org.tensorflow.demo.Classifier;
import org.tensorflow.demo.network.NetworkFragment;
import org.tensorflow.demo.network.NetworkListener;
import org.tensorflow.demo.phd.detector.cv.CvDetector;
import org.tensorflow.demo.simulator.App;
import org.xmlpull.v1.XmlSerializer;

import java.io.StringWriter;
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

        private String name;
        //private String description;
        private String[] permissions;
        private String privacyLabel;

        public String getName() {
            return name;
        }

        public String[] getPermissions() {
            return permissions;
        }

        public String getPrivacyLabel() {
            return privacyLabel;
        }

        public MrObject(final String name, final String[] permissions,
                        final String privacyLabel) {
            this.name = name;
            this.permissions = permissions;
            this.privacyLabel = privacyLabel;
        }

        public MrObject(final MrObject object) {
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
        List<MrObject> fromStorage = new ArrayList<>();

        // Get list from storage and add it to the live list.
        MrObjects.addAll(fromStorage);

        // We can also create a new list of sensitive objects from the pre-saved list of private
        // objects of the user.
    }

    private void addMrObject(final MrObject object) {

        if (MrObjects.isEmpty()) MrObjects.add(object);
        else {
            for (MrObject mrObject : MrObjects){
                if (mrObject.getName() != object.getName()) MrObjects.add(object);
            }
        }
    }

    public void refreshList(){
        // This refreshes the list of live MrObjects.
        // We can practically remove objects that have been added but has not been accessed for a
        // while or those that are past their time to live.
    }

    public void refreshListFromNetwork(NetworkFragment networkFragment, NetworkListener networkListener) {

/*        // Check for received objects over the network.
        List<MrObject> fromNetwork = new ArrayList<>();
        fromNetwork = networkFragment.getObjects(networkListener);

        // Get list from storage and add it to the live list.
        for (MrObject nObject : fromNetwork) {
            for (MrObject mrObject : MrObjects){
                if (mrObject.getName() != nObject.getName()) MrObjects.add(nObject);
            }
        }

        // Then refresh list.
        refreshList();*/

        // Then share some public objects.
        List<MrObject> publicObjects = new ArrayList<>();
        for (MrObject object: MrObjects) {
            if (object.getPrivacyLabel() == "PUBLIC") publicObjects.add(object);
        }

        networkFragment.shareObjects(writeXml(publicObjects));
    }

    private String writeXml(List<MrObjectManager.MrObject> mrObjects){
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.startTag("", "MR-objects");
            serializer.attribute("", "number", String.valueOf(mrObjects.size()));
            for (MrObjectManager.MrObject object: mrObjects){
                serializer.startTag("", "object");
                serializer.startTag("", "name");
                serializer.text(object.getName());
                serializer.endTag("", "name");
                serializer.endTag("", "object");
            }
            serializer.endTag("", "MR-objects");
            serializer.endDocument();
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void processDetection(App app, Classifier.Recognition object) {

        // check user preferences of what is the supposed sensitivity of this object
        // if app is not allowed to see this object type, return
        //if (!userPermitted(app.getName(),object.getTitle())) return;

        // check object if in 'live' list

        // if not, add to live list
        String[] permissions = {app.getName()};
        String privacyLabel = getPrivacylabel(object.getTitle());
        addMrObject(new MrObject(object.getTitle(),permissions,privacyLabel));
    }

    public void processDetection(App app, CvDetector.Recognition object) {

        // check user preferences of what is the supposed sensitivity of this object
        // if app is not allowed to see this object type, return
        //if (!userPermitted(app.getName(),object.getTitle())) return;

        // check object if in 'live' list

        // if not, add to live list
        String[] permissions = {app.getName()};
        String privacyLabel = getPrivacylabel(object.getTitle());
        addMrObject(new MrObject(object.getTitle(),permissions,privacyLabel));
    }

    public void storeList() {
        // when the app is closed, store the list before it is destroyed.
    }
}
