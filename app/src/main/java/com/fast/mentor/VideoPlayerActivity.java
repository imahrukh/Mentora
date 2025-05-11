package com.fast.mentor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fast.mentor.R;
import com.fast.mentor.DatabaseHelper;
import com.fast.mentor.Content;
import com.fast.mentor.ContentTracker;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

/**
 * Activity for playing video content with automatic completion tracking
 */
public class VideoPlayerActivity extends AppCompatActivity {
    private static final String TAG = "VideoPlayerActivity";
    private static final int POSITION_UPDATE_INTERVAL_MS = 5000; // Update every 5 seconds
    
    private PlayerView playerView;
    private SimpleExoPlayer player;
    private TextView titleTextView;
    private ProgressBar loadingProgress;
    private ImageButton backButton;
    private ImageButton fullscreenButton;
    
    private int contentId;
    private int userId;
    private String videoUrl;
    private String videoTitle;
    private boolean isVideoComplete = false;
    private boolean wasUserCompleting = false; // To prevent multiple completion events
    
    private ContentTracker contentTracker;
    private DatabaseHelper dbHelper;
    private Handler handler;
    private final Runnable positionUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            updateVideoPosition();
            // Schedule the next update
            handler.postDelayed(this, POSITION_UPDATE_INTERVAL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        
        // Initialize UI components
        playerView = findViewById(R.id.video_player);
        titleTextView = findViewById(R.id.video_title);
        loadingProgress = findViewById(R.id.loading_progress);
        backButton = findViewById(R.id.back_button);
        fullscreenButton = findViewById(R.id.fullscreen_button);
        
        // Get data from intent
        Intent intent = getIntent();
        contentId = intent.getIntExtra("content_id", -1);
        userId = intent.getIntExtra("user_id", -1); // Get from login session in real app
        videoUrl = intent.getStringExtra("video_url");
        videoTitle = intent.getStringExtra("video_title");
        
        if (contentId == -1 || userId == -1 || videoUrl == null) {
            Toast.makeText(this, "Error loading video", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Set title
        titleTextView.setText(videoTitle != null ? videoTitle : "Video");
        
        // Initialize helpers
        contentTracker = ContentTracker.getInstance(this);
        dbHelper = DatabaseHelper.getInstance(this);
        handler = new Handler();
        
        // Setup player
        initializePlayer();
        
        // Setup button listeners
        backButton.setOnClickListener(v -> onBackPressed());
        fullscreenButton.setOnClickListener(v -> toggleFullscreen());
        
        // Start tracking content consumption
        contentTracker.startTracking(userId, contentId, "video");
        
        // Schedule regular position updates
        handler.postDelayed(positionUpdateRunnable, POSITION_UPDATE_INTERVAL_MS);
    }
    
    private void initializePlayer() {
        // Create player instance
        player = new SimpleExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        
        // Create media source
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "Mentora"));
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(videoUrl));
        
        // Prepare player
        player.setMediaSource(mediaSource);
        player.prepare();
        player.setPlayWhenReady(true);
        
        // Add listeners
        player.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == Player.STATE_BUFFERING) {
                    loadingProgress.setVisibility(View.VISIBLE);
                } else if (playbackState == Player.STATE_READY) {
                    loadingProgress.setVisibility(View.GONE);
                } else if (playbackState == Player.STATE_ENDED) {
                    markVideoComplete();
                }
            }
            
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Toast.makeText(VideoPlayerActivity.this, 
                        "Error playing video: " + error.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Player error: ", error);
            }
        });
    }
    
    /**
     * Update video position in tracker
     */
    private void updateVideoPosition() {
        if (player != null && player.isPlaying()) {
            long currentPosition = player.getCurrentPosition() / 1000; // Convert to seconds
            long duration = player.getDuration() / 1000; // Convert to seconds
            
            // Update tracker with current position
            contentTracker.updatePosition(userId, contentId, (int)currentPosition, (int)duration);
            
            // Check if near the end of video for auto-completion
            // The ContentTracker checks it already, but we can also check here
            if (!isVideoComplete && duration > 0 && duration - currentPosition <= 10) {
                markVideoComplete();
            }
        }
    }
    
    /**
     * Mark video as completed
     */
    private void markVideoComplete() {
        if (!isVideoComplete && !wasUserCompleting) {
            wasUserCompleting = true;
            
            // Get current position and duration
            long currentPosition = player.getCurrentPosition() / 1000;
            long duration = player.getDuration() / 1000;
            
            // Mark as completed in tracker
            contentTracker.markAsCompleted(userId, contentId);
            
            // Show completion toast
            Toast.makeText(this, "Video completed!", Toast.LENGTH_SHORT).show();
            
            // Update state
            isVideoComplete = true;
            
            // In a real app, you might navigate to the next lesson
            // or show a completion dialog with options
            
            wasUserCompleting = false;
        }
    }
    
    /**
     * Toggle fullscreen mode
     */
    private void toggleFullscreen() {
        // In a real implementation, this would toggle fullscreen
        // This is just a placeholder
        Toast.makeText(this, "Fullscreen toggled", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            // When pausing, update position and pause playback
            updateVideoPosition();
            player.setPlayWhenReady(false);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }
    
    @Override
    protected void onDestroy() {
        // Clean up
        if (handler != null) {
            handler.removeCallbacks(positionUpdateRunnable);
        }
        
        if (player != null) {
            updateVideoPosition(); // Final position update
            contentTracker.stopTracking(userId, contentId);
            player.release();
            player = null;
        }
        
        super.onDestroy();
    }
}