package com.example.familyagent;

import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ServerCommunicator {
    void sendEmail(String email, String subject, String body) {
        // avoid creating several instances, should be singleon
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("email", email)
                .add("subject", subject)
                .add("body", body)
                .build();
        Request request = new Request.Builder()
                .url("http://23.92.74.62:8080/mailsender/emailsender.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.d("Response: ", "Post request got failed reason : " + response.message());
                    //throw new IOException("Unexpected code " + response);
                } else {
                    // do something wih the result
                    // successfully send the email.
                    Log.d("Response: ", "Post request got successful: response : " + response.message());
                }
            }
        });
    }
}
