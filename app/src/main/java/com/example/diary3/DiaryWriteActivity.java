package com.example.diary3;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.diary3.data.AppDatabase;
import com.example.diary3.data.dao.DiaryDao;
import com.example.diary3.data.entity.Diary;

import org.json.JSONException;
import org.json.JSONObject;

public class DiaryWriteActivity extends AppCompatActivity {

    private EditText editTextDiary;
    private EditText editTextTitle;
    private Button buttonSave;
    private Button buttonDelete;
    private DiaryDao diaryDao;
    private String selectedDate;


    private ImageView diaryCharacterImageView;
    private TextView feedbackTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_write);

        editTextDiary = findViewById(R.id.editTextDiary);
        editTextTitle = findViewById(R.id.editTextTitle);
        buttonSave = findViewById(R.id.buttonSave);
        buttonDelete = findViewById(R.id.buttonDelete);


        diaryCharacterImageView = findViewById(R.id.diary_character); // XML에 추가한 ImageView ID
        feedbackTextView = findViewById(R.id.feedbackTextView); // XML에 추가한 TextView ID

        selectedDate = getIntent().getStringExtra("selectedDate");

        diaryDao = AppDatabase.getInstance(this).diaryDao();

        new LoadDiaryTask().execute(selectedDate);

        buttonSave.setOnClickListener(v -> {
            String title = editTextTitle.getText().toString().trim();
            String diaryText = editTextDiary.getText().toString().trim();

            if (title.isEmpty() || diaryText.isEmpty()) {
                new AlertDialog.Builder(DiaryWriteActivity.this)
                        .setTitle("입력 확인")
                        .setMessage("제목과 내용을 모두 입력해주세요.")
                        .setPositiveButton("확인", null)
                        .show();
                return;
            }

            Diary diary = new Diary(selectedDate, title, diaryText, null);
            new SaveDiaryTask().execute(diary);
        });

        buttonDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(DiaryWriteActivity.this)
                    .setTitle("일기 삭제")
                    .setMessage("이 날짜의 일기를 삭제하시겠습니까?")
                    .setPositiveButton("예", (dialog, which) -> new DeleteDiaryTask().execute(selectedDate))
                    .setNegativeButton("아니오", null)
                    .show();
        });

        LinearLayout rootLayout = findViewById(R.id.rootLayout);
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String background = prefs.getString("selectedBackground", "background1");

        switch (background) {
            case "background1":
                rootLayout.setBackgroundResource(R.drawable.letter1);
                break;
            case "background2":
                rootLayout.setBackgroundResource(R.drawable.letter2);
                break;
            case "background3":
                rootLayout.setBackgroundResource(R.drawable.letter3);
                break;
            default:
                rootLayout.setBackgroundColor(getResources().getColor(android.R.color.white)); // 기본 배경
        }
        updateCharacterImage();

    }

    private class LoadDiaryTask extends AsyncTask<String, Void, Diary> {
        @Override
        protected Diary doInBackground(String... dates) {
            return diaryDao.getDiaryByDate(dates[0]);
        }

        @Override
        protected void onPostExecute(Diary diary) {
            if (diary != null) {
                editTextTitle.setText(diary.title);
                editTextDiary.setText(diary.text);
                if (diary.getFeedback() != null && !diary.getFeedback().isEmpty()) { // Getter 사용
                    feedbackTextView.setText(diary.getFeedback());
                } else {
                    feedbackTextView.setText("저장된 피드백이 없습니다.");
                }
            }
            else {
                feedbackTextView.setText("일기를 작성하면 캐릭터의 피드백을 받을 수 있어요!");
            }
        }
    }

    private int getCharacterResId(String characterKey) {
        switch (characterKey) {
            case "character1":
                return R.drawable.character1;
            case "character2":
                return R.drawable.character2;
            case "character3":
                return R.drawable.character3;
            default:
                return R.drawable.character1; // 기본 캐릭터 이미지
        }
    }
    private void updateCharacterImage() {
        if (diaryCharacterImageView == null) return;
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String selectedCharacter = prefs.getString("selectedCharacter", "character1"); // SharedPreferences에서 현재 선택된 캐릭터를 가져옴
        int resId = getCharacterResId(selectedCharacter);
        diaryCharacterImageView.setImageResource(resId);
    }

    private class SaveDiaryTask extends AsyncTask<Diary, Void, Void> {

        private Diary diaryToSave;

        @Override
        protected Void doInBackground(Diary... diaries) {
            diaryToSave = diaries[0];
            diaryDao.insert(diaryToSave);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                JSONObject json = new JSONObject();
                json.put("date", diaryToSave.date);
                json.put("title", diaryToSave.title);
                json.put("text", diaryToSave.text);

                EmotionRequestHelper.analyzeEmotion(json, new EmotionRequestHelper.EmotionCallback() {
                    @Override
                    public void onResult(JSONObject result) {
                        Log.d("EmotionResult", "감정 분석 결과: " + result.toString());
                        String feedbackText = "감정 분석 결과를 가져오는 중...";

                        try {
                            // API 응답 구조에 따라 'sentiment' 또는 다른 키를 사용
                            if (result.has("sentiment")) {
                                feedbackText = "일기 감정: " + result.getString("sentiment");
                            } else if (result.has("overall_sentiment")) {
                                feedbackText = "전반적 감정: " + result.getString("overall_sentiment");
                            } else {
                                feedbackText = "감정 분석 완료 (내용 없음)";
                            }
                        } catch (JSONException e) {
                            Log.e("EmotionResult", "JSON 파싱 오류", e);
                            feedbackText = "감정 분석 결과 파싱 오류";
                        }
                        final String finalFeedbackText = feedbackText;
                        runOnUiThread(() -> {
                            feedbackTextView.setText(finalFeedbackText);
                        });
                        new UpdateDiaryFeedbackTask().execute(diaryToSave.date, feedbackText);

                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("EmotionError", "감정 분석 실패", e);
                        runOnUiThread(() -> {
                            feedbackTextView.setText("감정 분석에 실패했습니다.");
                        });
                        }
                });

            } catch (JSONException e) {
                e.printStackTrace();

                runOnUiThread(() -> {
                            feedbackTextView.setText("일기 저장 중 오류 발생");
                });

            }

            Intent intent = new Intent();
            intent.putExtra("updatedDate", selectedDate);
            setResult(RESULT_OK, intent);
            finish();
        }
    }


    private class UpdateDiaryFeedbackTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            String date = params[0];
            String feedback = params[1];
            diaryDao.updateFeedback(date, feedback); // DiaryDao에 updateFeedback 메서드가 필요합니다.
            return null;
        }
    }


private class DeleteDiaryTask extends AsyncTask<String, Void, Void> {
    @Override
    protected Void doInBackground(String... dates) {
        diaryDao.deleteByDate(dates[0]);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Intent intent = new Intent();
        intent.putExtra("updatedDate", selectedDate);
        setResult(RESULT_OK, intent);
        finish();
    }
}
}

