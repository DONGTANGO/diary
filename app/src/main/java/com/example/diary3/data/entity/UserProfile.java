package com.example.diary3.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_profile")
public class UserProfile {

    @PrimaryKey(autoGenerate = true)
    public int id = 1; // 기본 1개만 저장하므로 고정 id로 사용

    public String name;
    public String birthDate;   // 생년월일 yyyy-MM-dd 형식
    public String gender;
    public String location;    // 사는 곳
    public String birthday;    // 생일 (월/일)
    public String mbti;
    public String closeFriends;
    public String family;
    public String hobbies;
    public String likes;
}
