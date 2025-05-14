package com.fast.mentor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fast.mentor.R;
import com.fast.mentor.CourseService;
import com.fast.mentor.Lesson;
import com.google.firebase.auth.FirebaseAuth;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Activity for video lessons using PierfrancescoSoffritti's YouTube player.
 */
public class VideoLessonActivity extends AppCompatActivity {

    private static final String TAG = "VideoLessonActivity";
    private static final float COMPLETION_THRESHOLD = 0.9f; // 90% progress is considered complete

    // Views
    private YouTubePlayerView youTubePlayerView;
    private TextView titleTextView;
    private TextView descriptionTextView;
    private Button continueButton;
    private ProgressBar loadingProgressBar;
    private View contentLayout;
    private View errorLayout;
    private Button retryButton;

    // Data
    private String lessonId;
    private Lesson lesson;
    private String userId;
    private CourseService courseService;
    private boolean lessonCompleted = false;
    private boolean videoEnded = false;
    private String videoId;
    private float videoDuration = 0;
    private float lastPosition = 0;
    private boolean isTracking = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_lesson);

        // Get lesson ID from intent
        lessonId = getIntent().getStringExtra("LESSON_ID");
        lesson = getIntent().getParcelableExtra("LESSON");

        if (lessonId == null && lesson != null) {
            lessonId = lesson.getId();
        }

        if (lessonId == null) {
            showError("Lesson ID not provided");
            return;
        }

        // Get current user ID
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize services
        courseService = new CourseService();

        // Initialize views
        initViews();

        // Load lesson data if not provided
        if (lesson == null) {
            loadLessonData();
        } else {
            setupLesson();
        }
    }

    @Override
    protected void onDestroy() {
        if (youTubePlayerView != null) {
            youTubePlayerView.release();
        }

        // If tracking is on and video wasn't completed, save last position
        if (isTracking && !videoEnded && !lessonCompleted && lastPosition > 0) {
            saveProgress();
        }

        super.onDestroy();
    }

    /**
     * Initialize views
     */
    private void initViews() {
        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Content views
        youTubePlayerView = findViewById(R.id.youtubePlayerView);
        titleTextView = findViewById(R.id.titleTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        continueButton = findViewById(R.id.continueButton);

        // Loading and error views
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        contentLayout = findViewById(R.id.contentLayout);
        errorLayout = findViewById(R.id.errorLayout);
        retryButton = findViewById(R.id.retryButton);

        // Set up lifecycle observer
        getLifecycle().addObserver(youTubePlayerView);


        // Set up click listeners
        continueButton.setOnClickListener(v -> navigateToNextLesson());
        retryButton.setOnClickListener(v -> loadLessonData());

        // Initially show loading state
        showLoading();
    }

    /**
     * Load lesson data from Firestore
     */
    private void loadLessonData() {
        showLoading();

        // Get lesson details from Firestore
        // We use a more generic approach since the CourseService has various methods
        // that could be used based on the design of getLesson
        try {
            // This is a placeholder. The actual implementation depends on your CourseService
            // courseService.getLesson(lessonId, ...) or similar method
            // For now, we'll use the lesson from intent
            if (lesson != null) {
                setupLesson();
            } else {
                showError("Failed to load lesson data");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading lesson", e);
            showError("Error loading lesson: " + e.getMessage());
        }
    }

    /**
     * Set up lesson data
     */
    private void setupLesson() {
        if (lesson == null) {
            showError("Lesson data not available");
            return;
        }

        // Set title and description
        setTitle(lesson.getTitle());
        titleTextView.setText(lesson.getTitle());
        descriptionTextView.setText(lesson.getDescription());

        // Extract video ID from URL
        String videoUrl = lesson.getVideoUrl();
        if (videoUrl != null && !videoUrl.isEmpty()) {
            videoId = extractYouTubeId(videoUrl);
            if (videoId != null) {
                initializeYouTubePlayer();
            } else {
                showError("Invalid YouTube URL: " + videoUrl);
            }
        } else {
            showError("No video URL provided");
        }

        // Show content
        hideLoading();
    }

    /**
     * Initialize YouTube player
     */
    private void initializeYouTubePlayer() {
        youTubePlayerView.getYouTubePlayerWhenReady(new YouTubePlayerCallback() {
            @Override
            public void onYouTubePlayer(@NonNull YouTubePlayer youTubePlayer) {
                // Add listener to track video progress
                youTubePlayer.addListener(new AbstractYouTubePlayerListener() {
                    @Override
                    public void onReady(@NonNull YouTubePlayer player) {
                        // Load the video but don't play it automatically
                        player.cueVideo(videoId, 0);
                    }

                    @Override
                    public void onStateChange(@NonNull YouTubePlayer player,
                                              @NonNull PlayerConstants.PlayerState state) {
                        if (state == PlayerConstants.PlayerState.ENDED) {
                            videoEnded = true;
                            markLessonComplete();
                        }
                    }

                    @Override
                    public void onVideoDuration(@NonNull YouTubePlayer player, float duration) {
                        videoDuration = duration;
                    }

                    @Override
                    public void onCurrentSecond(@NonNull YouTubePlayer player, float second) {
                        lastPosition = second;

                        // Mark lesson as complete when reaching 90% of the video
                        if (isTracking && videoDuration > 0 &&
                                (second / videoDuration) >= COMPLETION_THRESHOLD && !lessonCompleted) {
                            markLessonComplete();
                        }
                    }

                    @Override
                    public void onError(@NonNull YouTubePlayer player,
                                        @NonNull PlayerConstants.PlayerError error) {
                        Log.e(TAG, "YouTube player error: " + error.name());
                    }
                });
            }
        });
    }

    /**
     * Mark lesson as complete
     */
    private void markLessonComplete() {
        if (lessonCompleted) return;

        lessonCompleted = true;
        isTracking = false;

        // Update progress in Firestore
        courseService.updateLessonProgress(userId, lessonId, 100,
                aVoid -> {
                    Log.d(TAG, "Lesson marked as complete");
                    // Show continue button
                    continueButton.setVisibility(View.VISIBLE);
                }, e -> {
                    Log.e(TAG, "Error marking lesson as complete", e);
                });
    }

    /**
     * Save current progress without marking as complete
     */
    private void saveProgress() {
        if (videoDuration <= 0) return;

        int progressPercentage = (int) ((lastPosition / videoDuration) * 100);

        // Save progress to Firestore
        courseService.updateLessonProgress(userId, lessonId, progressPercentage,
                aVoid -> {
                    Log.d(TAG, "Progress saved: " + progressPercentage + "%");
                }, e -> {
                    Log.e(TAG, "Error saving progress", e);
                });
    }

    /**
     * Navigate to the next lesson
     */
    private void navigateToNextLesson() {
        // This would navigate to the next lesson based on course structure
        // For simplicity, we'll just finish this activity
        finish();
    }

    /**
     * Extract YouTube video ID from URL
     */
    private String extractYouTubeId(String youtubeUrl) {
        if (youtubeUrl == null || youtubeUrl.trim().isEmpty()) {
            return null;
        }

        String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(youtubeUrl);

        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    /**
     * Show loading state
     */
    private void showLoading() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
    }

    /**
     * Hide loading state
     */
    private void hideLoading() {
        loadingProgressBar.setVisibility(View.GONE);
        contentLayout.setVisibility(View.VISIBLE);
        errorLayout.setVisibility(View.GONE);
    }

    /**
     * Show error state
     */
    private void showError(String errorMessage) {
        loadingProgressBar.setVisibility(View.GONE);
        contentLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);

        // Set error message
        TextView errorTextView = findViewById(R.id.errorTextView);
        if (errorTextView != null) {
            errorTextView.setText(errorMessage);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}