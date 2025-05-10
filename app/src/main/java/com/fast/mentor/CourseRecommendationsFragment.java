package com.fast.mentor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fast.mentor.R;

/**
 * Fragment for the "Recommendations" tab in course home activity
 * Displays recommended courses based on current enrollment
 */
public class CourseRecommendationsFragment extends Fragment {

    private static final String ARG_COURSE_ID = "courseId";
    private String courseId;

    public static CourseRecommendationsFragment newInstance(String courseId) {
        CourseRecommendationsFragment fragment = new CourseRecommendationsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_COURSE_ID, courseId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            courseId = getArguments().getString(ARG_COURSE_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // This is a placeholder - we'll implement the full UI later
        View view = inflater.inflate(R.layout.fragment_placeholder, container, false);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.course_recommendations);
        return view;
    }
}