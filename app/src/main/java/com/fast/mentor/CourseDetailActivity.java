package com.fast.mentor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CourseDetailActivity extends AppCompatActivity {
    private ProgressBar courseProgressBar;
    private String currentCourseId;

    private FirebaseFirestore db;
    private String courseId;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);
        currentCourseId = getIntent().getStringExtra("COURSE_ID");
        courseProgressBar = findViewById(R.id.course_progress_bar);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        courseId = getIntent().getStringExtra("courseId");

        initializeViews();
        loadCourseData();
    }

    private void initializeViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        MaterialButton btnEnroll = findViewById(R.id.btnEnroll);
        btnEnroll.setOnClickListener(v -> showEnrollmentDialog());
    }

    private void loadCourseData() {
        db.collection("courses").document(courseId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Course course = documentSnapshot.toObject(Course.class);
                        updateUI(course);
                    }
                });
    }

    private void updateUI(Course course) {
        ((TextView) findViewById(R.id.tvCourseTitle)).setText(course.getTitle());
        ((TextView) findViewById(R.id.tvProvider)).setText(String.format("Offered By\n%s", course.getProvider()));
        ((TextView) findViewById(R.id.tvCourseDescription)).setText(course.getDescription());

        // Add course details
        LinearLayout detailsLayout = findViewById(R.id.layoutCourseDetails);
        addDetailItem(detailsLayout, R.drawable.ic_online, course.isOnline() ? "100% online" : "In-person");
        addDetailItem(detailsLayout, R.drawable.ic_deadline, "Flexible deadlines");
        addDetailItem(detailsLayout, R.drawable.ic_level, course.getLevel());
        addDetailItem(detailsLayout, R.drawable.ic_time, course.getDuration());
        addDetailItem(detailsLayout, R.drawable.ic_language, "English\nSubtitles: English");

        // Setup skills RecyclerView
        RecyclerView skillsRecycler = findViewById(R.id.rvSkills);
        skillsRecycler.setAdapter(new SkillsAdapter(course.getSkills()));
    }

    private void addDetailItem(LinearLayout layout, int iconRes, String text) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_course_detail, layout, false);

        ((ImageView) view.findViewById(R.id.ivIcon)).setImageResource(iconRes);
        ((TextView) view.findViewById(R.id.tvText)).setText(text);

        layout.addView(view);
    }

    private void showEnrollmentDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Enroll in Course")
                .setMessage("Are you sure you want to enroll in this course?")
                .setPositiveButton("Enroll", (dialog, which) -> enrollInCourse())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void enrollInCourse() {
        Map<String, Object> enrollment = new HashMap<>();
        enrollment.put("courseId", courseId);
        enrollment.put("enrollmentDate", new Date());

        db.collection("users").document(userId)
                .collection("enrollments").document(courseId)
                .set(enrollment)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Enrollment successful!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Enrollment failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProgressUpdate(ProgressUpdateEvent event) {
        if (event.getCourseId().equals(currentCourseId)) {
            // Update course-specific progress
            courseProgressBar.setProgress(event.getProgressPercentage());
            refreshModuleCompletion(event.getModuleId());
        }
    }

    private void refreshModuleCompletion(String moduleId) {
        // Update UI for completed module
        CourseHomeFragment homeFragment = (CourseHomeFragment) getSupportFragmentManager()
                .findFragmentByTag("HomeFragment");
        if (homeFragment != null) {
            homeFragment.markModuleComplete(moduleId);
        }
    }
}
}