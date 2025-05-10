package com.fast.mentor;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fast.mentor.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for the Explore tab
 * Shows topics, popular courses, and guided projects
 */
public class ExploreFragment extends Fragment {

    private TextView tvWelcome;
    private RecyclerView rvPopularCourses;
    private CardView cardSearch;
    private TextView tvViewAllCourses;
    private TextView tvViewAllProjects;
    
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    private CourseAdapter courseAdapter;
    private List<Course> popularCoursesList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_explore, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Initialize views
        tvWelcome = view.findViewById(R.id.tvWelcome);
        rvPopularCourses = view.findViewById(R.id.rvPopularCourses);
        cardSearch = view.findViewById(R.id.cardSearch);
        tvViewAllCourses = view.findViewById(R.id.tvViewAllCourses);
        tvViewAllProjects = view.findViewById(R.id.tvViewAllProjects);

        // Personalize welcome message if user is logged in
        if (currentUser != null && currentUser.getDisplayName() != null) {
            String welcomeMessage = getString(R.string.welcome_to_mentora) + ", " + 
                    currentUser.getDisplayName().split(" ")[0];
            tvWelcome.setText(welcomeMessage);
        }

        // Setup search card click listener
        cardSearch.setOnClickListener(v -> {
            // Navigate to search activity or show search dialog
            navigateToSearch();
        });

        // Setup view all buttons
        tvViewAllCourses.setOnClickListener(v -> {
            // Navigate to all courses screen
            navigateToAllCourses();
        });
        
        tvViewAllProjects.setOnClickListener(v -> {
            // Navigate to all projects screen
            navigateToAllProjects();
        });

        // Setup RecyclerView for popular courses
        setupPopularCourses();
    }

    private void setupPopularCourses() {
        popularCoursesList = new ArrayList<>();
        courseAdapter = new CourseAdapter(popularCoursesList, course -> {
            // Navigate to course detail screen
            navigateToCourseDetail(course);
        });

        rvPopularCourses.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPopularCourses.setAdapter(courseAdapter);

        // Load popular courses from Firestore
        loadPopularCourses();
    }

    private void loadPopularCourses() {
        firestore.collection("courses")
                .whereEqualTo("isPopular", true)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    popularCoursesList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Course course = document.toObject(Course.class);
                        course.setId(document.getId());
                        popularCoursesList.add(course);
                    }
                    courseAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Handle error
                    // For now, load dummy data for development
                    loadDummyData();
                });
    }

    private void loadDummyData() {
        // This is just for development, in production we'd handle the error differently
        popularCoursesList.clear();

        // Create some dummy courses
        Course course1 = new Course();
        course1.setId("1");
        course1.setTitle("Android App Development for Beginners");
        course1.setInstructorName("Dr. Sarah Johnson");
        course1.setInstructorAffiliation("Google Developer Expert");
        course1.setRating(4.7f);
        course1.setReviewsCount(1234);
        course1.setLevel("Beginner");
        course1.setDurationHours(10);
        course1.setLessonsCount(45);
        course1.setPrice(0); // Free
        course1.setPopular(true);
        course1.setThumbnailUrl("https://example.com/android_course.jpg");
        popularCoursesList.add(course1);

        Course course2 = new Course();
        course2.setId("2");
        course2.setTitle("Java Programming Masterclass");
        course2.setInstructorName("Prof. Michael Chen");
        course2.setInstructorAffiliation("Stanford University");
        course2.setRating(4.9f);
        course2.setReviewsCount(3421);
        course2.setLevel("Intermediate");
        course2.setDurationHours(15);
        course2.setLessonsCount(72);
        course2.setPrice(49.99f);
        course2.setPopular(true);
        course2.setThumbnailUrl("https://example.com/java_course.jpg");
        popularCoursesList.add(course2);

        Course course3 = new Course();
        course3.setId("3");
        course3.setTitle("Mobile UI/UX Design Principles");
        course3.setInstructorName("Alex Rodriguez");
        course3.setInstructorAffiliation("Design Academy");
        course3.setRating(4.5f);
        course3.setReviewsCount(892);
        course3.setLevel("All Levels");
        course3.setDurationHours(8);
        course3.setLessonsCount(38);
        course3.setPrice(29.99f);
        course3.setPopular(true);
        course3.setThumbnailUrl("https://example.com/design_course.jpg");
        popularCoursesList.add(course3);

        courseAdapter.notifyDataSetChanged();
    }

    private void navigateToSearch() {
        // Intent to SearchActivity
        // Intent intent = new Intent(getActivity(), SearchActivity.class);
        // startActivity(intent);
    }

    private void navigateToAllCourses() {
        // Intent to AllCoursesActivity
        // Intent intent = new Intent(getActivity(), AllCoursesActivity.class);
        // startActivity(intent);
    }

    private void navigateToAllProjects() {
        // Intent to AllProjectsActivity
        // Intent intent = new Intent(getActivity(), AllProjectsActivity.class);
        // startActivity(intent);
    }

    private void navigateToCourseDetail(Course course) {
        // Intent to CourseDetailActivity
        Intent intent = new Intent(getActivity(), CourseDetailActivity.class);
        intent.putExtra("courseId", course.getId());
        startActivity(intent);
    }
}