package com.example.diary3.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.diary3.data.dao.DiaryDao;
import com.example.diary3.data.dao.UserProfileDao;
import com.example.diary3.data.entity.Diary;
import com.example.diary3.data.entity.UserProfile;


@Database(entities = {Diary.class, UserProfile.class}, version = 4)
public abstract class AppDatabase extends RoomDatabase {

    public abstract DiaryDao diaryDao();
    public abstract UserProfileDao userProfileDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized(AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "diary_database")
                            .fallbackToDestructiveMigration()  // 이 줄 추가!
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
