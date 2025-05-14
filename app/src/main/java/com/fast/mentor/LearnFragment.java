package com.fast.mentor;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fast.mentor.CourseHomeActivity;
import com.fast.mentor.ExploreFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LearnFragment extends Fragment implements TabLayout.OnTabSelectedListener {

    private TabLayout tabLayout;
    private RecyclerView rvEnrolledCourses;
    private ConstraintLayout emptyStateContainer;
    private Button btnStartLearning;
    private ProgressBar progressBar;

    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    private EnrolledCourseAdapter adapter;
    private List<EnrolledCourse> enrolledCoursesList = new ArrayList<>();
    private boolean isShowingInProgress = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_learn, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        tabLayout = view.findViewById(R.id.tabLayout);
        rvEnrolledCourses = view.findViewById(R.id.rvEnrolledCourses);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
        btnStartLearning = view.findViewById(R.id.btnStartLearning);
        progressBar = view.findViewById(R.id.progressBar);

        tabLayout.addOnTabSelectedListener(this);

        adapter = new EnrolledCourseAdapter(enrolledCoursesList, course -> navigateToCourseHome(course));
        rvEnrolledCourses.setLayoutManager(new LinearLayoutManager(getContext()));
        rvEnrolledCourses.setAdapter(adapter);

        btnStartLearning.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, new ExploreFragment())
                        .commit();
            }
        });

        uploadDummyEnrollment(currentUser.getUid());
        loadEnrolledCourses();
    }

    private void loadEnrolledCourses() {
        if (currentUser == null) {
            showEmptyState();
            return;
        }

        showLoading(true);
        enrolledCoursesList.clear();
        String userId = currentUser.getUid();

        firestore.collection("enrollments")
                .whereEqualTo("userId", userId)
                //.whereEqualTo("isCompleted", !isShowingInProgress) // Temporarily removed
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    if (querySnapshots.isEmpty()) {
                        showEmptyState();
                        return;
                    }

                    Map<String, QueryDocumentSnapshot> enrollmentMap = new HashMap<>();
                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        String courseId = doc.getString("courseId");
                        if (courseId != null) {
                            enrollmentMap.put(courseId, doc);
                        }
                    }

                    if (enrollmentMap.isEmpty()) {
                        showEmptyState();
                        return;
                    }

                    List<String> courseIds = new ArrayList<>(enrollmentMap.keySet());

                    for (String courseId : courseIds) {
                        firestore.collection("courses")
                                .document(courseId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        QueryDocumentSnapshot enrollmentDoc = enrollmentMap.get(courseId);

                                        EnrolledCourse enrolledCourse = new EnrolledCourse();
                                        enrolledCourse.setCourseId(courseId);
                                        enrolledCourse.setTitle(documentSnapshot.getString("title"));
                                        enrolledCourse.setInstructorName(documentSnapshot.getString("instructorName"));
                                        enrolledCourse.setThumbnailUrl(documentSnapshot.getString("thumbnailUrl"));

                                        Long duration = documentSnapshot.getLong("durationHours");
                                        enrolledCourse.setDurationHours(duration != null ? duration.intValue() : 0);

                                        double progress = enrollmentDoc.getDouble("progress") != null
                                                ? enrollmentDoc.getDouble("progress") : 0.0;
                                        enrolledCourse.setProgress((float) progress);

                                        if (progress < 100 && enrolledCourse.getDurationHours() > 0) {
                                            float remainingHours = (1 - (float) progress / 100) * enrolledCourse.getDurationHours();
                                            enrolledCourse.setRemainingHours(remainingHours);
                                        } else {
                                            enrolledCourse.setRemainingHours(0);
                                        }

                                        enrolledCoursesList.add(enrolledCourse);

                                        if (enrolledCoursesList.size() == courseIds.size()) {
                                            showCourses();
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    if (enrolledCoursesList.isEmpty()) showEmptyState();
                                    else showCourses();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    showEmptyState();
                    showLoading(false);
                });
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
        if (!isLoading) rvEnrolledCourses.setVisibility(View.VISIBLE);
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
    public void onTabUnselected(TabLayout.Tab tab) { }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        loadEnrolledCourses();
    }

    private void uploadDummyEnrollment(String userId) {
        // Dummy course
        Map<String, Object> dummyCourse = new HashMap<>();
        dummyCourse.put("title", "Kotlin for Android Beginners");
        dummyCourse.put("instructorName", "Jane Doe");
        dummyCourse.put("thumbnailUrl", "https://via.placeholder.com/300x200.png?text=Kotlin+Course");
        dummyCourse.put("durationHours", 8L);
        dummyCourse.put("isPopular", true);

        String courseId = "dummy_kotlin_course";

        firestore.collection("courses").document(courseId).set(dummyCourse)
                .addOnSuccessListener(unused -> {
                    // Dummy enrollment
                    Map<String, Object> dummyEnrollment = new HashMap<>();
                    dummyEnrollment.put("userId", userId);
                    dummyEnrollment.put("courseId", courseId);
                    dummyEnrollment.put("progress", 30);
                    dummyEnrollment.put("isCompleted", false);

                    firestore.collection("enrollments").add(dummyEnrollment)
                            .addOnSuccessListener(doc -> {
                                Toast.makeText(getContext(), "enrollment added", Toast.LENGTH_SHORT).show();
                            });
                });
    }
}
