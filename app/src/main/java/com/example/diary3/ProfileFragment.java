package com.example.diary3;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.diary3.data.entity.UserProfile;
import com.example.diary3.data.AppDatabase;

public class ProfileFragment extends Fragment {

    private EditText editName, editBirthDate, editGender, editLocation, editBirthday,
            editMBTI, editCloseFriends, editFamily, editHobbies, editLikes;
    private Button btnSaveOrEdit;

    private AppDatabase db;

    private boolean isEditing = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        editName = view.findViewById(R.id.editName);
        editBirthDate = view.findViewById(R.id.editBirthDate);
        editGender = view.findViewById(R.id.editGender);
        editLocation = view.findViewById(R.id.editLocation);
        editBirthday = view.findViewById(R.id.editBirthday);
        editMBTI = view.findViewById(R.id.editMBTI);
        editCloseFriends = view.findViewById(R.id.editCloseFriends);
        editFamily = view.findViewById(R.id.editFamily);
        editHobbies = view.findViewById(R.id.editHobbies);
        editLikes = view.findViewById(R.id.editLikes);

        btnSaveOrEdit = view.findViewById(R.id.btnSaveOrEdit);

        db = AppDatabase.getInstance(requireContext());

        loadUserProfile();

        btnSaveOrEdit.setOnClickListener(v -> {
            if (isEditing) {
                saveUserProfile();
            } else {
                setEditTextsEnabled(true);
                btnSaveOrEdit.setText("저장");
                isEditing = true;
            }
        });

        return view;
    }

    private void setEditTextsEnabled(boolean enabled) {
        editName.setEnabled(enabled);
        editBirthDate.setEnabled(enabled);
        editGender.setEnabled(enabled);
        editLocation.setEnabled(enabled);
        editBirthday.setEnabled(enabled);
        editMBTI.setEnabled(enabled);
        editCloseFriends.setEnabled(enabled);
        editFamily.setEnabled(enabled);
        editHobbies.setEnabled(enabled);
        editLikes.setEnabled(enabled);
    }

    private void loadUserProfile() {
        new AsyncTask<Void, Void, UserProfile>() {
            @Override
            protected UserProfile doInBackground(Void... voids) {
                return db.userProfileDao().getUserProfile();
            }

            @Override
            protected void onPostExecute(UserProfile userProfile) {
                if (userProfile != null) {
                    editName.setText(userProfile.name);
                    editBirthDate.setText(userProfile.birthDate);
                    editGender.setText(userProfile.gender);
                    editLocation.setText(userProfile.location);
                    editBirthday.setText(userProfile.birthday);
                    editMBTI.setText(userProfile.mbti);
                    editCloseFriends.setText(userProfile.closeFriends);
                    editFamily.setText(userProfile.family);
                    editHobbies.setText(userProfile.hobbies);
                    editLikes.setText(userProfile.likes);

                    setEditTextsEnabled(false);
                    btnSaveOrEdit.setText("수정");
                    isEditing = false;
                } else {
                    setEditTextsEnabled(true);
                    btnSaveOrEdit.setText("저장");
                    isEditing = true;
                }
            }
        }.execute();
    }

    private void saveUserProfile() {
        String name = editName.getText().toString().trim();
        String birthDate = editBirthDate.getText().toString().trim();
        String gender = editGender.getText().toString().trim();
        String location = editLocation.getText().toString().trim();
        String birthday = editBirthday.getText().toString().trim();
        String mbti = editMBTI.getText().toString().trim();
        String closeFriends = editCloseFriends.getText().toString().trim();
        String family = editFamily.getText().toString().trim();
        String hobbies = editHobbies.getText().toString().trim();
        String likes = editLikes.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(requireContext(), "이름을 입력해주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        UserProfile userProfile = new UserProfile();
        userProfile.id = 1;  // 고정 id
        userProfile.name = name;
        userProfile.birthDate = birthDate;
        userProfile.gender = gender;
        userProfile.location = location;
        userProfile.birthday = birthday;
        userProfile.mbti = mbti;
        userProfile.closeFriends = closeFriends;
        userProfile.family = family;
        userProfile.hobbies = hobbies;
        userProfile.likes = likes;

        new AsyncTask<UserProfile, Void, Void>() {
            @Override
            protected Void doInBackground(UserProfile... profiles) {
                UserProfile existing = db.userProfileDao().getUserProfile();
                if (existing == null) {
                    db.userProfileDao().insert(profiles[0]);
                } else {
                    db.userProfileDao().update(profiles[0]);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                Toast.makeText(requireContext(), "저장되었습니다", Toast.LENGTH_SHORT).show();
                setEditTextsEnabled(false);
                btnSaveOrEdit.setText("수정");
                isEditing = false;
            }
        }.execute(userProfile);
    }
}
