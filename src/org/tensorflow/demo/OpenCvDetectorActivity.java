package org.tensorflow.demo;

/**
 * Created by deg032 on 25/1/18.
 */

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.Image;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.Display;
import android.view.Surface;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.opencv.android.OpenCVLoader;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Scalar;
import org.opencv.features2d.FlannBasedMatcher;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.xfeatures2d.SIFT;
import org.tensorflow.demo.OverlayView.DrawCallback;
import org.tensorflow.demo.env.BorderedText;
import org.tensorflow.demo.env.ImageUtils;
import org.tensorflow.demo.env.Logger;
import org.tensorflow.demo.tracking.MultiBoxTracker;
import org.tensorflow.demo.R; // Explicit import needed for internal Google builds.

/**
 * An activity that follows Tensorflow's demo DetectorActivity class as template and implements
 * classical visual detection using OpenCV.
 */
public class OpenCvDetectorActivity extends CameraActivity implements OnImageAvailableListener {
    private static final Logger LOGGER = new Logger();

    // Configuration values for the prepackaged multibox model.
    private static final int MB_INPUT_SIZE = 224;
    private static final int MB_IMAGE_MEAN = 128;
    private static final float MB_IMAGE_STD = 128;
    private static final String MB_INPUT_NAME = "ResizeBilinear";
    private static final String MB_OUTPUT_LOCATIONS_NAME = "output_locations/Reshape";
    private static final String MB_OUTPUT_SCORES_NAME = "output_scores/Reshape";
    private static final String MB_MODEL_FILE = "file:///android_asset/multibox_model.pb";
    private static final String MB_LOCATION_FILE =
            "file:///android_asset/multibox_location_priors.txt";

    private static final int TF_OD_API_INPUT_SIZE = 300;
    private static final String TF_OD_API_MODEL_FILE =
            "file:///android_asset/ssd_mobilenet_v1_android_export.pb";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/coco_labels_list.txt";

    // Configuration values for tiny-yolo-voc. Note that the graph is not included with TensorFlow and
    // must be manually placed in the assets/ directory by the user.
    // Graphs and models downloaded from http://pjreddie.com/darknet/yolo/ may be converted e.g. via
    // DarkFlow (https://github.com/thtrieu/darkflow). Sample command:
    // ./flow --model cfg/tiny-yolo-voc.cfg --load bin/tiny-yolo-voc.weights --savepb --verbalise
    private static final String YOLO_MODEL_FILE = "file:///android_asset/graph-tiny-yolo-voc.pb";
    private static final int YOLO_INPUT_SIZE = 416;
    private static final String YOLO_INPUT_NAME = "input";
    private static final String YOLO_OUTPUT_NAMES = "output";
    private static final int YOLO_BLOCK_SIZE = 32;

    // Which detection model to use: by default uses Tensorflow Object Detection API frozen
    // checkpoints.  Optionally use legacy Multibox (trained using an older version of the API)
    // or YOLO.
    private enum DetectorMode {
        TF_OD_API, MULTIBOX, YOLO;
    }
    private static final DetectorMode MODE = DetectorMode.TF_OD_API;

    // Minimum detection confidence to track a detection.
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.6f;
    private static final float MINIMUM_CONFIDENCE_MULTIBOX = 0.1f;
    private static final float MINIMUM_CONFIDENCE_YOLO = 0.25f;

