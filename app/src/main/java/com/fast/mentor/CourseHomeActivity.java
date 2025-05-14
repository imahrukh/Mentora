package com.fast.mentor;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.fast.mentor.R;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Activity for displaying course details and content
 */
public class CourseHomeActivity extends AppCompatActivity {

    private String courseId;
    private String courseTitle;
    
    private Toolbar toolbar;
    private TextView tvCourseTitle;
    private TabLayout tabLayout;
    
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_home);
        
        // Get course ID from intent
        courseId = getIntent().getStringExtra("courseId");
        courseTitle = getIntent().getStringExtra("courseTitle");
        
        if (courseId == null) {
            finish();
            return;
        }
        
        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        
        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        tvCourseTitle = findViewById(R.id.tvCourseTitle);
        tabLayout = findViewById(R.id.tabLayout);
        
        // Setup toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        
        // Set course title
        if (courseTitle != null && !courseTitle.isEmpty()) {
            tvCourseTitle.setText(courseTitle);
        } else {
            loadCourseTitle();
        }
        
        // Setup tabs
        setupTabs();
        
        // Initially load Content fragment
        loadFragment(CourseContentFragment.newInstance(courseId));
    }
    
    private void loadCourseTitle() {
        firestore.collection("courses")
                .document(courseId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        courseTitle = documentSnapshot.getString("title");
                        if (courseTitle != null && !courseTitle.isEmpty()) {
                            tvCourseTitle.setText(courseTitle);
                        }
                    }
                });
    }
    
    private void setupTabs() {
        // Add tabs
        tabLayout.addTab(tabLayout.newTab().setText(R.string.content));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.info));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.grades));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.recommendations));
        
        // Set tab listener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        // Content tab
                        loadFragment(CourseContentFragment.newInstance(courseId));
                        break;
                    case 1:
                        // Info tab
                        loadFragment(CourseInfoFragment.newInstance(courseId));
                        break;
                    case 2:
                        // Grades tab
                        loadFragment(CourseGradesFragment.newInstance(courseId));
                        break;
                    case 3:
                        // Discussions tab
                        loadFragment(CourseRecommendationsFragment.newInstance(courseId));
                        break;
                }
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not needed
            }
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Not needed
            }
        });
    }
    
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}