package org.tensorflow.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import org.opencv.android.OpenCVLoader;
import org.tensorflow.demo.env.Logger;
import org.tensorflow.demo.phd.MrDetectorActivity;
import org.tensorflow.demo.phd.ProtectedMrDetectorActivity;
import org.tensorflow.demo.phd.ProtectedMrDetectorActivityWithNetwork;
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
    private boolean hasList = false;

    private SingletonAppList singletonAppList;
    private TextView textView;
    private EditText numberText;
    private EditText urlString;
    private Switch debugSwitch; // This switch just tells the processing activities if captures are limited or not.
    private Switch networkSwitch; // This switch just tells whether the detection is local or remote.

    private String NetworkMode;
    private String remoteUrl = null;

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

        initialize(); //Initializes the views

        if (isNetworkConnected()) {
            ProgressDialog mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Please wait...");
            mProgressDialog.setCancelable(true);
            mProgressDialog.show();
        } else {
            noConnection();
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE); // 1
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo(); // 2
        return networkInfo != null && networkInfo.isConnected(); // 3
    }

    private void noConnection(){
        new AlertDialog.Builder(this)
                .setTitle("No Internet Connection")
                .setMessage("It looks like your internet connection is off. Please turn it " +
                        "on and try again")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });//.setIcon(android.R.drawable.ic_dialog_alert).show();
    }

    private void initialize() {
        textView = (TextView) findViewById(R.id.generate_textView);
        numberText = (EditText) findViewById(R.id.number_of_apps);
        urlString = (EditText) findViewById(R.id.remote_url);
        debugSwitch = (Switch) findViewById(R.id.debug_toggle);
        networkSwitch = (Switch) findViewById(R.id.network_toggle);
        singletonAppList = SingletonAppList.getInstance();

        // Checking for available generated app list.
        hasList = checkList();
    }

    public void generateAppList(View view){

        String sNumberOfApps = numberText.getText().toString();
        numberOfApps = Integer.valueOf(sNumberOfApps);

        runInBackground(new Runnable() {
            @Override
            public void run() {

                String message;

                if (debugSwitch.isChecked()) message = "Will only take 10 captures.\n";
                else message = "Will capture continuously.\n";

                message = message + "Creating a new " + numberOfApps + "-app list.\n";
                LOGGER.i(message);

                writeToTextView(message);

                randomizer = AppRandomizer.create();
                appList = randomizer.appGenerator(getApplicationContext(), numberOfApps);

                String appLogMessage = "App list:\n";
                for (App app: appList) {
                    appLogMessage = appLogMessage + app.getName() + "\n";
                }
                LOGGER.i(appLogMessage);
                appListText = appLogMessage;

                writeToTextView(message + appLogMessage);
                singletonAppList.setList(appList);
                singletonAppList.setListText(appListText);
                singletonAppList.setFastDebug(debugSwitch.isChecked());
            }
        });

    }

    private boolean checkList(){

        if (singletonAppList.getList().isEmpty()) {
            writeToTextView(firstMessage);
            return false;
        } else if (networkSwitch.isChecked() && (remoteUrl == null)) {// || !URLUtil.isValidUrl(remoteUrl)) ) {
            writeToTextView("No or Invalid URL for remote.");
            return false;
        } else {
            writeToTextView(singletonAppList.getListText());
            return true;
        }

    }

    private void writeToTextView(final String message){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (textView == null) textView = (TextView) findViewById(R.id.generate_textView);
                textView.setText(message);
            }
        });
    }

    public void onFastDebug(View view){
        if (debugSwitch.isChecked()) debugSwitch.setTextColor(Color.BLACK);
        else  debugSwitch.setTextColor(Color.LTGRAY);

    }

    public void onNetworkProcess(View view){

        remoteUrl = urlString.getText().toString();

        if (networkSwitch.isChecked()) {
            LOGGER.i("Remote image processing.");
            networkSwitch.setTextColor(Color.BLACK);
            NetworkMode = "REMOTE_PROCESS";
            singletonAppList.setRemoteUrl(remoteUrl);
        } else {
            LOGGER.i("Local image processing.");
            networkSwitch.setTextColor(Color.LTGRAY);
            NetworkMode = "LOCAL";
        }
    }

    public void mrDetectionIntent(View view){

        if (!checkList()) return;

        Intent detectorIntent = new Intent(this, MrDetectorActivity.class);
        startActivity(detectorIntent);

    }

    public void mrDetectionIntentProtected(View view){

        if (!checkList()) return;

        Intent detectorIntent = new Intent(this, ProtectedMrDetectorActivity.class);
        startActivity(detectorIntent);

    }

    public void mrDetectionIntentWithSharing(View view){

        if (!checkList()) return;

        Intent detectorIntent = new Intent(this, ProtectedMrDetectorActivityWithNetwork.class);
        detectorIntent.putExtra("NetworkMode",NetworkMode);
        startActivity(detectorIntent);

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
