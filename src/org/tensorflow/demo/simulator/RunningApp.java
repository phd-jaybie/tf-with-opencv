package org.tensorflow.demo.simulator;

import org.tensorflow.demo.Classifier;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by deg032 on 9/2/18.
 */

public class RunningApp {

    private App app;

    private final List<RunningApp.AppCallback> callbacks = new LinkedList<>();

    public RunningApp(App app) {
        this.app = app;
    }

    public interface AppCallback {
        public void appCallback();
    }

    public void addCallback(final AppCallback callback) {
        callbacks.add(callback);
    }

    public synchronized void processAllCallbacks() {
        for (final AppCallback callback : callbacks) {
            callback.appCallback();
        }
    }

    public void process(Classifier.Recognition recognition, Long timestamp){
        // This is a sample method that an app's appCallback calls.
        // Each app can define their own appCallback methods.
    }

}
