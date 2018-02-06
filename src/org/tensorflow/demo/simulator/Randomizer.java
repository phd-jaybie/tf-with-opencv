package org.tensorflow.demo.simulator;

import android.graphics.Bitmap;

import org.tensorflow.demo.Classifier;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by deg032 on 2/2/18.
 */

public interface Randomizer {

    List<AppRandomizer.App> appGenerator(int numberOfApps);

}
