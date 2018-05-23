package org.tensorflow.demo.threading;

import android.graphics.Bitmap;
import android.graphics.RectF;

import org.tensorflow.demo.Classifier;
import org.tensorflow.demo.env.Logger;
import org.tensorflow.demo.phd.detector.cv.CvDetector;
import org.tensorflow.demo.phd.detector.cv.OrbDetector;
import org.tensorflow.demo.phd.detector.cv.SiftDetector;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by deg032 on 22/5/18.
 */
public class ProcessTask implements
        FrameTrackingRunnable.TaskRunnableFrameTrackingMethods,
        ObjectDetectionRunnable.TaskRunnableObjectDetectionMethods {

    /*
     * Field containing the Thread this task is running on.
     */
    Thread mThreadThis;

    /*
     * Fields containing references to the two runnable objects that handle downloading and
     * decoding of the image.
     */
    private Runnable mObjectDetectionRunnable;
    private Runnable mFrameTrackingRunnable;

    // The Thread on which this task is currently running.
    private Thread mCurrentThread;

    // Delegates handling the current state of the task to the ProcessManager object
    void handleState(int state) {
        sProcessManager.handleState(this, state);
    }

    /*
     * An object that contains the ThreadPool singleton.
     */
    private static ProcessManager sProcessManager;

    // Input Bitmap frame to be processed
    private Bitmap mInputFrame;
    private List<Classifier.Recognition> mResults;
    private Classifier mDetector; //for TF detection
    private CvDetector cvDetector; //for OpenCV detection

    /**
     * Creates a PhotoTask containing a download object and a decoder object.
     */
    ProcessTask() {
        // Create the runnables
        mObjectDetectionRunnable = new ObjectDetectionRunnable(this);
        mFrameTrackingRunnable = new FrameTrackingRunnable(this);//, mInputFrame);
        sProcessManager = ProcessManager.getInstance();
    }

    /*
 * Returns the Thread that this Task is running on. The method must first get a lock on a
 * static field, in this case the ThreadPool singleton. The lock is needed because the
 * Thread object reference is stored in the Thread object itself, and that object can be
 * changed by processes outside of this app.
 */
    public Thread getCurrentThread() {
        synchronized(sProcessManager) {
            return mCurrentThread;
        }
    }

    /*
     * Sets the identifier for the current Thread. This must be a synchronized operation; see the
     * notes for getCurrentThread()
     */
    public void setCurrentThread(Thread thread) {
        synchronized(sProcessManager) {
            mCurrentThread = thread;
        }
    }

/*    *//**
     * Recycles an Process Task object before it's put back into the pool. One reason to do
     * this is to avoid memory leaks.
     *//*
    void recycle() {

        // Deletes the weak reference to the imageView
        if ( null != mImageWeakRef ) {
            mImageWeakRef.clear();
            mImageWeakRef = null;
        }

        // Releases references to the byte buffer and the BitMap
        mImageBuffer = null;
        mDecodedImage = null;
    }*/

    @Override
    public void setFrameTrackingThread(Thread currentThread) { setCurrentThread(currentThread);
    }

    @Override
    public void handleFrameTrackingState(int state) {
        int outState;

        // Converts the decode state to the overall state.
        switch(state) {
            case FrameTrackingRunnable.TRACKING_STATE_COMPLETED:
                outState = ProcessManager.TRACKING_COMPLETE;
                break;
            //case FrameTrackingRunnable.TRACKING_STATE_FAILED:
            //    outState = ProcessManager.TRA;
            //    break;
            default:
                outState = ProcessManager.TRACKING_STARTED;
                break;
        }

        // Passes the state to the ThreadPool object.
        handleState(outState);
    }

    @Override
    public Bitmap getInputBitmap() {
        return mInputFrame;
    }

    @Override
    public void setInputBitmap(Bitmap inputBitmap){
        mInputFrame = inputBitmap;
    }

    @Override
    public List<Classifier.Recognition> getResults(){
        return mResults;
    };

    @Override
    public void setResults(List<Classifier.Recognition> results){
        mResults = results;
    };

    @Override
    public void setObjectDetectionThread(Thread currentThread) {
        setCurrentThread(currentThread);
    }

    @Override
    public void handleObjectDetectionState(int state) {
        int outState;

        // Converts the decode state to the overall state.
        switch(state) {
            case ObjectDetectionRunnable.DETECTION_STATE_COMPLETED:
                outState = ProcessManager.DETECTION_COMPLETE;
                break;
            case ObjectDetectionRunnable.DETECTION_STATE_FAILED:
                outState = ProcessManager.DETECTION_FAILED;
                break;
            default:
                outState = ProcessManager.DETECTION_STARTED;
                break;
        }

        // Passes the state to the ThreadPool object.
        handleState(outState);
    };

    public void setTFDetector(Classifier detector){
        mDetector = detector;
    }

    @Override
    public Classifier getDetector(){
        return mDetector;
    }

    public void setCVDetector(CvDetector detector){
        cvDetector = detector;
    }

    // Returns the instance that downloaded the image
    Runnable getObjectDetectionRunnable() {
        return mObjectDetectionRunnable;
    }

    // Returns the instance that decode the image
    Runnable getFrameTrackingRunnable() {
        return mFrameTrackingRunnable;
    }

}

