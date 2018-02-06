package org.tensorflow.demo.phd;

/**
 * Created by deg032 on 6/2/18.
 *
 * Note: To maintain the privacy of object/s and user/s, please have these objects only created by
 * by the system-level functions and not by external or third-party applications and/or services.
 */

public class MrObjectManager {

    class MrObject {

        private int id;
        private String name;
        private String description;
        private String permissions;
        private String privacyLabel;

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public String getPermissions() {
            return permissions;
        }

        public String getPrivacyLabel() {
            return privacyLabel;
        }

        public MrObject(int id, String name, String description, String permissions, String privacyLabel) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.permissions = permissions;
            this.privacyLabel = privacyLabel;
        }

        public MrObject(MrObject object) {
            this.id = object.getId();
            this.name = object.getName();
            this.description = object.getDescription();
            this.permissions = object.getPermissions();
            this.privacyLabel = object.getPrivacyLabel();
        }

    }


}
