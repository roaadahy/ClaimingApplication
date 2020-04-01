package com.example.claimapplication;

import androidx.annotation.RequiresApi;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class FirstActivity extends Activity {

    static String mobile_number;
    static SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);

        mobile_number = sharedPreferences.getString("MOBILE_NUMBER_KEY", "");
        boolean isFirstRun = sharedPreferences.getBoolean("isFirstRun", true);
        boolean isVerified = sharedPreferences.getBoolean("isVerified", false);

        if (isFirstRun || !isVerified) {
            setContentView(R.layout.first_screen);

            final EditText mobile_number_edit_txt = findViewById(R.id.mobile_number_edit_txt);
            final Button next = findViewById(R.id.next_btn);

            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mobile_number = mobile_number_edit_txt.getText().toString();
                    if (mobile_number.length() != 11) {
                        Toast.makeText(FirstActivity.this, "Incorrect Phone Number", Toast.LENGTH_LONG).show();
                        mobile_number_edit_txt.setText("");
                    } else if (!mobile_number.startsWith("01")) {
                        Toast.makeText(FirstActivity.this, "Incorrect Phone Number", Toast.LENGTH_LONG).show();
                        mobile_number_edit_txt.setText("");
                    } else {
                        sharedPreferences.edit().putString("MOBILE_NUMBER_KEY", mobile_number).apply();
                        sharedPreferences.edit().putBoolean("isFirstRun", false).apply();

                        AsyncT asyncT = new AsyncT();
                        asyncT.execute();
                    }
                }
            });

        } else {
            Intent intent = new Intent(FirstActivity.this, SecondActivity.class);
            startActivity(intent);
        }
    }

    class AsyncT extends AsyncTask<Void, Void, Void> {
        int responseCode;

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                URL url = new URL("http://api-env.pjxxtmeicp.us-east-2.elasticbeanstalk.com/api/sendclaim/reg");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.connect();

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("mobile_number", mobile_number);

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

                    String numberResponse;
                    numberResponse = jsonResponse.toString();
                    if (numberResponse.contains("\"Code\":1")) {
                        responseCode = 1;
                    } else if (numberResponse.contains("\"Code\":-1")) {
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

            SmsVerify smsVerify = new SmsVerify();
            if (responseCode == 1) {
                smsVerify.executeAsyncTask();
            } else if (responseCode == -1) {
                Toast.makeText(getBaseContext(), "Already existing", Toast.LENGTH_LONG).show();
                smsVerify.executeAsyncTask();
            } else {
                Toast.makeText(getBaseContext(), "Error", Toast.LENGTH_LONG).show();
            }
        }
    }


    public class SmsVerify {

        protected void executeAsyncTask() {
            AsyncT asyncT = new AsyncT();
            asyncT.execute();
        }

        class AsyncT extends AsyncTask<Void, Void, Void> {
            int smsResponseCode;

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    URL url = new URL("http://api-env.pjxxtmeicp.us-east-2.elasticbeanstalk.com/api/sendclaim/smsVerify");
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
                        StringBuilder jsonResponse = new StringBuilder();
                        String responseLine;
                        while ((responseLine = read.readLine()) != null) {
                            jsonResponse.append(responseLine.trim());
                        }

                        System.out.println(jsonResponse.toString());

                        String numberResponse = jsonResponse.toString();
                        if (numberResponse.contains("\"Code\":1")) {
                            smsResponseCode = 1;
                        } else if (numberResponse.contains("\"Code\":-1")) {
                            smsResponseCode = -1;
                        } else {
                            smsResponseCode = 0;
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

                if (smsResponseCode == 1) {
                    Intent intent = new Intent(FirstActivity.this, VerificationActivity.class);
                    startActivity(intent);
                } else if (smsResponseCode == -1) {
                    Toast.makeText(getBaseContext(), "User not registered or Code already verified", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getBaseContext(), "Error", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
