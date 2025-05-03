package com.fast.mentor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class GradesFragment extends Fragment {
    private Course course;
    private RecyclerView rvGrades;
    private LinearProgressIndicator progressBar;
    private TextView tvProgressPercentage;
    private GradesAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grades, container, false);

        rvGrades = view.findViewById(R.id.rvGrades);
        progressBar = view.findViewById(R.id.progressBar);
        tvProgressPercentage = view.findViewById(R.id.tvProgressPercentage);

        loadGradesData();
        return view;
    }

    private void loadGradesData() {
        // Fetch data from Firestore
        FirebaseFirestore.getInstance()
                .collection("users").document(userId)
                .collection("enrolledCourses").document(courseId)
                .get()
                .addOnSuccessListener(document -> {
                    CourseProgress progress = document.toObject(CourseProgress.class);
                    List<GradeItem> gradeItems = processProgressData(progress);
                    updateUI(gradeItems);
                });
    }

    private List<GradeItem> processProgressData(CourseProgress progress) {
        // Convert course progress to grade items
        List<GradeItem> items = new ArrayList<>();

        for(Week week : course.getWeeks()) {
            for(Module module : week.getModules()) {
                for(ModuleItem item : module.getItems()) {
                    if(item.getWeightage() > 0) { // Only graded items
                        GradeItem gradeItem = new GradeItem();
                        gradeItem.setTitle(item.getTitle());
                        gradeItem.setType(item.getType().toString());
                        gradeItem.setWeight(item.getWeightage());
                        gradeItem.setCompleted(ModuleProgress.isItemCompleted(item.getItemId()));
                        items.add(gradeItem);
                    }
                }
            }
        }
        return items;
    }

    private void updateUI(List<GradeItem> gradeItems) {
        // Update progress bar
        float totalWeight = 0;
        float completedWeight = 0;

        for(GradeItem item : gradeItems) {
            totalWeight += item.getWeight();
            if(item.isCompleted()) completedWeight += item.getWeight();
        }

        int progress = (int) ((completedWeight / totalWeight) * 100);
        progressBar.setProgress(progress);
        tvProgressPercentage.setText(progress + "%");

        // Setup RecyclerView
        adapter = new GradesAdapter(gradeItems);
        rvGrades.setAdapter(adapter);
    }
}