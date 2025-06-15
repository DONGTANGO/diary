package com.example.diary3;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class StoreFragment extends Fragment {

    private final int[] characterIds = { R.id.character1, R.id.character2, R.id.character3 };
    private final int[] backgroundIds = { R.id.background1, R.id.background2, R.id.background3 };

    public StoreFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_store, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        for (int i = 0; i < characterIds.length; i++) {
            ImageView characterView = view.findViewById(characterIds[i]);
            final String prefValue = "character" + (i + 1);
            characterView.setOnClickListener(v -> {
                showConfirmDialog("이 캐릭터를 선택하시겠습니까?", "selectedCharacter", prefValue);
            });
        }

        for (int i = 0; i < backgroundIds.length; i++) {
            ImageView backgroundView = view.findViewById(backgroundIds[i]);
            final String prefValue = "background" + (i + 1);
            backgroundView.setOnClickListener(v -> {
                showConfirmDialog("이 배경을 다이어리 배경으로 선택하시겠습니까?", "selectedBackground", prefValue);
            });
        }
    }

    private void showConfirmDialog(String message, String prefKey, String prefValue) {
        new AlertDialog.Builder(requireContext())
                .setMessage(message)
                .setPositiveButton("확인", (dialog, which) -> {
                    SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                    prefs.edit().putString(prefKey, prefValue).apply();
                })
                .setNegativeButton("취소", null)
                .show();
    }
}
