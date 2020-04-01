package com.example.claimapplication;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class HistoryActivity extends Activity {

    String[] address;
    String[] claimDateTime;
    String[] status;
    String[] authentic;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executeAsyncTask();
    }

    protected void executeAsyncTask() {
        AsyncT asyncT = new AsyncT();
        asyncT.execute();
    }


    class AsyncT extends AsyncTask<Void, Void, Void> {
        int array_length;
        StringBuilder jsonResponse;

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                URL url = new URL("http://api-env.pjxxtmeicp.us-east-2.elasticbeanstalk.com/api/sendclaim/history");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.connect();

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("mobile_number", FirstActivity.mobile_number);

                DataOutputStream write = new DataOutputStream(httpURLConnection.getOutputStream());
                write.writeBytes(jsonObject.toString());
                write.flush();
                write.close();

                try (BufferedReader read = new BufferedReader(
                        new InputStreamReader(httpURLConnection.getInputStream(), "utf-8"))) {
                    jsonResponse = new StringBuilder();
                    String responseLine;
                    while ((responseLine = read.readLine()) != null) {
                        jsonResponse.append(responseLine.trim());
                    }

                    System.out.println(jsonResponse.toString());

                    if (jsonResponse.toString().contains("\"code\":1")) {
                        JSONObject root = new JSONObject(jsonResponse.toString());
                        JSONArray jsonArray = root.getJSONArray("Names");
                        array_length = jsonArray.length();
                        address = new String[array_length];
                        claimDateTime = new String[array_length];
                        status = new String[array_length];
                        authentic = new String[array_length];

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject nameObject = jsonArray.getJSONObject(i);
                            address[i] = nameObject.getString("Name");
                            JSONObject stateObject = nameObject.getJSONObject("state");
                            claimDateTime[i] = stateObject.getString("claim_date_time");
                            status[i] = stateObject.getString("status");
                            authentic[i] = stateObject.getString("authentic");
                        }
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void aVoid) {
            super.onPostExecute(aVoid);
            if (jsonResponse.toString().contains("\"code\":1")) {
                setContentView(R.layout.history_screen);
                ArrayList<HistoryData> historyList = new ArrayList<>();
                for (int i = 0; i < array_length; i++) {
                    if (authentic[i].equals("1")) {
                        authentic[i] = "authentic";
                    } else if (authentic[i].equals("0")) {
                        authentic[i] = "unknown";
                    } else {
                        authentic[i] = "bogus";
                    }
                    claimDateTime[i] = claimDateTime[i].replace("T", " ");
                    claimDateTime[i] = claimDateTime[i].replace("Z", " ");

                    historyList.add(new HistoryData(address[i], claimDateTime[i], status[i], authentic[i]));
                }
                ListView history_list_view = findViewById(R.id.history_list_view);
                HistoryListAdapter adapter = new HistoryListAdapter(HistoryActivity.this, 0, historyList);
                history_list_view.setAdapter(adapter);

                final SwipeRefreshLayout swipeLayout = findViewById(R.id.swipe_refresh_layout);
                swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        executeAsyncTask();
                    }
                });

            } else if (jsonResponse.toString().contains("\"Code\":-1")) {
                finish();
                Toast.makeText(getBaseContext(), "No past Claims", Toast.LENGTH_LONG).show();
            } else {
                finish();
                Toast.makeText(getBaseContext(), "Error", Toast.LENGTH_LONG).show();
            }
        }
    }
}