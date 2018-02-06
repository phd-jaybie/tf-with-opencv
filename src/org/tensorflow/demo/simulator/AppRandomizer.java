package org.tensorflow.demo.simulator;

import android.graphics.Bitmap;

import org.tensorflow.demo.Classifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by deg032 on 5/2/18.
 */

public class AppRandomizer implements Randomizer {

    static final String[] methods = new String[]
            {"TF_DETECTOR","CV_DETECTOR","TF_CLASSIFIER"};

    static final String[][] objects = new String[][]
            {
                    {"tv", "laptop", "mouse", "remote","keyboard", "scissors","cell phone",
                            "book"}, //office objects
                    {"person", "bed", "toilet", "laptop", "mouse","keyboard",
                            "cell phone"}, //high sensitivity objects
                    {"bus", "train", "truck", "boat", "traffic light", "fire hydrant", "stop sign",
                            "parking meter","bench"}, //outside objects
                    {"bird", "cat", "dog", "horse", "sheep","cow","elephant","bear","zebra",
                            "giraffe"}, //animal objects
                    {"frisbee","skis", "snowboard","sports ball","kite","baseball bat",
                            "baseball glove","skateboard","surfboard",
                            "tennis racket"}, //sporty objects
                    {"potted plant", "microwave", "oven","toaster","sink","refrigerator","vase",
                            "hair drier", "tv", "remote"}, //house objects
                    {"bottle","wine glass","cup","fork","knife","spoon","bowl","banana","apple",
                            "sandwich","orange","broccoli","carrot","hot dog","pizza","donut",
                            "cake", "chair","couch", "dining table"}, //kitchen or food objects
            };

    public class App{
        private final int id;
        private final String name;
        private String method;
        private final String[] objectsOfInterest;

        public App(int id, String name, String method, String[] objects) {
            this.id = id;
            this.name = name;
            this.method = method;
            this.objectsOfInterest = objects;
        }

        public int getId() {
            return id;
        }

        public String getMethod() {
            return method;
        }

        public String getName() {
            return name;
        }

        public String[] getObjectsOfInterest() {
            return objectsOfInterest;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public List<Classifier.Recognition> tfDetector(Bitmap bitmap, Classifier detector){
            List<Classifier.Recognition> results = detector.recognizeImage(bitmap);
            return results;
        }
    }

    public static Randomizer create(){
        final AppRandomizer appRandomizer = new AppRandomizer();
        return appRandomizer;
    }

    public List<App> appGenerator(int numberOfApps){
        Random rnd = new Random();
        final List<App> appList = new ArrayList<>(numberOfApps);

        for (int i = 0; i < numberOfApps ; i++){
            String method = methods[rnd.nextInt(methods.length)];
            String[] objectsOfInterest = objects[rnd.nextInt(objects.length)];
            String name = method + "_" + Integer.toString(i);
            App app = new App(i,name,method,objectsOfInterest);
            appList.add(app);
        }
        return appList;
    }

}
