package com.fast.mentor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fast.mentor.R;
import com.fast.mentor.CourseService;
import com.fast.mentor.Lesson;

/**
 * Activity that determines the appropriate lesson type activity to launch
 * based on the lesson type (video, document, quiz, assignment).
 */
public class LessonContentActivity extends AppCompatActivity {

    private static final String TAG = "LessonContentActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Temporarily set content view (this activity should redirect immediately)
        setContentView(R.layout.activity_loading);

        // Get lesson ID from intent
        String lessonId = getIntent().getStringExtra("LESSON_ID");
        Lesson lesson = getIntent().getParcelableExtra("LESSON");

        if (lessonId == null && lesson == null) {
            Toast.makeText(this, "Lesson information not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // If lesson is provided directly, redirect immediately
        if (lesson != null) {
            redirectToLessonTypeActivity(lesson);
            return;
        }

        // Otherwise, load lesson from Firestore
        CourseService courseService = new CourseService();
        courseService.getLesson(lessonId,
                loadedLesson -> {
                    // Redirect to appropriate activity
                    redirectToLessonTypeActivity(loadedLesson);
                },
                e -> {
                    // Handle error
                    Log.e(TAG, "Error loading lesson", e);
                    Toast.makeText(this,
                            "Error loading lesson: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    /**
     * Redirect to the appropriate activity based on lesson type
     */
    private void redirectToLessonTypeActivity(Lesson lesson) {
        Intent intent = null;

        // Determine lesson type
        if (lesson.isVideo()) {
            // Video lesson
            intent = new Intent(this, VideoLessonActivity.class);
        } else if (lesson.isDocument()) {
            // Document (PDF) lesson
            intent = new Intent(this, PDFViewerActivity.class);
        } else if (lesson.isQuiz()) {
            // Quiz lesson - replace with your quiz activity
            // intent = new Intent(this, QuizLessonActivity.class);
            Toast.makeText(this, "Quiz lessons not implemented yet", Toast.LENGTH_SHORT).show();
            finish();
            return;
        } else if (lesson.isAssignment()) {
            // Assignment lesson
            intent = new Intent(this, AssignmentLessonActivity.class);
        } else {
            // Unknown type
            Toast.makeText(this, "Unknown lesson type", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Pass lesson to the activity
        intent.putExtra("LESSON_ID", lesson.getId());
        intent.putExtra("LESSON", lesson);

        // Start the activity and finish this one
        startActivity(intent);
        finish();
    }
}