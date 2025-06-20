package com.example.diary3;



import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;




public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private ViewPager2 viewPager;  //스와이프



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        viewPager = findViewById(R.id.viewPager);//스와이프

        viewPager.setAdapter(new ScreenSlidePagerAdapter(this));  //스와이프

        bottomNavigationView.setOnItemSelectedListener(item -> {
//            Fragment selectedFragment;

            int itemId = item.getItemId();
            if (itemId == R.id.nav_calendar) {
                viewPager.setCurrentItem(0); // MainFragment
            } else if (itemId == R.id.nav_store) {
                viewPager.setCurrentItem(1); // StoreFragment
            } else if (itemId == R.id.nav_profile) {
                viewPager.setCurrentItem(2); // ProfileFragment
            }
            return true;
        });


        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
            }
        });
    }

    private static class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return new MainFragment();
                case 1: return new StoreFragment();
                case 2: return new ProfileFragment();
                default: return new MainFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}
