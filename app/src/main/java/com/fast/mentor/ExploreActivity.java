package com.fast.mentor;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class ExploreActivity extends AppCompatActivity {

    private LinearLayout topicLayout;
    private RecyclerView courseRecyclerView, projectRecyclerView;
    private List<Course> courseList;
    private List<Project> projectList;
    private CoursesAdapter coursesAdapter;
    private ProjectsAdapter projectsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        topicLayout = findViewById(R.id.topicLayout);
        courseRecyclerView = findViewById(R.id.courseRecycler);
        projectRecyclerView = findViewById(R.id.projectRecycler);

        // 1. Display Topics
        displayTopics();

        // 2. Setup Courses RecyclerView
        courseList = new ArrayList<>();
        courseList.add(new Course(R.drawable.course1, "Intro to AI", "Coursera"));
        courseList.add(new Course(R.drawable.course1, "Data Science Basics", "edX"));
        courseList.add(new Course(R.drawable.course1, "Machine Learning", "Udacity"));
        coursesAdapter = new CoursesAdapter(this,courseList);
        courseRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        courseRecyclerView.setAdapter(coursesAdapter);

        // 3. Setup Projects RecyclerView
        projectList = new ArrayList<>();
        projectList.add(new Project(R.drawable.project1, "Stock Predictor", "FAST-NUCES", "Guided Project"));
        projectList.add(new Project(R.drawable.project1, "AI Chatbot", "GLI", "Guided Project"));
        projectList.add(new Project(R.drawable.project1, "Resume Parser", "Mentora", "Guided Project"));
        projectsAdapter = new ProjectsAdapter(this, projectList);
        projectRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        projectRecyclerView.setAdapter(projectsAdapter);

        // 4. Bottom Navigation Handling
        BottomNavigationView navBar = findViewById(R.id.bottomNav);
        navBar.setSelectedItemId(R.id.explore);

        navBar.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.explore) {
                return true; // already here
            } else if (id == R.id.profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            } else if (id == R.id.settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });
    }

    private void displayTopics() {
        String[] topics = {"AI", "Data Science", "Web Dev", "Android", "Cloud", "ML", "Cybersecurity", "Design", "IoT"};
        for (String topic : topics) {
            Button button = new Button(this);
            button.setText(topic);
            button.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            button.setBackgroundColor(Color.parseColor("#FF3E6B"));
            button.setAllCaps(false);
            button.setPadding(40, 10, 40, 10);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(10, 10, 10, 10);
            button.setLayoutParams(params);

            button.setOnClickListener(view -> {
                Toast toast = Toast.makeText(this, "Feature not available yet", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 300);
                toast.show();
            });

            topicLayout.addView(button);
        }
    }
}

