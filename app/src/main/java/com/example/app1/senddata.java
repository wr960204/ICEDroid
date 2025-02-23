package com.example.app1;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class senddata {
    public void sendDataToServer(String data) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        RequestBody body = new FormBody.Builder()
                .add("payload", data)
                .build();

        Request request = new Request.Builder()
                .url("http://119.29.243.226:8000/learn/receive_file_content/")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("Network", "Send failed: " + e.getMessage());
                e.printStackTrace(); // 添加完整堆栈跟踪
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    String body = response.body().string();
                    if (response.isSuccessful()) {
                        Log.d("Network", "Send success. Response: " + body);
                    } else {
                        Log.e("Network", "Server error: " + response.code() + "\n" + body);
                    }
                } catch (IOException e) {
                    Log.e("Network", "Response parsing error", e);
                }
            }
        });
    }
}
