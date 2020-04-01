package com.example.claimapplication;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SecondActivity extends Activity {

    String latitude, longitude;
    String lastLatitude, lastLongitude;
    boolean isFirstClaim;
    private GpsTracker gpsTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_screen);

        lastLatitude = FirstActivity.sharedPreferences.getString("LATITUDE_KEY", "");
        lastLongitude = FirstActivity.sharedPreferences.getString("LONGITUDE_KEY", "");
        isFirstClaim = FirstActivity.sharedPreferences.getBoolean("isFirstClaim", true);

        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Button claim = findViewById(R.id.claim_btn);
        claim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gpsTracker = new GpsTracker(SecondActivity.this);

                if (gpsTracker.canGetLocation()) {
                    if (isFirstClaim) {
                        latitude = String.valueOf(gpsTracker.getLatitude());
                        lastLatitude = String.valueOf(Double.valueOf(latitude) + 0.004504);
                        longitude = String.valueOf(gpsTracker.getLongitude());
                        lastLongitude = String.valueOf(Double.valueOf(longitude) + 0.004504);
                        FirstActivity.sharedPreferences.edit().putBoolean("isFirstClaim", false).apply();
                    } else {
                        latitude = String.valueOf(gpsTracker.getLatitude());
                        longitude = String.valueOf(gpsTracker.getLongitude());
                    }
                } else {
                    gpsTracker.showSettingsAlert();
                }

                if (Float.compare((float)(Math.abs(Double.valueOf(latitude) - Double.valueOf(lastLatitude))) , (float)(0.004504)) == 0 ) {
                    if (Float.compare((float) (Math.abs(Double.valueOf(longitude) - Double.valueOf(lastLongitude))), (float) (0.004504)) == 0) {
                        FirstActivity.sharedPreferences.edit().putString("LATITUDE_KEY", latitude).apply();
                        FirstActivity.sharedPreferences.edit().putString("LONGITUDE_KEY", longitude).apply();

                        AsyncT asyncT = new AsyncT();
                        asyncT.execute();
                    }
                } else {
                    Toast.makeText(getBaseContext(), "Already sent", Toast.LENGTH_LONG).show();
                }

                System.out.println(FirstActivity.sharedPreferences.getAll());
            }
        });

        Button showMyHistory = findViewById(R.id.show_my_history_btn);
        showMyHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SecondActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }


    class AsyncT extends AsyncTask<Void, Void, Void> {
        int responseCode;

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                URL url = new URL("http://Api-env.pjxxtmeicp.us-east-2.elasticbeanstalk.com/api/sendclaim/send");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("mobile_number", FirstActivity.mobile_number);
                jsonObject.put("latitude", latitude);
                jsonObject.put("longitude", longitude);

                DataOutputStream write = new DataOutputStream(httpURLConnection.getOutputStream());
                write.writeBytes(jsonObject.toString());
                write.flush();
                write.close();

                try (BufferedReader read = new BufferedReader(
                        new InputStreamReader(httpURLConnection.getInputStream(), "utf-8"))) {
                    StringBuilder jsonResponse = new StringBuilder();
                    String responseLine;
                    while ((responseLine = read.readLine()) != null) {
                        jsonResponse.append(responseLine.trim());
                    }
                    System.out.println(jsonResponse.toString());

                    String locResponse;
                    locResponse = jsonResponse.toString();
                    if (locResponse.contains("\"Code\":1")) {
                        responseCode = 1;
                    } else if (locResponse.contains("\"Code\":-1")) {
                        responseCode = -1;
                    } else {
                        responseCode = 0;
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
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (responseCode == 1) {
                Toast.makeText(getBaseContext(), "Your claim has been successfully sent!", Toast.LENGTH_LONG).show();
            } else if (responseCode == -1) {
                Toast.makeText(getBaseContext(), "Another claim is already pending at the same location", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getBaseContext(), "Error while processing! Please try again later", Toast.LENGTH_LONG).show();
            }
        }
    }
}