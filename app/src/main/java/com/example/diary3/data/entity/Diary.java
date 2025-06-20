package com.example.diary3.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "diary")
public class Diary {

    @PrimaryKey(autoGenerate = false)
    @NonNull
    public String date;
    public String title;
    public String text;
    public String feedback;


    public String getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }
    public String getFeedback() {
        return feedback;
    }

    public Diary(@NonNull String date, String title, String text, String feedback) {
        this.date = date;
        this.title = title;
        this.text = text;
        this.feedback = feedback;
    }
}
