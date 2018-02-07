package org.tensorflow.demo.simulator;

import android.content.Context;
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

    List<App> appGenerator(Context context, int numberOfApps);

    //List<User> userGenerator(int numberOfUsers);

}
