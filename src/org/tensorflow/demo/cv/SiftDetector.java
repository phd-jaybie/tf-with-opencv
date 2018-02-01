package org.tensorflow.demo.cv;

/**
 * Created by deg032 on 1/2/18.
 */

import android.graphics.Bitmap;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Pair;

import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.features2d.FlannBasedMatcher;
import org.opencv.imgproc.Imgproc;
import org.opencv.xfeatures2d.SIFT;
import org.tensorflow.demo.env.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.tensorflow.demo.CameraActivity.MIN_MATCH_COUNT;
import static org.tensorflow.demo.CameraActivity.mRefDescriptors;
import static org.tensorflow.demo.CameraActivity.mRefKeyPoints;
import static org.tensorflow.demo.CameraActivity.objImageMat;

public class SiftDetector implements CvDetector{
    private static final Logger LOGGER = new Logger();

    public static CvDetector create() {
        final SiftDetector detector = new SiftDetector();
        return detector;
    }

    @Override
    public Pair<Path, RectF> imageDetector(Bitmap bitmap){

        ArrayList<org.opencv.core.Point> scenePoints = new ArrayList<>();
        final SIFT mFeatureDetector = SIFT.create();
        final MatOfKeyPoint mKeyPoints = new MatOfKeyPoint();
        final Mat mDescriptors = new Mat();

        long startTime = System.currentTimeMillis();

        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap,mat);

        LOGGER.d("Matrix has width: " + Integer.toString(mat.width())
                + " and height: " + Integer.toString(mat.height()));

        try {
            mFeatureDetector.detect(mat, mKeyPoints);
            mFeatureDetector.compute(mat, mKeyPoints, mDescriptors);
            LOGGER.d("Time to Extract locally: " + Long.toString((System.currentTimeMillis() - startTime))
                    + ", Number of Key points: " + mKeyPoints.toArray().length);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.d("Cannot process.");
        } /**finally
         } */

        if (0 != mKeyPoints.toArray().length) {
            scenePoints = ImageMatcher(mKeyPoints, mDescriptors);
            //Imgproc.drawContours(mat, scenePoints, 0, new Scalar(255, 0, 0), 3);
            // for using the draw contours again, please change scenePoints from
            // ArrayList<Point> to List<MatOfPoint>, and change the ImageMatcher
            // return value as well to List<MatOfPoint> type.
        } else {
            LOGGER.d("Cannot process: No key points");
        }

        /**
         * Using path to draw a transformed bounding box.
         */
        final Path path = new Path();
        if (!scenePoints.isEmpty()) {
            path.moveTo((float) scenePoints.get(0).x, (float) scenePoints.get(0).y);
            path.lineTo((float) scenePoints.get(1).x, (float) scenePoints.get(1).y);
            path.lineTo((float) scenePoints.get(2).x, (float) scenePoints.get(2).y);
            path.lineTo((float) scenePoints.get(3).x, (float) scenePoints.get(3).y);
            path.close();
        }

        /**
         * Using RectF to draw a fixed rectangle bounding box.
         */
        final RectF location = new RectF();
        if (!scenePoints.isEmpty()) {
            float[] xValues = {(float) scenePoints.get(0).x,
                    (float) scenePoints.get(1).x,
                    (float) scenePoints.get(2).x,
                    (float) scenePoints.get(3).x};
            float[] yValues = {(float) scenePoints.get(0).y,
                    (float) scenePoints.get(1).y,
                    (float) scenePoints.get(2).y,
                    (float) scenePoints.get(3).y};
            Arrays.sort(xValues);
            Arrays.sort(yValues);
            location.set(xValues[0], yValues[0], xValues[3], yValues[3]);
        }

        Pair<Path, RectF> result = new Pair(path, location);

