package com.example.diary3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_write);

        editTextDiary = findViewById(R.id.editTextDiary);
        editTextTitle = findViewById(R.id.editTextTitle);
        buttonSave = findViewById(R.id.buttonSave);
        buttonDelete = findViewById(R.id.buttonDelete);

        selectedDate = getIntent().getStringExtra("selectedDate");

        diaryDao = AppDatabase.getInstance(this).diaryDao();

        // DB에서 기존 데이터 불러오기
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

        EditText rootLayout = findViewById(R.id.editTextDiary);
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
            }
        }
    }

    private class SaveDiaryTask extends AsyncTask<Diary, Void, Void> {

        private Diary diaryToSend; // 🔽 [추가]

        @Override
        protected Void doInBackground(Diary... diaries) {
//            diaryDao.insert(diaries[0]);
            diaryToSend = diaries[0]; // 🔽 [추가]
            diaryDao.insert(diaryToSend);
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {

            try {
                JSONObject json = new JSONObject();
                json.put("date", diaryToSend.date);
                json.put("title", diaryToSend.title);
                json.put("text", diaryToSend.text);

                EmotionRequestHelper.analyzeEmotion(json, new EmotionRequestHelper.EmotionCallback() {
                    @Override
                    public void onResult(JSONObject result) {
                        Log.d("EmotionResult", "감정 분석 결과: " + result.toString());
                        // TODO: 결과 활용 (예: DB에 저장, UI 반영 등)
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("EmotionError", "감정 분석 실패", e);
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent();
            intent.putExtra("updatedDate", selectedDate);
            setResult(RESULT_OK, intent);
            finish();
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

