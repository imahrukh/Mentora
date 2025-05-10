package com.fast.mentor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fast.mentor.R;
import com.fast.mentor.course.CourseService;
import com.fast.mentor.model.Lesson;
import com.fast.mentor.model.Resource;

import java.util.List;

/**
 * Base activity for displaying lesson content.
 * This activity handles common functionality for all lesson types.
 */
public class LessonContentActivity extends AppCompatActivity {

    public static final String EXTRA_LESSON_ID = "extra_lesson_id";
    public static final String EXTRA_MODULE_ID = "extra_module_id";
    public static final String EXTRA_COURSE_ID = "extra_course_id";
    public static final String EXTRA_IS_LAST_LESSON = "extra_is_last_lesson";
    public static final String EXTRA_IS_FIRST_LESSON = "extra_is_first_lesson";
    public static final String EXTRA_PREVIOUS_LESSON_ID = "extra_previous_lesson_id";
    public static final String EXTRA_NEXT_LESSON_ID = "extra_next_lesson_id";

    // UI components
    protected Toolbar toolbar;
    protected FrameLayout contentContainer;
    protected ProgressBar loadingProgressBar;
    protected LinearLayout errorView;
    protected TextView errorTextView;
    protected Button retryButton;
    protected Button previousButton;
    protected Button actionButton;
    protected Button nextButton;

    // Data
    protected String lessonId;
    protected String moduleId;
    protected String courseId;
    protected boolean isFirstLesson;
    protected boolean isLastLesson;
    protected String previousLessonId;
    protected String nextLessonId;
    protected Lesson lesson;
    protected List<Resource> resources;

    // Services
    protected CourseService courseService;
    protected LessonProgressManager progressManager;

    /**
     * Create intent to launch this activity
     */
    public static Intent createIntent(Context context, String lessonId, String moduleId, 
                                      String courseId, boolean isFirstLesson, boolean isLastLesson,
                                      String previousLessonId, String nextLessonId) {
        Intent intent = new Intent(context, LessonContentActivity.class);
        intent.putExtra(EXTRA_LESSON_ID, lessonId);
        intent.putExtra(EXTRA_MODULE_ID, moduleId);
        intent.putExtra(EXTRA_COURSE_ID, courseId);
        intent.putExtra(EXTRA_IS_FIRST_LESSON, isFirstLesson);
        intent.putExtra(EXTRA_IS_LAST_LESSON, isLastLesson);
        intent.putExtra(EXTRA_PREVIOUS_LESSON_ID, previousLessonId);
        intent.putExtra(EXTRA_NEXT_LESSON_ID, nextLessonId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_content);
        
        // Initialize services
        courseService = new CourseService();
        progressManager = new LessonProgressManager();
        
        // Get intent data
        lessonId = getIntent().getStringExtra(EXTRA_LESSON_ID);
        moduleId = getIntent().getStringExtra(EXTRA_MODULE_ID);
        courseId = getIntent().getStringExtra(EXTRA_COURSE_ID);
        isFirstLesson = getIntent().getBooleanExtra(EXTRA_IS_FIRST_LESSON, false);
        isLastLesson = getIntent().getBooleanExtra(EXTRA_IS_LAST_LESSON, false);
        previousLessonId = getIntent().getStringExtra(EXTRA_PREVIOUS_LESSON_ID);
        nextLessonId = getIntent().getStringExtra(EXTRA_NEXT_LESSON_ID);
        
        // Initialize UI components
        initializeViews();
        setupNavigation();
        
        // Load lesson content
        loadLesson();
    }

    /**
     * Initialize views and setup click listeners
     */
    protected void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        contentContainer = findViewById(R.id.contentContainer);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        errorView = findViewById(R.id.errorView);
        errorTextView = findViewById(R.id.errorTextView);
        retryButton = findViewById(R.id.retryButton);
        previousButton = findViewById(R.id.previousButton);
        actionButton = findViewById(R.id.actionButton);
        nextButton = findViewById(R.id.nextButton);
        
