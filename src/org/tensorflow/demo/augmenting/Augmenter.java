package org.tensorflow.demo.augmenting;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.Pair;

import org.tensorflow.demo.Classifier;
import org.tensorflow.demo.env.Logger;
import org.tensorflow.demo.phd.MrObjectManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by deg032 on 1/2/18.
 */

public class Augmenter extends MrObjectManager{

    private final Logger logger = new Logger();

    //private List<Pair<MrObject, Long>> liveMrObjects = new ArrayList<>();

    public synchronized void drawAugmentations (Canvas canvas){
        /**
         * Insert here actual drawing of augmentations of live, detected and tracked bject/s.
         */

        for (final MrObject mrObject: MrObjects) {

        }
    }

    public synchronized void trackResults(final byte[] frame, final long timestamp) {
        /**
         * Insert code here that handles the augmentation of detected/tracked objects.
         */
    }
}
