package com.example.diary3;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.*;

public class EmotionRequestHelper {

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(300, TimeUnit.SECONDS)
            .writeTimeout(300, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS)
            .build();
    private static final String SERVER_URL = "http://34.58.122.169:5000/analyze";


    private static final String MONTHLY_SUMMARY_URL = "http://34.58.122.169:5000/monthly_summary";

    public interface EmotionCallback {
        void onResult(JSONObject result);
        void onError(Exception e);
    }

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

    public static JSONObject callSyncMonthlySummaryApi(JSONObject dataToSend) throws IOException, JSONException {
        RequestBody body = RequestBody.create(
                dataToSend.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(MONTHLY_SUMMARY_URL) // 월간 요약 분석 URL 사용
                .post(body)
                .build();

        Response response = client.newCall(request).execute(); // 동기 호출!

        try {
            String responseBody = response.body() != null ? response.body().string() : "{}";
            Log.d("EmotionRequestHelper", "월간 요약 API 응답: " + responseBody);

            if (!response.isSuccessful()) {
                throw new IOException("월간 요약 API 요청 실패. 응답 코드: " + response.code() + ", 에러: " + responseBody);
            }

            return new JSONObject(responseBody);
        } finally {
            response.close();
        }
    }
}
