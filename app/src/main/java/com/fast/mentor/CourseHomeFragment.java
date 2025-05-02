package com.fast.mentor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class CourseHomeFragment extends Fragment {
    private Course course;
    private RecyclerView rvWeeks, rvModules;
    private WeeksAdapter weeksAdapter;
    private ModuleAdapter moduleAdapter;
    private Map<String, ModuleProgress> progressMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_home, container, false);

        // Initialize views
        rvWeeks = view.findViewById(R.id.rvWeeks);
        rvModules = view.findViewById(R.id.rvModules);

        // Load data
        loadCourseProgress();
        return view;
    }

    private void loadCourseProgress() {
        FirebaseFirestore.getInstance()
                .collection("users").document(userId)
                .collection("enrolledCourses").document(courseId)
                .get()
                .addOnSuccessListener(document -> {
                    CourseProgress progress = document.toObject(CourseProgress.class);
                    progressMap = progress.getModuleProgressMap();
                    updateAdapters();
                });
    }

    private void updateAdapters() {
        // Assume course is loaded with weeks/modules
        weeksAdapter = new WeeksAdapter(course.getWeeks(), this::onWeekSelected);
        rvWeeks.setAdapter(weeksAdapter);

        moduleAdapter = new ModuleAdapter(course.getWeeks().get(0).getModules(), progressMap);
        rvModules.setAdapter(moduleAdapter);
    }

    private void onWeekSelected(int weekPosition) {
        List<Module> modules = course.getWeeks().get(weekPosition).getModules();
        moduleAdapter.updateModules(modules);
    }

    private void setupWeeksRecycler() {
        List<Week> weeks = course.getWeeks(); // Get weeks from course object

        // Configure horizontal layout
        rvWeeks.setLayoutManager(new LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        ));

        // Initialize adapter
        WeeksAdapter weeksAdapter = new WeeksAdapter(weeks, position -> {
            // Update modules when week changes
            List<Module> selectedWeekModules = weeks.get(position).getModules();
            moduleAdapter.updateModules(selectedWeekModules);
        });

        rvWeeks.setAdapter(weeksAdapter);
    }
}
