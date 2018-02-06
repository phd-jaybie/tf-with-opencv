package org.tensorflow.demo.augmenting;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;

import org.tensorflow.demo.Classifier;
import org.tensorflow.demo.env.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by deg032 on 1/2/18.
 */

public class Augmenter {
    private final Logger logger = new Logger();

    private static class TrackedRecognition {
        //ObjectTracker.TrackedObject trackedObject;
        RectF location;
        float detectionConfidence;
        int color;
        String title;
    }

    private final List<TrackedRecognition> trackedObjects = new LinkedList<TrackedRecognition>();

    private Context context;

    public Augmenter (final Context context) {
        this.context = context;
    }

    public synchronized void drawAugmentations (Canvas canvas){
        /**
         * Insert here actual drawing of augmentations of tracked object/s.
         */
    }

    public synchronized void trackResults(
            final List<Classifier.Recognition> results, final byte[] frame, final long timestamp) {
        logger.i("Processing %d results from %d", results.size(), timestamp);
        /**
         * Insert code here that handles the augmentation of detected/tracked objects.
         */
    }
}
