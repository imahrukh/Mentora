package com.fast.mentor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

public class CourseInfoFragment extends Fragment {
    private RecyclerView rvInstructors;
    private TextView tvCourseTitle, tvCourseDescription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_info, container, false);

        tvCourseTitle = view.findViewById(R.id.tvCourseTitle);
        tvCourseDescription = view.findViewById(R.id.tvCourseDescription);
        rvInstructors = view.findViewById(R.id.rvInstructors);

        rvInstructors.setLayoutManager(new LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false));

        loadCourseInfo();
        return view;
    }

    private void loadCourseInfo() {
        String courseId = getArguments().getString("courseId");

        FirebaseFirestore.getInstance()
                .collection("courses").document(courseId)
                .get().addOnSuccessListener(document -> {
                    CourseInfo courseInfo = document.toObject(CourseInfo.class);
                    if(courseInfo != null) {
                        tvCourseTitle.setText(courseInfo.getTitle());
                        tvCourseDescription.setText(courseInfo.getDescription());

                        InstructorAdapter adapter = new InstructorAdapter(courseInfo.getInstructors());
                        rvInstructors.setAdapter(adapter);
                    }
                });
    }
}