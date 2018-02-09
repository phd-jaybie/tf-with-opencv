package org.tensorflow.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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

    public List<App> appList = new ArrayList<>();
    public String appListText;

    private SingletonAppList singletonAppList;

    private TextView textView;
    private EditText numberText;

    public final String firstMessage = "Generate App list first";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.generate_textView);
        numberText = (EditText) findViewById(R.id.number_of_apps);
        singletonAppList = SingletonAppList.getInstance();

    }

    public void generateAppList(){

        final int numberOfApps = Integer.getInteger(numberText.getText().toString());

        String message = "Creating a new " + numberOfApps + "-app list.";
        LOGGER.i(message);
        textView.setText(message);

        runInBackground(new Runnable() {
            @Override
            public void run() {
                randomizer = AppRandomizer.create();
                appList = randomizer.appGenerator(getApplicationContext(), numberOfApps);

                String appLogMessage = "App list: ";
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

        if (appList == null) {
            textView.setText(firstMessage);
            return;
        }

        Intent detectorIntent = new Intent(this, MrDetectorActivity.class);
        startActivity(detectorIntent);

    }

    public void mrDetectionIntentProtected(View view){

        if (appList == null) {
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

        startBackgroundThread();
    }

    @Override
    public synchronized void onResume() {
        LOGGER.d("onResume " + this);
        super.onResume();

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

    @Override
    public synchronized void onStop() {
        LOGGER.d("onStop " + this);
        super.onStop();
    }

    @Override
    public synchronized void onDestroy() {
        LOGGER.d("onDestroy " + this);
        super.onDestroy();
    }

    protected synchronized void runInBackground(final Runnable r) {
        if (handler != null) {
            handler.post(r);
        }
    }

}
