package com.fast.mentor;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * Adapter for the course home tabs (Home, Grades, Recommendations, Info)
 */
public class CourseHomeAdapter extends FragmentStateAdapter {

    private final String courseId;
    
    public CourseHomeAdapter(@NonNull FragmentActivity fragmentActivity, String courseId) {
        super(fragmentActivity);
        this.courseId = courseId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return CourseContentFragment.newInstance(courseId);
            case 1:
                return CourseGradesFragment.newInstance(courseId);
            case 2:
                return CourseRecommendationsFragment.newInstance(courseId);
            case 3:
                return CourseInfoFragment.newInstance(courseId);
            default:
                return CourseContentFragment.newInstance(courseId);
        }
    }

    @Override
    public int getItemCount() {
        return 4; // Home, Grades, Recommendations, Info
    }
}