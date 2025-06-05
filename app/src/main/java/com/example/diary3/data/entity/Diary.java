package com.example.diary3.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "diary")
public class Diary {

    @PrimaryKey(autoGenerate = false)
    public String date;  // yyyy-MM-dd 같은 날짜 문자열

    public String title;
    public String text;
    public String emotion;

    public Diary(String date, String title, String text, String emotion) {
        this.date = date;
        this.title = title;
        this.text = text;
        this.emotion = emotion;
    }
}
