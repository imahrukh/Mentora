package com.fast.mentor;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fast.mentor.R;
import com.fast.mentor.courses.CourseDetailActivity;
import com.fast.mentor.courses.ExploreFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for the Learn tab
 * Shows enrolled courses and progress
 */
public class LearnFragment extends Fragment implements TabLayout.OnTabSelectedListener {

    private TabLayout tabLayout;
    private RecyclerView rvEnrolledCourses;
    private ConstraintLayout emptyStateContainer;
    private Button btnStartLearning;
    private ProgressBar progressBar;

    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    private EnrolledCourseAdapter adapter;
    private List<EnrolledCourse> enrolledCoursesList;

    private boolean isShowingInProgress = true; // true for "In Progress", false for "Completed"

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_learn, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Initialize views
        tabLayout = view.findViewById(R.id.tabLayout);
        rvEnrolledCourses = view.findViewById(R.id.rvEnrolledCourses);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
        btnStartLearning = view.findViewById(R.id.btnStartLearning);
        progressBar = view.findViewById(R.id.progressBar);

        // Setup tab selection listener
        tabLayout.addOnTabSelectedListener(this);

        // Setup RecyclerView
        enrolledCoursesList = new ArrayList<>();
        adapter = new EnrolledCourseAdapter(enrolledCoursesList, course -> {
            // Navigate to course home activity
            navigateToCourseHome(course);
        });

        rvEnrolledCourses.setLayoutManager(new LinearLayoutManager(getContext()));
        rvEnrolledCourses.setAdapter(adapter);

        // Setup "Start Learning" button in empty state
        btnStartLearning.setOnClickListener(v -> {
            // Navigate to explore fragment
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new ExploreFragment())
                        .commit();
                
                // Update bottom navigation selection
                if (getActivity().findViewById(R.id.bottomNavigationView) != null) {
                    getActivity().findViewById(R.id.bottomNavigationView).findViewById(R.id.nav_explore).performClick();
                }
            }
        });

        // Load enrolled courses
        loadEnrolledCourses();
    }

    private void loadEnrolledCourses() {
        if (currentUser == null) {
            showEmptyState();
            return;
        }

        showLoading(true);

        String userId = currentUser.getUid();
        firestore.collection("enrollments")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isCompleted", !isShowingInProgress) // Filter based on tab
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    enrolledCoursesList.clear();
                    
                    if (queryDocumentSnapshots.isEmpty()) {
                        showEmptyState();
                    } else {
                        List<String> courseIds = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String courseId = document.getString("courseId");
                            if (courseId != null) {
                                courseIds.add(courseId);
                            }
                        }
                        
                        loadCourseDetails(courseIds, queryDocumentSnapshots);
                    }
                })
                .addOnFailureListener(e -> {
                    showEmptyState();
                    showLoading(false);
                });
    }

    private void loadCourseDetails(List<String> courseIds, Iterable<QueryDocumentSnapshot> enrollmentDocs) {
        if (courseIds.isEmpty()) {
            showEmptyState();
            return;
        }

        for (String courseId : courseIds) {
            firestore.collection("courses")
                    .document(courseId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Find corresponding enrollment doc
                            for (QueryDocumentSnapshot enrollmentDoc : enrollmentDocs) {
                                if (courseId.equals(enrollmentDoc.getString("courseId"))) {
                                    // Create enrolled course object
                                    EnrolledCourse enrolledCourse = new EnrolledCourse();
                                    enrolledCourse.setCourseId(courseId);
                                    enrolledCourse.setTitle(documentSnapshot.getString("title"));
                                    enrolledCourse.setInstructorName(documentSnapshot.getString("instructorName"));
                                    enrolledCourse.setThumbnailUrl(documentSnapshot.getString("thumbnailUrl"));
                                    enrolledCourse.setDurationHours(documentSnapshot.getLong("durationHours") != null ? 
                                            documentSnapshot.getLong("durationHours").intValue() : 0);
                                    
                                    // Set progress from enrollment
                                    double progress = enrollmentDoc.getDouble("progress") != null ? 
                                            enrollmentDoc.getDouble("progress") : 0.0;
                                    enrolledCourse.setProgress((float) progress);
                                    
                                    // Calculate time remaining
                                    if (progress < 100 && enrolledCourse.getDurationHours() > 0) {
                                        float remainingHours = (1 - (float) progress / 100) * enrolledCourse.getDurationHours();
                                        enrolledCourse.setRemainingHours(remainingHours);
                                    } else {
                                        enrolledCourse.setRemainingHours(0);
                                    }
                                    
                                    enrolledCoursesList.add(enrolledCourse);
                                    break;
                                }
                            }
                            
                            // Update UI when all courses are loaded
                            if (enrolledCoursesList.size() == courseIds.size()) {
                                if (enrolledCoursesList.isEmpty()) {
                                    showEmptyState();
                                } else {
                                    showCourses();
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // If we fail to load even one course, still show what we have
                        if (enrolledCoursesList.isEmpty()) {
                            showEmptyState();
                        } else {
                            showCourses();
                        }
                    });
        }
    }

    private void showEmptyState() {
        showLoading(false);
        rvEnrolledCourses.setVisibility(View.GONE);
        emptyStateContainer.setVisibility(View.VISIBLE);
    }

    private void showCourses() {
        showLoading(false);
        adapter.notifyDataSetChanged();
        emptyStateContainer.setVisibility(View.GONE);
        rvEnrolledCourses.setVisibility(View.VISIBLE);
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        rvEnrolledCourses.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        emptyStateContainer.setVisibility(View.GONE);
    }

    private void navigateToCourseHome(EnrolledCourse course) {
        Intent intent = new Intent(getActivity(), CourseHomeActivity.class);
        intent.putExtra("courseId", course.getCourseId());
        startActivity(intent);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        isShowingInProgress = tab.getPosition() == 0;
        loadEnrolledCourses();
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        // Not needed
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        // Reload when tab is reselected (optional refresh functionality)
        loadEnrolledCourses();
    }
}