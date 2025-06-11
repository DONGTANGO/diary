package com.example.diary3.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.diary3.data.entity.UserProfile;

@Dao
public interface UserProfileDao {

    @Query("SELECT * FROM user_profile WHERE id = 1")
    UserProfile getUserProfile();  //

    @Insert
    void insert(UserProfile userProfile); //

    @Update
    void update(UserProfile userProfile); //
}
