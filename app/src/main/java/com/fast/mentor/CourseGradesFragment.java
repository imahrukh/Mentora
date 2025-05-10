package com.fast.mentor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fast.mentor.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment for displaying course grades
 */
public class CourseGradesFragment extends Fragment {

    private String courseId;
    
    private RecyclerView rvGrades;
    private ProgressBar progressBar;
    private TextView tvNoGrades;
    private TextView tvOverallGrade;
    private TextView tvCourseProgress;
    
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    
    // Map of module ID to module object (for associating grades with modules)
    private Map<String, CourseModule> moduleMap = new HashMap<>();
    
    public CourseGradesFragment() {
        // Required empty public constructor
    }
    
    public static CourseGradesFragment newInstance(String courseId) {
        CourseGradesFragment fragment = new CourseGradesFragment();
        Bundle args = new Bundle();
        args.putString("courseId", courseId);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            courseId = getArguments().getString("courseId");
        }
        
        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_course_grades, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        rvGrades = view.findViewById(R.id.rvGrades);
        progressBar = view.findViewById(R.id.progressBar);
        tvNoGrades = view.findViewById(R.id.tvNoGrades);
        tvOverallGrade = view.findViewById(R.id.tvOverallGrade);
        tvCourseProgress = view.findViewById(R.id.tvCourseProgress);
        
        // Setup recycler view
        rvGrades.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Load modules first (to associate grades with modules)
        loadModules();
    }
    
    private void loadModules() {
        if (courseId == null) {
            showEmptyState();
            return;
        }
        
        showLoading(true);
        
        // Load modules
        firestore.collection("modules")
                .whereEqualTo("courseId", courseId)
                .orderBy("orderIndex")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    moduleMap.clear();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        CourseModule module = document.toObject(CourseModule.class);
                        module.setId(document.getId());
                        moduleMap.put(module.getId(), module);
                    }
                    
                    // After loading modules, load grades
                    loadGrades();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    showEmptyState();
                    Toast.makeText(getContext(), "Failed to load modules: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
    
    private void loadGrades() {
        if (currentUser == null) {
            showEmptyState();
            return;
        }
        
        // First, get the enrollment to check if user is enrolled and get progress
        firestore.collection("enrollments")
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("courseId", courseId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        showLoading(false);
                        showEmptyState();
                        tvNoGrades.setText(R.string.not_enrolled);
                        return;
                    }
                    
                    // Get enrollment data
                    QueryDocumentSnapshot enrollmentDoc = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                    
                    // Get course progress
                    long progress = enrollmentDoc.getLong("progress") != null ? 
                            enrollmentDoc.getLong("progress") : 0;
                    
                    tvCourseProgress.setText(getString(R.string.course_progress_format, progress));
                    
                    // Now load quiz scores
                    loadQuizScores();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    showEmptyState();
                    Toast.makeText(getContext(), "Failed to load enrollment: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
    
    private void loadQuizScores() {
        // In a real app, you would load all graded items (quizzes, assignments)
        // and calculate the overall grade
        
        // For now, we'll just show a placeholder message
        showLoading(false);
        
        // Set overall grade as Not Available for now
        tvOverallGrade.setText(R.string.grade_not_available);
        
        // Show empty state with a message
        showEmptyState();
        tvNoGrades.setText(R.string.no_grades_yet);
    }
    
    private void showEmptyState() {
        rvGrades.setVisibility(View.GONE);
        tvNoGrades.setVisibility(View.VISIBLE);
    }
    
    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }
}