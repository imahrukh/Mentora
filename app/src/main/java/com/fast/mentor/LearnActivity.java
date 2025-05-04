package com.fast.mentor;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class LearnActivity extends AppCompatActivity {

    RecyclerView recyclerCourses;
    DatabaseHelper dbHelper;
    List<Course> courses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);

        recyclerCourses = findViewById(R.id.recyclerCourses);
        recyclerCourses.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new DatabaseHelper(this);
        courses = dbHelper.getAllCourses();

        if (courses.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("No Courses")
                    .setMessage("You have not registered in any courses yet.")
                    .setPositiveButton("OK", null)
                    .show();
        } else {
            RegisteredCourseAdapter adapter = new RegisteredCourseAdapter(this, courses);
            recyclerCourses.setAdapter(adapter);
        }
    }

}
