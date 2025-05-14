package com.fast.mentor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    private static final String TAG = "ExploreFragment";

    private TextView tvWelcome;
    private RecyclerView rvPopularCourses;
    private CardView cardSearch;
    private TextView tvViewAllCourses;
    private TextView tvViewAllProjects;

    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    private CourseAdapter courseAdapter;
    private List<Course> popularCoursesList;

    public ExploreFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Firebase references early to catch any initialization issues
        try {
            firestore = FirebaseFirestore.getInstance();
            currentUser = FirebaseAuth.getInstance().getCurrentUser();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase components", e);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Just inflate the layout here, don't do any other initialization
        return inflater.inflate(R.layout.fragment_explore, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            // Initialize views safely with null checks on the activity
            if (getActivity() == null) {
                Log.e(TAG, "Activity is null in onViewCreated");
                return;
            }

            // Initialize views
            initializeViews(view);

            // Set up click listeners
            setupClickListeners();

            // Setup RecyclerView for popular courses
            setupPopularCourses();

            // Load data from Firestore or dummy data
            loadPopularCourses();

            // For debugging and development purposes
            uploadDummyCoursesToFirestore();

        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated", e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error loading explore content", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initializeViews(View view) {
        tvWelcome = view.findViewById(R.id.tvWelcome);
        rvPopularCourses = view.findViewById(R.id.rvPopularCourses);
        cardSearch = view.findViewById(R.id.cardSearch);
        tvViewAllCourses = view.findViewById(R.id.tvViewAllCourses);
        tvViewAllProjects = view.findViewById(R.id.tvViewAllProjects);

        // Personalize welcome message if user is logged in
        if (currentUser != null && currentUser.getDisplayName() != null) {
            String welcomeMessage = getString(R.string.welcome_to_mentora);
            try {
                welcomeMessage += ", " + currentUser.getDisplayName().split(" ")[0];
            } catch (Exception e) {
                Log.e(TAG, "Error personalizing welcome message", e);
            }
            tvWelcome.setText(welcomeMessage);
        }
    }

    private void setupClickListeners() {
        // Setup search card click listener
        if (cardSearch != null) {
            cardSearch.setOnClickListener(v -> {
                try {
                    // Navigate to search activity or show search dialog
                    navigateToSearch();
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to search", e);
                    showToast("Unable to open search");
                }
            });
        }

        // Setup view all buttons
        if (tvViewAllCourses != null) {
            tvViewAllCourses.setOnClickListener(v -> {
                try {
                    // Navigate to all courses screen
                    navigateToAllCourses();
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to all courses", e);
                    showToast("Unable to view all courses");
                }
            });
        }

        if (tvViewAllProjects != null) {
            tvViewAllProjects.setOnClickListener(v -> {
                try {
                    // Navigate to all projects screen
                    navigateToAllProjects();
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to all projects", e);
                    showToast("Unable to view all projects");
                }
            });
        }
    }

    private void setupPopularCourses() {
        if (getContext() == null || rvPopularCourses == null) {
            Log.e(TAG, "Context or RecyclerView is null in setupPopularCourses");
            return;
        }

        try {
            popularCoursesList = new ArrayList<>();

            // Make sure the RecyclerView has a LayoutManager
            rvPopularCourses.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

            // Create and set the adapter
            courseAdapter = new CourseAdapter(popularCoursesList, course -> {
                if (course != null) {
                    navigateToCourseDetail(course);
                }
            });

            rvPopularCourses.setAdapter(courseAdapter);

            Log.d(TAG, "RecyclerView and adapter setup complete");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView", e);
        }
    }

    private void loadPopularCourses() {
        if (firestore == null) {
            Log.e(TAG, "Firestore is null, loading dummy data");
            loadDummyData();
            return;
        }

        Log.d(TAG, "Loading popular courses from Firestore...");

        try {
            firestore.collection("courses")
                    .whereEqualTo("popular", true)
                    .limit(5)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                            Log.d(TAG, "No popular courses found in Firestore, loading dummy data");
                            loadDummyData();
                            return;
                        }

                        try {
                            popularCoursesList.clear();
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                Course course = document.toObject(Course.class);
                                course.setCourseId(document.getId());
                                popularCoursesList.add(course);
                                Log.d(TAG, "Added course: " + course.getTitle());
                            }

                            if (courseAdapter != null) {
                                courseAdapter.notifyDataSetChanged();
                                Log.d(TAG, "Successfully loaded " + popularCoursesList.size() + " courses");
                            } else {
                                Log.e(TAG, "CourseAdapter is null, cannot update UI");
                                loadDummyData();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing Firestore documents", e);
                            loadDummyData();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to load courses from Firestore: ", e);
                        loadDummyData();
                    });
        } catch (Exception e) {
            Log.e(TAG, "Exception querying Firestore", e);
            loadDummyData();
        }
    }

    private void loadDummyData() {
        Log.d(TAG, "Loading dummy course data");

        try {
            // Clear existing list if any
            if (popularCoursesList == null) {
                popularCoursesList = new ArrayList<>();
            } else {
                popularCoursesList.clear();
            }

            // Create some dummy courses
            Course course1 = new Course();
            course1.setCourseId("1");
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
            course2.setCourseId("2");
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
            course3.setCourseId("3");
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

            // Notify adapter of changes if it exists
            if (courseAdapter != null) {
                courseAdapter.notifyDataSetChanged();
                Log.d(TAG, "Loaded " + popularCoursesList.size() + " dummy courses");
            } else {
                Log.e(TAG, "CourseAdapter is null in loadDummyData");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading dummy data", e);
        }
    }

    private void navigateToSearch() {
        try {
            // Check if we're attached to an activity
            if (getActivity() == null) {
                Log.e(TAG, "Activity is null when trying to navigate to search");
                return;
            }

            // Intent to SearchActivity - use the correct class name here
            // Make sure SearchActivity exists in your project
            Intent intent = new Intent(getActivity(), SearchFragment.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to search", e);
        }
    }

    private void navigateToAllCourses() {
        try {
            if (getActivity() == null) return;

            // Assuming AllCoursesActivity exists in your project
            // If it doesn't, replace with the correct activity class
            Toast.makeText(getActivity(), "All courses view not implemented yet", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to all courses", e);
            // As a fallback, you could try using the current fragment class if you have a specific screen
            showToast("Courses view not implemented yet");
        }
    }

    private void navigateToAllProjects() {
        try {
            if (getActivity() == null) return;

            // Assuming AllProjectsActivity exists in your project
            // If it doesn't, replace with the correct activity class
            Toast.makeText(getActivity(), "All projects view not implemented yet", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to all projects", e);
            showToast("Projects view not implemented yet");
        }
    }

    private void navigateToCourseDetail(Course course) {
        try {
            if (getActivity() == null) return;
            if (course == null || course.getCourseId() == null) {
                Log.e(TAG, "Course or courseId is null in navigateToCourseDetail");
                return;
            }

            // Assuming CourseDetailActivity exists in your project
            Intent intent = new Intent(getActivity(), CourseDetailActivity.class);
            intent.putExtra("courseId", course.getCourseId());
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to course detail", e);
        }
    }

    private void uploadDummyCoursesToFirestore() {
        // Only proceed if Firestore is initialized
        if (firestore == null) {
            Log.e(TAG, "Firestore is null in uploadDummyCoursesToFirestore");
            return;
        }

        Log.d(TAG, "Uploading dummy courses to Firestore");

        try {
            List<Course> dummyCourses = new ArrayList<>();

            Course course1 = new Course("1", "Android App Development for Beginners", "Dr. Sarah Johnson",
                    "Google Developer Expert", 4.7f, 1234, "Beginner", 10, 45,
                    0f, true, "https://example.com/android_course.jpg");

            Course course2 = new Course("2", "Java Programming Masterclass", "Prof. Michael Chen",
                    "Stanford University", 4.9f, 3421, "Intermediate", 15, 72,
                    49.99f, true, "https://example.com/java_course.jpg");

            Course course3 = new Course("3", "Mobile UI/UX Design Principles", "Alex Rodriguez",
                    "Design Academy", 4.5f, 892, "All Levels", 8, 38,
                    29.99f, true, "https://example.com/design_course.jpg");

            dummyCourses.add(course1);
            dummyCourses.add(course2);
            dummyCourses.add(course3);

            for (Course course : dummyCourses) {
                firestore.collection("courses").document(course.getCourseId())
                        .set(course)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Course uploaded: " + course.getTitle()))
                        .addOnFailureListener(e -> Log.e(TAG, "Error uploading course: " + course.getTitle(), e));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in uploadDummyCoursesToFirestore", e);
        }
    }

    private void showToast(String message) {
        try {
            if (getContext() != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing toast", e);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up any resources if needed
        popularCoursesList = null;
        courseAdapter = null;
    }
}