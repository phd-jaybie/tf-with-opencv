package org.tensorflow.demo.network;

import android.content.res.AssetManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Xml;


import org.tensorflow.demo.env.Logger;
import org.tensorflow.demo.phd.MrObjectManager;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by deg032 on 22/2/18.
 */

public class NetworkFragment extends Fragment {

    public static final String TAG = "NetworkFragment";
    private static final Logger LOGGER = new Logger();


    private static final String URL_KEY = "UrlKey";

    private NetworkListener networkListener;
    private ShareTask mShareTask;
    private String mUrlString;

    private NetworkServer networkServer;
    private String mPayload;

    /**
     * Static initializer for NetworkFragment that sets the URL of the host it will be downloading
     * from.
     */
    public static NetworkFragment getInstance(FragmentManager fragmentManager, String url) {
        // Recover NetworkFragment in case we are re-creating the Activity due to a config change.
        // This is necessary because NetworkFragment might have a task that began running before
        // the config change and has not finished yet.
        // The NetworkFragment is recoverable via this method because it calls
        // setRetainInstance(true) upon creation.

        NetworkFragment networkFragment = (NetworkFragment) fragmentManager
                .findFragmentByTag(NetworkFragment.TAG);
        if (networkFragment == null) {
            networkFragment = new NetworkFragment();
            Bundle args = new Bundle();
            args.putString(URL_KEY, url);
            networkFragment.setArguments(args);
            fragmentManager.beginTransaction().add(networkFragment, TAG).commit();
        }

        return networkFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain this Fragment across configuration changes in the host Activity.
        setRetainInstance(true);
        mUrlString = getArguments().getString(URL_KEY);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (networkServer!= null) networkServer.start();
        // Host Activity will handle callbacks from task.
        networkListener = (NetworkListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Clear reference to host Activity.
        if (networkServer!= null) networkServer.stop();
        networkListener = null;
    }

    @Override
    public void onDestroy() {
        // Cancel task when Fragment is destroyed.
        cancelDownload();
        if (networkServer!= null) networkServer.stop();
        super.onDestroy();
    }

    /**
     * Start non-blocking execution of NetworkTask.
     */
    public void startNetwork(String mUrl) {
        cancelDownload();
        mShareTask = new ShareTask();
        mShareTask.execute(mUrl);
    }

    public void shareObjects(String payload){
        // Convert the list of public objects to an XML. Then send out.
        mPayload = payload;

        // Instead of using a single URL, a list of URLs can be iterated and used.
        // For the meantime, we use a single URL.
        startNetwork(mUrlString);
    }

    /**
     * Cancel (and interrupt if necessary) any ongoing DownloadTask execution.
     */
    public void cancelDownload() {
        if (mShareTask != null) {
            mShareTask.cancel(true);
            mShareTask = null;
        }
    }

    /**
     * For listening to a port, we create a simple server that will do that.
     */

    public void startServer(int mPort, AssetManager assets){
        networkServer = new NetworkServer(mPort, assets);
    }

    public void setServerListener(NetworkListener networkListener){
        if (networkServer != null) networkServer.setNetworkListener(networkListener);
    }

    public List<MrObjectManager.MrObject> getObjects(){
        // parse the XML file received and extract the objects.
        return null;
    }


    /**
     * Implementation of AsyncTask that runs a network operation on a background thread.
     */
    private class ShareTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                return uploadData(params[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                networkListener.uploadComplete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String uploadData(String urlString) throws IOException {
            final String payload = mPayload;

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            /*try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();
                is = conn.getInputStream();
                return convertToString(is);
            } finally {
                if (is != null) {
                    is.close();
                }
            }*/

            String[] result;

            try {
                InputStream inputStream = null;

                long time = System.currentTimeMillis();

                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setChunkedStreamingMode(0);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept-Encoding", "identity");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-type", "xml");
                ;
                conn.addRequestProperty("Content-length", payload.getBytes().length+ "");

                //conn.connect();

                OutputStream outputBuff = new BufferedOutputStream(conn.getOutputStream());

                LOGGER.d("Sharing objects to remote.");

                try {
                    //showToast("Uploaded to: " + mURL.toString());
                    outputBuff.write(payload.getBytes());
                    //Log.d(TAG, "Uploaded.");
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    outputBuff.close();
                }

                int responseCode = conn.getResponseCode();
                String responseLength = conn.getHeaderField("Content-length");

                if (responseCode != HttpsURLConnection.HTTP_OK) {
                    throw new IOException("HTTP error code: " + responseCode);
                }

                try {
                    inputStream = conn.getInputStream();
                    // Extract the input stream.
                    //showToast("Received from remote: " + result);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                conn.disconnect();
            }

            return "Success";
        }

        private String convertToString(InputStream is) throws IOException {
            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
            return new String(total);
        }

    }

}