    private static final boolean MAINTAIN_ASPECT = MODE == DetectorMode.YOLO;

    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);

    private static final boolean SAVE_PREVIEW_BITMAP = false;
    private static final float TEXT_SIZE_DIP = 10;

    private Integer sensorOrientation;

    private Classifier detector;

    private long lastProcessingTimeMs;
    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;
    private Bitmap cropCopyBitmap = null;

    private boolean computingDetection = false;

    private long timestamp = 0;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;

    private MultiBoxTracker tracker;

    private byte[] luminanceCopy;

    private BorderedText borderedText;
    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        final float textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        tracker = new MultiBoxTracker(this);

        int cropSize = TF_OD_API_INPUT_SIZE;
        if (MODE == DetectorMode.YOLO) {
            detector =
                    TensorFlowYoloDetector.create(
                            getAssets(),
                            YOLO_MODEL_FILE,
                            YOLO_INPUT_SIZE,
                            YOLO_INPUT_NAME,
                            YOLO_OUTPUT_NAMES,
                            YOLO_BLOCK_SIZE);
            cropSize = YOLO_INPUT_SIZE;
        } else if (MODE == DetectorMode.MULTIBOX) {
            detector =
                    TensorFlowMultiBoxDetector.create(
                            getAssets(),
                            MB_MODEL_FILE,
                            MB_LOCATION_FILE,
                            MB_IMAGE_MEAN,
                            MB_IMAGE_STD,
                            MB_INPUT_NAME,
                            MB_OUTPUT_LOCATIONS_NAME,
                            MB_OUTPUT_SCORES_NAME);
            cropSize = MB_INPUT_SIZE;
        } else {
            try {
                detector = TensorFlowObjectDetectionAPIModel.create(
                        getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
                cropSize = TF_OD_API_INPUT_SIZE;
            } catch (final IOException e) {
                LOGGER.e("Exception initializing classifier!", e);
                Toast toast =
                        Toast.makeText(
                                getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
                toast.show();
                finish();
            }
        }

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        sensorOrientation = rotation - getScreenOrientation();
        LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Config.ARGB_8888);

        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        cropSize, cropSize,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(
                new DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                        tracker.draw(canvas);
                        if (isDebug()) {
                            tracker.drawDebug(canvas);
                        }
                    }
                });

        addCallback(
                new DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                        if (!isDebug()) {
                            return;
                        }
                        final Bitmap copy = cropCopyBitmap;
                        if (copy == null) {
                            return;
                        }

                        final int backgroundColor = Color.argb(100, 0, 0, 0);
                        canvas.drawColor(backgroundColor);

                        final Matrix matrix = new Matrix();
                        final float scaleFactor = 2;
                        matrix.postScale(scaleFactor, scaleFactor);
                        matrix.postTranslate(
                                canvas.getWidth() - copy.getWidth() * scaleFactor,
                                canvas.getHeight() - copy.getHeight() * scaleFactor);
                        canvas.drawBitmap(copy, matrix, new Paint());

                        final Vector<String> lines = new Vector<String>();
                        if (detector != null) {
                            final String statString = detector.getStatString();
                            final String[] statLines = statString.split("\n");
                            for (final String line : statLines) {
                                lines.add(line);
                            }
                        }
                        lines.add("");

                        lines.add("Frame: " + previewWidth + "x" + previewHeight);
                        lines.add("Crop: " + copy.getWidth() + "x" + copy.getHeight());
                        lines.add("View: " + canvas.getWidth() + "x" + canvas.getHeight());
                        lines.add("Rotation: " + sensorOrientation);
                        lines.add("Inference time: " + lastProcessingTimeMs + "ms");

                        borderedText.drawLines(canvas, 10, canvas.getHeight() - 10, lines);
                    }
                });
    }

    /**
     * Processes the JPEG {@link Image} using OpenCV.
     */
    private ArrayList<org.opencv.core.Point> ImageMatcher(MatOfKeyPoint keyPoints, Mat descriptors){
        final String TAG = "LocalImageMatcher";

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

            Mat obj_corners = new Mat(4,1,CvType.CV_32FC2);
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
                Log.d(TAG, "Time to Match: " + Long.toString((time1 - time))
                        + ", Number of matches: " + good_matches.size()
                        + " (" + Integer.toString(MIN_MATCH_COUNT) + ")"
                        + ", Time to transform: " + Long.toString((System.currentTimeMillis() - time1)));
            } else {
                Log.d(TAG, "Time to Match: " + Long.toString((time1 - time))
                        + ", Object probably not in view even with " + good_matches.size()
                        + " (" + Integer.toString(MIN_MATCH_COUNT) + ") matches.");
            }
            //result = "Enough matches.";
        } else {
            Log.d(TAG, "Time to Match: " + Long.toString((System.currentTimeMillis() - time))
                    + ", Not Enough Matches (" + good_matches.size()
                    + "/" + Integer.toString(MIN_MATCH_COUNT) + ")");
            //result = "Not enough matches.";
        }

        return points; //mScenePoints; for using drawContours
    }

    OverlayView trackingOverlay;

    @Override
    protected void processImage() {
        ++timestamp;
        final long currTimestamp = timestamp;
        //byte[] originalLuminance = getLuminance();
        final byte[] mBytes = getImageMat();

        // Usually, this onFrame method below doesn't really happen as you would see in the toast
        // message that appears when you start up this detector app.
        /*tracker.onFrame(
                previewWidth,
                previewHeight,
                getLuminanceStride(),
                sensorOrientation,
                originalLuminance,
                timestamp);*/
        trackingOverlay.postInvalidate();

        // No mutex needed as this method is not reentrant.
        if (computingDetection) {
            readyForNextImage();
            return;
        }
        computingDetection = true;
        LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");

        final int mWidth = matCvWidth;
        final int mHeight = matCvHeight;

        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

        /*if (luminanceCopy == null) {
            luminanceCopy = new byte[originalLuminance.length];
        }
        System.arraycopy(originalLuminance, 0, luminanceCopy, 0, originalLuminance.length);*/
        readyForNextImage();

        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(croppedBitmap);
        }

        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        LOGGER.i("Running detection on image " + currTimestamp);
                        final long startTime = SystemClock.uptimeMillis();
                        //final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);

                        /**
                         * Below are the snippet of code that is used to process the image.
                         * Until --> *!*
                         */

                        ArrayList<org.opencv.core.Point> scenePoints = new ArrayList<>();

                        Mat buf = new Mat(mWidth, mHeight, CvType.CV_8UC1);
                        buf.put(0,0, mBytes);
                        Mat mat = Imgcodecs.imdecode(buf, Imgcodecs.IMREAD_COLOR);

                        SIFT mFeatureDetector = SIFT.create();
                        MatOfKeyPoint mKeyPoints = new MatOfKeyPoint();
                        Mat mDescriptors = new Mat();

                        /** Resizing image *
                         Mat nMat = new Mat();
                         org.opencv.core.Size sz = new org.opencv.core.Size(mat.width()/nResolutionDivider,mat.height()/nResolutionDivider);
                         Imgproc.resize( mat, nMat, sz );*/

                        long time2 = System.currentTimeMillis();

                        try {
                            mFeatureDetector.detect(mat, mKeyPoints);
                            mFeatureDetector.compute(mat, mKeyPoints, mDescriptors);
                            LOGGER.d("Height: " + Integer.toString(mHeight)
                                    + ", Width: " + Integer.toString(mWidth)
                                    + " Time to Extract locally: " + Long.toString((System.currentTimeMillis() - time2))
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
                            // ArrayList<Point> to List<MatOfPoint>
                        } else {
                            LOGGER.d("Cannot process: No key points");
                        }

                        // save output image
                        /*File cvFile = new File(getActivity().getExternalFilesDir(null), "cv_local_process.jpg");
                        String filename = cvFile.getAbsolutePath();
                        Imgcodecs.imwrite(filename, mat);*/

                        /**
                         * Ends here <-- *!*
                         */

                        lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

                        cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
                        final Canvas canvas = new Canvas(cropCopyBitmap);
                        final Paint paint = new Paint();
                        paint.setColor(Color.RED);
                        paint.setStyle(Style.STROKE);
                        paint.setStrokeWidth(2.0f);

                        final Path path = new Path();
                        path.moveTo((float) scenePoints.get(0).x, (float) scenePoints.get(0).y);
                        path.lineTo((float) scenePoints.get(1).x, (float) scenePoints.get(1).y);
                        path.lineTo((float) scenePoints.get(2).x, (float) scenePoints.get(2).y);
                        path.lineTo((float) scenePoints.get(3).x, (float) scenePoints.get(3).y);
                        path.close();

                        /*float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                        switch (MODE) {
                            case TF_OD_API:
                                minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                                break;
                            case MULTIBOX:
                                minimumConfidence = MINIMUM_CONFIDENCE_MULTIBOX;
                                break;
                            case YOLO:
                                minimumConfidence = MINIMUM_CONFIDENCE_YOLO;
                                break;
                        }

                        final List<Classifier.Recognition> mappedRecognitions =
                                new LinkedList<Classifier.Recognition>();

                        for (final Classifier.Recognition result : results) {
                            final RectF location = result.getLocation();
                            if (location != null && result.getConfidence() >= minimumConfidence) {
                                canvas.drawRect(location, paint);

                                // Just checking the values of the RectF.
                                //LOGGER.i("Bounding box dimensions are left: " + String.valueOf(location.left) +
                                //        " and bottom: " + String.valueOf(location.bottom));

                                cropToFrameTransform.mapRect(location);
                                result.setLocation(location);
                                mappedRecognitions.add(result);
                            }
                        }*/

                        canvas.drawPath(path, paint);

                        /*tracker.trackResults(mappedRecognitions, luminanceCopy, currTimestamp);*/
                        trackingOverlay.postInvalidate();

                        requestRender();
                        computingDetection = false;
                    }
                });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.camera_connection_fragment_tracking;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    @Override
    public void onSetDebug(final boolean debug) {
        detector.enableStatLogging(debug);
    }
}
