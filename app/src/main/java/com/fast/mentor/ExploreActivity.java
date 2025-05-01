package com.fast.mentor;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.graphics.Color;
import androidx.recyclerview.widget.LinearLayoutManager;

public class ExploreActivity extends AppCompatActivity {

    private LinearLayout topicLayout;
    private RecyclerView courseRecycler, projectRecycler;
    private String[] topics = {"Data Science", "Computer Science", "AI", "Web Dev", "Mobile", "Health"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        topicLayout = findViewById(R.id.topicLayout);
        courseRecycler = findViewById(R.id.courseRecycler);
        projectRecycler = findViewById(R.id.projectRecycler);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        setupTopics();
        setupCourseRecycler();
        setupProjectRecycler();

        bottomNav.setSelectedItemId(R.id.nav_explore);
        bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_courses:
                    startActivity(new Intent(this, CoursesActivity.class));
                    return true;
                case R.id.nav_search:
                    startActivity(new Intent(this, SearchActivity.class));
                    return true;
                case R.id.nav_profile:
                    startActivity(new Intent(this, ProfileActivity.class));
                    return true;
                default:
                    return true;
            }
        });
    }

    private void setupTopics() {
        for (String topic : topics) {
            Button btn = new Button(this);
            btn.setText(topic);
            btn.setBackgroundColor(Color.parseColor("#ff3e6b"));
            btn.setTextColor(Color.WHITE);
            btn.setAllCaps(false);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 8, 8, 8);
            btn.setLayoutParams(params);
            btn.setOnClickListener(v -> Toast.makeText(this, "Feature not available yet", Toast.LENGTH_SHORT).show());
            topicLayout.addView(btn);
        }
    }

    private void setupCourseRecycler() {
        courseRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        ArrayList<Course> courses = new ArrayList<>();
        courses.add(new Course("Data Analytics", "Google"));
        courses.add(new Course("R Programming", "Harvard"));
        courses.add(new Course("Advanced AI", "DeepLearning.AI"));
        courseRecycler.setAdapter(new CoursesAdapter(courses, this));
    }

    private void setupProjectRecycler() {
        projectRecycler.setLayoutManager(new LinearLayoutManager(this));
        ArrayList<Project> projects = new ArrayList<>();
        projects.add(new Project("Build app w/ Azure", "Microsoft"));
        projects.add(new Project("MS Excel", "Coursera Network"));
        projects.add(new Project("AWS S3", "Coursera Network"));
        projectRecycler.setAdapter(new ProjectsAdapter(projects, this));
    }
}
