package org.tensorflow.demo;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.opencv.android.OpenCVLoader;
import org.tensorflow.demo.env.Logger;
import org.tensorflow.demo.phd.MrDetectorActivity;
import org.tensorflow.demo.phd.ProtectedMrDetectorActivity;
import org.tensorflow.demo.simulator.App;
import org.tensorflow.demo.simulator.AppRandomizer;
import org.tensorflow.demo.simulator.Randomizer;
import org.tensorflow.demo.simulator.SingletonAppList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by deg032 on 9/2/18.
 */

public class MainActivity extends Activity {
    private static final Logger LOGGER = new Logger();

    private Handler handler;
    private HandlerThread handlerThread;

    private Randomizer randomizer;

    private int numberOfApps;
    public List<App> appList = new ArrayList<>();
    public String appListText;

    private SingletonAppList singletonAppList;
    private TextView textView;
    private EditText numberText;

    public final String firstMessage = "Generate App list first";

    static {
        if(!OpenCVLoader.initDebug()){
            LOGGER.d("OpenCV not loaded");
        } else {
            LOGGER.d("OpenCV loaded");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        LOGGER.d("onCreate " + this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();
    }

    private void initialize() {
        textView = (TextView) findViewById(R.id.generate_textView);
        numberText = (EditText) findViewById(R.id.number_of_apps);
        singletonAppList = SingletonAppList.getInstance();
    }

    public void generateAppList(View view){

        String sNumberOfApps = numberText.getText().toString();
        numberOfApps = Integer.valueOf(sNumberOfApps);

        String message = "Creating a new " + numberOfApps + "-app list.";
        LOGGER.i(message);
        textView.setText(message);

        runInBackground(new Runnable() {
            @Override
            public void run() {
                randomizer = AppRandomizer.create();
                appList = randomizer.appGenerator(getApplicationContext(), numberOfApps);

                String appLogMessage = "App list:\n";
                for (App app: appList) {
                    appLogMessage = appLogMessage + app.getName() + "\n";
                }
                LOGGER.i(appLogMessage);
                appListText = appLogMessage;

                textView.setText(appLogMessage);
                singletonAppList.setList(appList);
                singletonAppList.setListText(appListText);
            }
        });

    }

    public void mrDetectionIntent(View view){

        if (singletonAppList.getList().isEmpty()) {
            textView.setText(firstMessage);
            return;
        }

        Intent detectorIntent = new Intent(this, MrDetectorActivity.class);
        startActivity(detectorIntent);

    }

    public void mrDetectionIntentProtected(View view){

        if (singletonAppList.getList().isEmpty()) {
            textView.setText(firstMessage);
            return;
        }

        Intent protectedDetectorIntent = new Intent(this, ProtectedMrDetectorActivity.class);
        startActivity(protectedDetectorIntent);

    }

    private void startBackgroundThread() {
        handlerThread = new HandlerThread("main activity");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    private void stopBackgroundThread() {
        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void onStart() {
        LOGGER.d("onStart " + this);
        super.onStart();

        initialize();
        startBackgroundThread();
    }

    @Override
    public synchronized void onResume() {
        LOGGER.d("onResume " + this);
        super.onResume();

        initialize();
        startBackgroundThread();
    }

    @Override
    public synchronized void onPause() {
        LOGGER.d("onPause " + this);

        if (!isFinishing()) {
            LOGGER.d("Requesting finish");
            finish();
        }

        stopBackgroundThread();

        super.onPause();
    }

    protected synchronized void runInBackground(final Runnable r) {
        if (handler != null) {
            handler.post(r);
        }
    }

}
