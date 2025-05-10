package com.fast.mentor;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fast.mentor.R;
import com.fast.mentor.model.Resource;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Activity for video-based lessons using ExoPlayer.
 */
public class VideoLessonActivity extends LessonContentActivity {

    private PlayerView playerView;
    private SimpleExoPlayer player;
    private ProgressBar videoLoadingProgressBar;
    private LinearLayout videoErrorView;
    private TextView durationTextView;
    private TextView playbackSpeedTextView;
    private TextView lessonTitleTextView;
    private TextView lessonDescriptionTextView;
    private RecyclerView resourcesRecyclerView;
    private TextView emptyResourcesTextView;
    
    private ResourceAdapter resourceAdapter;
    private long playbackPosition = 0;
    private boolean playWhenReady = true;
    private boolean isPlayerInitialized = false;
    
    // Video completion tracking
    private boolean isLessonCompleted = false;
    private long totalDuration = 0;
    private final long AUTO_COMPLETE_THRESHOLD_MS = 10000; // 10 seconds before end

    /**
     * Create intent to launch this activity
     */
    public static Intent createIntent(Context context, String lessonId, String moduleId, 
                                     String courseId, boolean isFirstLesson, boolean isLastLesson,
                                     String previousLessonId, String nextLessonId) {
        Intent intent = new Intent(context, VideoLessonActivity.class);
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
        
        // Inflate video content layout into content container
        LayoutInflater.from(this).inflate(R.layout.content_video_lesson, contentContainer, true);
        
        // Initialize video-specific views
        initializeVideoViews();
        
        // Get any saved state
        if (savedInstanceState != null) {
            playbackPosition = savedInstanceState.getLong("playback_position", 0);
            playWhenReady = savedInstanceState.getBoolean("play_when_ready", true);
            isLessonCompleted = savedInstanceState.getBoolean("is_lesson_completed", false);
        }
    }

    /**
     * Initialize video-specific views
     */
    private void initializeVideoViews() {
        playerView = findViewById(R.id.playerView);
        videoLoadingProgressBar = findViewById(R.id.videoLoadingProgressBar);
        videoErrorView = findViewById(R.id.videoErrorView);
        durationTextView = findViewById(R.id.durationTextView);
        playbackSpeedTextView = findViewById(R.id.playbackSpeedTextView);
        lessonTitleTextView = findViewById(R.id.lessonTitleTextView);
        lessonDescriptionTextView = findViewById(R.id.lessonDescriptionTextView);
        resourcesRecyclerView = findViewById(R.id.resourcesRecyclerView);
        emptyResourcesTextView = findViewById(R.id.emptyResourcesTextView);
        
        // Setup resources recycler view
        resourcesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        resourceAdapter = new ResourceAdapter(this);
        resourcesRecyclerView.setAdapter(resourceAdapter);
        
        // Setup playback speed control
        playbackSpeedTextView.setOnClickListener(v -> cyclePlaybackSpeed());
        
        // Setup retry button in error view
        findViewById(R.id.videoRetryButton).setOnClickListener(v -> {
            videoErrorView.setVisibility(View.GONE);
            initializePlayer();
        });
    }

    @Override
    protected void initializeLessonContent() {
        // Set lesson info
        lessonTitleTextView.setText(lesson.getTitle());
        lessonDescriptionTextView.setText(lesson.getDescription());
        
        // Initialize video player
        initializePlayer();
        
        // Update action button text based on completion status
        checkLessonCompletionStatus();
    }

    /**
     * Check if this lesson has already been completed
     */
    private void checkLessonCompletionStatus() {
        String userId = getCurrentUserId();
        
        courseService.getLessonProgress(userId, lessonId, progress -> {
            if (progress != null && progress.getProgress() >= 100) {
                isLessonCompleted = true;
                actionButton.setText(R.string.continue_to_next);
            } else {
                isLessonCompleted = false;
                actionButton.setText(R.string.complete_lesson);
            }
        }, e -> {
            // If we can't get progress, assume not completed
            isLessonCompleted = false;
            actionButton.setText(R.string.complete_lesson);
        });
    }

