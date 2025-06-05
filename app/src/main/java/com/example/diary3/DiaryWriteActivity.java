package com.example.diary3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class DiaryWriteActivity extends AppCompatActivity {

    private EditText editTextDiary;
    private EditText editTextTitle;  // 제목 입력
    private Button buttonSave;
    private Button buttonDelete; // 추가

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_write);

        editTextDiary = findViewById(R.id.editTextDiary);
        editTextTitle = findViewById(R.id.editTextTitle); // 추가
        buttonSave = findViewById(R.id.buttonSave);
        buttonDelete = findViewById(R.id.buttonDelete); // 연결

        String selectedDate = getIntent().getStringExtra("selectedDate");



        SharedPreferences prefs = getSharedPreferences("Diary", MODE_PRIVATE);
        String savedText = prefs.getString(selectedDate + "_text", "");
        String savedTitle = prefs.getString(selectedDate + "_title", "");
        editTextDiary.setText(savedText);
        editTextTitle.setText(savedTitle);

        Log.d("DiaryWrite", "title: " + savedTitle);
        Log.d("DiaryWrite", "text: " + savedText);


        buttonSave.setOnClickListener(v -> {
            String title = editTextTitle.getText().toString().trim();
            String diaryText = editTextDiary.getText().toString().trim();

            // 제목 또는 내용이 비어 있으면 저장 막고 안내
            if (title.isEmpty() || diaryText.isEmpty()) {
                new AlertDialog.Builder(DiaryWriteActivity.this)
                        .setTitle("입력 확인")
                        .setMessage("제목과 내용을 모두 입력해주세요.")
                        .setPositiveButton("확인", null)
                        .show();
                return; // 저장 중단
                }
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(selectedDate + "_title", title);
            editor.putString(selectedDate + "_text", diaryText);
            editor.apply();

            Log.d("DiaryWriteActivity", "저장됨 - 날짜: " + selectedDate + ", 제목: " + title + ", 내용: " + diaryText);


            Intent intent = new Intent();
            intent.putExtra("updatedDate", selectedDate); // 예: "2025-06-01"
            setResult(RESULT_OK, intent);

            finish(); // 저장 후 닫기
        });


        buttonDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(DiaryWriteActivity.this)
                    .setTitle("일기 삭제")
                    .setMessage("이 날짜의 일기를 삭제하시겠습니까?")
                    .setPositiveButton("예", (dialog, which) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(selectedDate + "_title");
            editor.remove(selectedDate + "_text");
            editor.apply();

                        Log.d("DiaryWriteActivity", "삭제됨 - 날짜: " + selectedDate);

                        Intent intent = new Intent();
                        intent.putExtra("updatedDate", selectedDate);
                        setResult(RESULT_OK, intent);
            editor.apply();
            finish(); // 삭제 후 닫기
        })
            .setNegativeButton("아니오", null)
                    .show();
        });
    }
}
