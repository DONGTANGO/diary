package com.example.diary3.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.diary3.data.dao.DiaryDao;
import com.example.diary3.data.entity.Diary;

@Database(entities = {Diary.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DiaryDao diaryDao();
}