    /**
     * Initialize ExoPlayer
     */
    private void initializePlayer() {
        if (player == null) {
            // Create player instance
            player = new SimpleExoPlayer.Builder(this).build();
            playerView.setPlayer(player);
            
            // Set player listeners
            player.addListener(new Player.EventListener() {
                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    updatePlaybackState(playbackState);
                }
                
                @Override
                public void onPlayerError(ExoPlaybackException error) {
                    handlePlayerError(error);
                }
                
                @Override
                public void onPositionDiscontinuity(int reason) {
                    // Track position for auto-completion
                    if (player != null) {
                        trackProgress();
                    }
                }
            });
            
            // Auto-complete lesson when close to the end
            player.addAnalyticsListener(new AnalyticsListener() {
                @Override
                public void onPositionDiscontinuity(EventTime eventTime, int reason) {
                    trackProgress();
                }
                
                @Override
                public void onPlaybackStateChanged(EventTime eventTime, int state) {
                    updatePlaybackState(state);
                    if (state == Player.STATE_ENDED) {
                        handleVideoCompleted();
                    }
                }
            });
            
            // Prepare media source
            prepareMediaSource();
        }
    }

    /**
     * Prepare media source for ExoPlayer
     */
    private void prepareMediaSource() {
        if (lesson == null || lesson.getVideoUrl() == null) {
            handlePlayerError(new Exception("Invalid video URL"));
            return;
        }
        
        // Create media source
        Uri uri = Uri.parse(lesson.getVideoUrl());
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, getString(R.string.app_name)));
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri);
        
        // Prepare player
        player.prepare(mediaSource);
        player.seekTo(playbackPosition);
        player.setPlayWhenReady(playWhenReady);
        
        isPlayerInitialized = true;
    }

    /**
     * Update UI based on playback state
     */
    private void updatePlaybackState(int playbackState) {
        switch (playbackState) {
            case Player.STATE_BUFFERING:
                videoLoadingProgressBar.setVisibility(View.VISIBLE);
                break;
                
            case Player.STATE_READY:
                videoLoadingProgressBar.setVisibility(View.GONE);
                
                // Get total duration once available
                if (totalDuration == 0 && player.getDuration() > 0) {
                    totalDuration = player.getDuration();
                }
                
                // Update duration text
                updateDurationText();
                break;
                
            case Player.STATE_ENDED:
                handleVideoCompleted();
                break;
                
            case Player.STATE_IDLE:
                // No-op
                break;
        }
    }

    /**
     * Update duration text showing current position and total duration
     */
    private void updateDurationText() {
        if (player != null) {
            long currentPosition = player.getCurrentPosition();
            long duration = player.getDuration();
            
            String positionStr = formatDuration(currentPosition);
            String durationStr = formatDuration(duration);
            
            durationTextView.setText(String.format("%s / %s", positionStr, durationStr));
        }
    }

    /**
     * Format milliseconds duration to readable format (MM:SS)
     */
    private String formatDuration(long milliseconds) {
        return String.format(Locale.getDefault(), "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }

    /**
     * Handle player errors
     */
    private void handlePlayerError(Exception error) {
        videoLoadingProgressBar.setVisibility(View.GONE);
        videoErrorView.setVisibility(View.VISIBLE);
        
        // Log error for debugging
        error.printStackTrace();
    }

    /**
     * Handle video completion
     */
    private void handleVideoCompleted() {
        // Mark lesson as complete when video finishes
        if (!isLessonCompleted) {
            markLessonComplete();
        }
    }

    /**
     * Track progress for auto-completion
     */
    private void trackProgress() {
        if (player != null && totalDuration > 0) {
            long currentPosition = player.getCurrentPosition();
            
            // Update duration text
            updateDurationText();
            
            // Auto-complete when near the end
            long timeRemaining = totalDuration - currentPosition;
            if (!isLessonCompleted && timeRemaining <= AUTO_COMPLETE_THRESHOLD_MS) {
                handleVideoCompleted();
            }
            
            // Every 30 seconds, update server with current position
            if (currentPosition % 30000 < 1000) {
                updateProgressOnServer(currentPosition);
            }
        }
    }

    /**
     * Update progress on server periodically
     */
    private void updateProgressOnServer(long position) {
        String userId = getCurrentUserId();
        int progress = totalDuration > 0 ? (int) ((position * 100) / totalDuration) : 0;
        
        progressManager.updateLessonProgress(userId, lessonId, courseId, progress, 
            () -> {
                // Success - silent update
            },
            error -> {
                // Failed to update progress - silent failure, will try again later
            }
        );
    }

    /**
     * Cycle through playback speeds (0.5x, 1.0x, 1.5x, 2.0x)
     */
    private void cyclePlaybackSpeed() {
        if (player != null) {
            float currentSpeed = player.getPlaybackParameters().speed;
            float newSpeed;
            
            // Cycle through speeds
            if (currentSpeed < 0.75f) {
                newSpeed = 1.0f;
            } else if (currentSpeed < 1.25f) {
                newSpeed = 1.5f;
            } else if (currentSpeed < 1.75f) {
                newSpeed = 2.0f;
            } else {
                newSpeed = 0.5f;
            }
            
            // Set new speed
            player.setPlaybackParameters(new PlaybackParameters(newSpeed));
            playbackSpeedTextView.setText(String.format(Locale.getDefault(), "%.1fx", newSpeed));
            
            // Show toast
            Toast.makeText(this, getString(R.string.playback_speed_changed, newSpeed), 
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActionButtonClicked() {
        if (isLessonCompleted) {
            // Already completed, navigate to next lesson
            navigateToNextLesson();
        } else {
            // Not completed, mark as complete
            markLessonComplete();
        }
    }

    @Override
    protected void updateResourcesView() {
        if (resources != null && !resources.isEmpty()) {
            resourceAdapter.setResources(resources);
            resourcesRecyclerView.setVisibility(View.VISIBLE);
            emptyResourcesTextView.setVisibility(View.GONE);
        } else {
            resourcesRecyclerView.setVisibility(View.GONE);
            emptyResourcesTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        
        // Save player state
        if (player != null) {
            outState.putLong("playback_position", player.getCurrentPosition());
            outState.putBoolean("play_when_ready", player.getPlayWhenReady());
        }
        outState.putBoolean("is_lesson_completed", isLessonCompleted);
    }

    @Override
    protected void onPause() {
        super.onPause();
        
        // Save playback position and pause player
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            playWhenReady = player.getPlayWhenReady();
            
            // Update progress on server when pausing
            if (totalDuration > 0) {
                updateProgressOnServer(playbackPosition);
            }
            
            // Pause player
            player.setPlayWhenReady(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Resume player if initialized
        if (isPlayerInitialized && player != null) {
            player.setPlayWhenReady(playWhenReady);
        }
    }

    @Override
    protected void onDestroy() {
        // Release player resources
        if (player != null) {
            player.release();
            player = null;
        }
        
        super.onDestroy();
    }
}