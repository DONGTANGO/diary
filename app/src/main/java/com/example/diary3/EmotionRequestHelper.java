package com.example.diary3;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.*;

public class EmotionRequestHelper {

    private static final OkHttpClient client = new OkHttpClient();
    private static final String SERVER_URL = "http://10.0.2.2:5000/analyze"; // 추후 외부 IP로 변경

    // 콜백 인터페이스: 다양한 정보 처리를 위한 JSON 전체 결과 전달
    public interface EmotionCallback {
        void onResult(JSONObject result); // JSONObject 통째로 받음
        void onError(Exception e);
    }

    // 다양한 데이터를 JSON으로 전송
    public static void analyzeEmotion(JSONObject dataToSend, EmotionCallback callback) {
        try {
            RequestBody body = RequestBody.create(
                    dataToSend.toString(),
                    MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(SERVER_URL)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    callback.onError(e);
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        if (!response.isSuccessful()) {
                            throw new IOException("Unexpected code " + response);
                        }

                        String responseBody = response.body().string();
                        JSONObject resultJson = new JSONObject(responseBody);
                        callback.onResult(resultJson);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onError(e);
                    }
                }
            });

        } catch (Exception e) {
            callback.onError(e);
        }
    }
}