        return result;
    }


    private ArrayList<Point> ImageMatcher(MatOfKeyPoint keyPoints, Mat descriptors){

        ArrayList<org.opencv.core.Point> points = new ArrayList<>();
        List<MatOfPoint> mScenePoints = new ArrayList<>();
        List<MatOfDMatch> matches = new ArrayList<>();
        FlannBasedMatcher descriptorMatcher = FlannBasedMatcher.create();
        descriptorMatcher.knnMatch(mRefDescriptors, descriptors, matches, 2);

        long time = System.currentTimeMillis();

        // ratio test
        LinkedList<DMatch> good_matches = new LinkedList<>();
        for (Iterator<MatOfDMatch> iterator = matches.iterator(); iterator.hasNext();) {
            MatOfDMatch matOfDMatch = iterator.next();
            if (matOfDMatch.toArray()[0].distance / matOfDMatch.toArray()[1].distance < 0.75) {
                good_matches.add(matOfDMatch.toArray()[0]);
            }
        }

        long time1 = System.currentTimeMillis();

        if (good_matches.size()>MIN_MATCH_COUNT){
            /** get keypoint coordinates of good matches to find homography and remove outliers using ransac */
            List<org.opencv.core.Point> refPoints = new ArrayList<>();
            List<org.opencv.core.Point> mPoints = new ArrayList<>();
            for(int i = 0; i<good_matches.size(); i++){
                refPoints.add(mRefKeyPoints.toList().get(good_matches.get(i).queryIdx).pt);
                mPoints.add(keyPoints.toList().get(good_matches.get(i).trainIdx).pt);
            }
            // convertion of data types - there is maybe a more beautiful way
            Mat outputMask = new Mat();
            MatOfPoint2f rPtsMat = new MatOfPoint2f();
            rPtsMat.fromList(refPoints);
            MatOfPoint2f mPtsMat = new MatOfPoint2f();
            mPtsMat.fromList(mPoints);

            Mat obj_corners = new Mat(4,1, CvType.CV_32FC2);
            Mat scene_corners = new Mat(4,1,CvType.CV_32FC2);

            obj_corners.put(0, 0, new double[] {0,0});
            obj_corners.put(1, 0, new double[] {objImageMat.width()-1,0});
            obj_corners.put(2, 0, new double[] {objImageMat.width()-1,objImageMat.height()-1});
            obj_corners.put(3, 0, new double[] {0,objImageMat.height()-1});

            // Find homography - here just used to perform match filtering with RANSAC, but could be used to e.g. stitch images
            // the smaller the allowed reprojection error (here 15), the more matches are filtered
            Mat Homog = Calib3d.findHomography(rPtsMat, mPtsMat, Calib3d.RANSAC, 15, outputMask, 2000, 0.995);
            Core.perspectiveTransform(obj_corners,scene_corners,Homog);

            MatOfPoint sceneCorners = new MatOfPoint();
            for (int i=0; i < scene_corners.rows(); i++) {
                org.opencv.core.Point point = new org.opencv.core.Point();
                point.set(scene_corners.get(i,0));
                points.add(point);
            }
            sceneCorners.fromList(points);
            mScenePoints.add(sceneCorners);

            if (Imgproc.contourArea(mScenePoints.get(0)) > (MIN_MATCH_COUNT*MIN_MATCH_COUNT)) {
                LOGGER.i("Time to Match: " + Long.toString((time1 - time))
                        + ", Number of matches: " + good_matches.size()
                        + " (" + Integer.toString(MIN_MATCH_COUNT) + ")"
                        + ", Time to transform: " + Long.toString((System.currentTimeMillis() - time1)));
            } else {
                LOGGER.i( "Time to Match: " + Long.toString((time1 - time))
                        + ", Object probably not in view even with " + good_matches.size()
                        + " (" + Integer.toString(MIN_MATCH_COUNT) + ") matches.");
            }
            //result = "Enough matches.";
        } else {
            LOGGER.i( "Time to Match: " + Long.toString((System.currentTimeMillis() - time))
                    + ", Not Enough Matches (" + good_matches.size()
                    + "/" + Integer.toString(MIN_MATCH_COUNT) + ")");
            //result = "Not enough matches.";
        }

        return points; //mScenePoints; for using drawContours
    }

}
