package com.example.diary3.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.diary3.data.entity.Diary;

import java.util.List;

@Dao
public interface DiaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Diary diary);

    @Update
    void update(Diary diary);

    @Query("SELECT * FROM diary WHERE date = :date")
    Diary getDiaryByDate(String date);

    @Query("SELECT * FROM diary")
    List<Diary> getAllDiaries();

    @Query("DELETE FROM diary WHERE date = :date")
    void deleteByDate(String date);

    @Query("UPDATE diary SET feedback = :feedback WHERE date = :date")
    void updateFeedback(String date, String feedback);


    @Query("SELECT feedback FROM diary WHERE substr(date, 1, 7) = :yearMonthString AND feedback IS NOT NULL AND feedback != ''")
    List<String> getFeedbacksByYearMonth(String yearMonthString);
}