        // Setup toolbar
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        
        // Setup retry button
        retryButton.setOnClickListener(v -> loadLesson());
    }

    /**
     * Setup navigation buttons (previous, complete/continue, next)
     */
    protected void setupNavigation() {
        // Previous button
        previousButton.setEnabled(!isFirstLesson);
        previousButton.setAlpha(isFirstLesson ? 0.5f : 1f);
        previousButton.setOnClickListener(v -> navigateToPreviousLesson());
        
        // Next button
        nextButton.setEnabled(!isLastLesson);
        nextButton.setAlpha(isLastLesson ? 0.5f : 1f);
        nextButton.setOnClickListener(v -> navigateToNextLesson());
        
        // Action button (complete/continue)
        actionButton.setOnClickListener(v -> onActionButtonClicked());
    }

    /**
     * Load lesson data from Firestore
     */
    protected void loadLesson() {
        showLoading();
        
        courseService.getLesson(lessonId, lesson -> {
            this.lesson = lesson;
            setTitle(lesson.getTitle());
            
            // Load resources for this lesson
            loadResources();
            
            // Initialize the appropriate content view based on lesson type
            initializeLessonContent();
            
            hideLoading();
        }, e -> {
            showError("Failed to load lesson: " + e.getMessage());
        });
    }

    /**
     * Load resources for this lesson
     */
    protected void loadResources() {
        courseService.getLessonResources(lessonId, resourceList -> {
            this.resources = resourceList;
            updateResourcesView();
        }, e -> {
            // Handle silently - resources are optional
            this.resources = null;
            updateResourcesView();
        });
    }

    /**
     * Initialize content view based on lesson type
     * This should be overridden by subclasses
     */
    protected void initializeLessonContent() {
        // To be implemented in subclasses
        // Here we determine the lesson type and inflate the appropriate layout
        
        switch (lesson.getType()) {
            case Lesson.TYPE_VIDEO:
                launchVideoLesson();
                break;
                
            case Lesson.TYPE_DOCUMENT:
                launchDocumentLesson();
                break;
                
            case Lesson.TYPE_QUIZ:
                launchQuizLesson();
                break;
                
            case Lesson.TYPE_ASSIGNMENT:
                launchAssignmentLesson();
                break;
                
            default:
                showError("Unknown lesson type: " + lesson.getType());
                break;
        }
    }

    /**
     * Launch appropriate lesson activity based on type
     */
    private void launchVideoLesson() {
        Intent intent = VideoLessonActivity.createIntent(this, lessonId, moduleId, courseId, 
                isFirstLesson, isLastLesson, previousLessonId, nextLessonId);
        startActivity(intent);
        finish(); // Close this activity
    }
    
    private void launchDocumentLesson() {
        Intent intent = DocumentLessonActivity.createIntent(this, lessonId, moduleId, courseId, 
                isFirstLesson, isLastLesson, previousLessonId, nextLessonId);
        startActivity(intent);
        finish(); // Close this activity
    }
    
    private void launchQuizLesson() {
        Intent intent = QuizLessonActivity.createIntent(this, lessonId, moduleId, courseId, 
                isFirstLesson, isLastLesson, previousLessonId, nextLessonId);
        startActivity(intent);
        finish(); // Close this activity
    }
    
    private void launchAssignmentLesson() {
        Intent intent = AssignmentLessonActivity.createIntent(this, lessonId, moduleId, courseId, 
                isFirstLesson, isLastLesson, previousLessonId, nextLessonId);
        startActivity(intent);
        finish(); // Close this activity
    }

    /**
     * Update resources view - to be implemented by subclasses
     */
    protected void updateResourcesView() {
        // Implemented in subclasses
    }

    /**
     * Handle action button click (complete/continue)
     */
    protected void onActionButtonClicked() {
        // Default implementation marks lesson as complete
        markLessonComplete();
    }

    /**
     * Mark lesson as complete and update progress
     */
    protected void markLessonComplete() {
        // Get current user ID
        String userId = getCurrentUserId();
        
        // Update progress in Firestore
        progressManager.markLessonComplete(userId, lessonId, courseId, 
            () -> {
                // Success - navigate to next lesson or back to course
                Toast.makeText(this, R.string.lesson_completed, Toast.LENGTH_SHORT).show();
                
                if (!isLastLesson) {
                    navigateToNextLesson();
                } else {
                    // Last lesson, go back to course
                    finish();
                }
            },
            error -> {
                // Failed to update progress
                Toast.makeText(this, getString(R.string.error_updating_progress, error.getMessage()), 
                        Toast.LENGTH_SHORT).show();
            }
        );
    }

    /**
     * Navigate to the previous lesson
     */
    protected void navigateToPreviousLesson() {
        if (!isFirstLesson && previousLessonId != null) {
            // Create intent for previous lesson
            Intent intent = createIntent(this, previousLessonId, moduleId, courseId, 
                    false, false, null, lessonId);
            startActivity(intent);
            finish();
        }
    }

    /**
     * Navigate to the next lesson
     */
    protected void navigateToNextLesson() {
        if (!isLastLesson && nextLessonId != null) {
            // Create intent for next lesson
            Intent intent = createIntent(this, nextLessonId, moduleId, courseId, 
                    false, false, lessonId, null);
            startActivity(intent);
            finish();
        }
    }

    /**
     * Get current user ID from auth
     */
    protected String getCurrentUserId() {
        // In a real implementation, this would get the ID from Firebase Auth
        // For now, we'll return a placeholder ID
        return "current_user_id"; // TODO: Implement actual user authentication
    }

    /**
     * Show loading state
     */
    protected void showLoading() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        contentContainer.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
    }

    /**
     * Hide loading state
     */
    protected void hideLoading() {
        loadingProgressBar.setVisibility(View.GONE);
        contentContainer.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
    }

    /**
     * Show error state
     */
    protected void showError(String errorMessage) {
        loadingProgressBar.setVisibility(View.GONE);
        contentContainer.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
        errorTextView.setText(errorMessage);
    }
}