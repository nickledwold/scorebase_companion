package com.nickledwold.scorebase_companion;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ContinuousHttpGet {

    private OnHttpResultListener resultListener;
    private HttpGetTask httpGetTask;
    private Handler handler;
    private final int INTERVAL = 1000; // 1 second interval

    SharedPreferences sharedPreferences;

    public ContinuousHttpGet(OnHttpResultListener listener, SharedPreferences SP) {
        sharedPreferences = SP;
        this.resultListener = listener;
        handler = new Handler();
    }

    public void startContinuousHttpGet() {
        if (httpGetTask == null) {
            httpGetTask = new HttpGetTask();
            httpGetTask.execute();
        }
    }

    public void stopContinuousHttpGet() {
        if (httpGetTask != null) {
            httpGetTask.cancel(true);
            httpGetTask = null;
            handler.removeCallbacksAndMessages(null);
        }
    }


    private class HttpGetTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            StringBuilder response = new StringBuilder();

            try {
                System.out.println("Attempting API connection");
                String ipAddress = sharedPreferences.getString("ipAddress", "10.0.0.11");
                URL url = new URL("http://"+ipAddress+":1337/competitionData");
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(3 * 1000);
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                } else {
                    resultListener.onHttpErrorOrException();
                }
            } catch (Exception e) {
                resultListener.onHttpErrorOrException();
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }

            return response.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            if (!isCancelled() && resultListener != null) {
                resultListener.onHttpResult(result);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (httpGetTask != null && !httpGetTask.isCancelled()) {
                            httpGetTask = new HttpGetTask();
                            httpGetTask.execute();
                        }
                    }
                }, INTERVAL);
            }
        }
    }

    public interface OnHttpResultListener {
        void onHttpResult(String result);
        void onHttpErrorOrException();
    }
}
