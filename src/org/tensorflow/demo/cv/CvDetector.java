package org.tensorflow.demo.cv;

/**
 * Created by deg032 on 1/2/18.
 */

import android.graphics.Bitmap;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Pair;

public interface CvDetector{

    Pair<Path, RectF> imageDetector(Bitmap bitmap);

}
