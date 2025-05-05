package com.fast.mentor;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ExploreActivity extends AppCompatActivity {
    private RecyclerView courseRecyclerView, projectRecyclerView;
    private CoursesAdapter coursesAdapter;
    private ProjectsAdapter projectsAdapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        db = FirebaseFirestore.getInstance();
        setupBottomNav();
        setupCoursesRecycler();
        setupProjectsRecycler();
    }

    private void setupCoursesRecycler() {
        Query query = db.collection("courses").limit(10);
        FirestoreRecyclerOptions<Course> options = new FirestoreRecyclerOptions.Builder<Course>()
                .setQuery(query, Course.class)
                .build();

        coursesAdapter = new CoursesAdapter(options);
        courseRecyclerView = findViewById(R.id.courseRecycler);
        courseRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        courseRecyclerView.setAdapter(coursesAdapter);
    }

    private void setupProjectsRecycler() {
        Query query = db.collection("projects").limit(10);
        FirestoreRecyclerOptions<Project> options = new FirestoreRecyclerOptions.Builder<Project>()
                .setQuery(query, Project.class)
                .build();

        projectsAdapter = new ProjectsAdapter(options);
        projectRecyclerView = findViewById(R.id.projectRecycler);
        projectRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        projectRecyclerView.setAdapter(projectsAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        coursesAdapter.startListening();
        projectsAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        coursesAdapter.stopListening();
        projectsAdapter.stopListening();
    }

    private void setupBottomNav() {
        BottomNavigationView navBar = findViewById(R.id.bottomNav);
        navBar.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_explore:
                    break;
                case R.id.nav_courses:
                    startActivity(new Intent(this, LearnActivity.class));
                    break;
                case R.id.nav_search:
                    startActivity(new Intent(this, SearchActivity.class));
                    break;
                case R.id.nav_profile:
                    startActivity(new Intent(this, ProfileActivity.class));
                    break;
            }
            return true;
        });
    }
}