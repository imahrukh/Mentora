package com.fast.mentor;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class CoursePagerAdapter extends FragmentStateAdapter {
    public CoursePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new CourseHomeFragment();
            case 1: return new GradesFragment();

            default: return new CourseHomeFragment();
        }
    }

    @Override
    public int getItemCount() { return 5; } // Home, Grades, Notes, Recommendations, Info
}
