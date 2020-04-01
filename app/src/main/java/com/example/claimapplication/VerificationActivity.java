package com.example.claimapplication;

import androidx.annotation.RequiresApi;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

public class VerificationActivity extends Activity {

    int verificationCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verification_screen);

        TextView phone_number_txt = findViewById(R.id.phone_number_txt);
        phone_number_txt.setText(FirstActivity.mobile_number);

        final EditText verification_code_edit_txt = findViewById(R.id.verification_code_edit_txt);

        Button submit = findViewById(R.id.submit_btn);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verificationCode = Integer.parseInt(verification_code_edit_txt.getText().toString());
                AsyncT asyncT = new AsyncT();
                asyncT.execute();
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
                URL url = new URL("http://Api-env.pjxxtmeicp.us-east-2.elasticbeanstalk.com/api/sendclaim/codeVerify");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.connect();

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("mobile_number", FirstActivity.mobile_number);
                jsonObject.put("ver_code", verificationCode);

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
            if (responseCode == 1) {
                Intent intent = new Intent(VerificationActivity.this, SecondActivity.class);
                startActivity(intent);
                FirstActivity.sharedPreferences.edit().putBoolean("isVerified", true).apply();
            } else if (responseCode == -1) {
                Toast.makeText(getBaseContext(), "Incorrect Verification Code", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getBaseContext(), "Error", Toast.LENGTH_LONG).show();
            }
        }
    }
}
