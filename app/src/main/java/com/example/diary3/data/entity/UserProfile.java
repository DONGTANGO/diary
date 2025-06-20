package com.example.diary3.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_profile")
public class UserProfile {

    @PrimaryKey(autoGenerate = true)
    public int id = 1;

    public String name;
    public String birthDate;
    public String gender;
    public String location;
    public String birthday;
    public String mbti;
    public String closeFriends;
    public String family;
    public String hobbies;
    public String likes;
}
