package com.fast.mentor;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.fast.mentor.DatabaseHelper;
import com.fast.mentor.Content;
import com.fast.mentor.ContentProgress;
import com.fast.mentor.UserMetrics;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Tracks user progress through various learning content
 * Handles automatic completion detection and time tracking
 */
public class ContentTracker {
    private static final String TAG = "ContentTracker";
    private static final int UPDATE_INTERVAL_MS = 30000; // Update progress every 30 seconds
    private static final int VIDEO_COMPLETION_THRESHOLD = 10; // seconds from the end
    private static final int PDF_MIN_TIME_FOR_COMPLETION = 30; // seconds

    private static ContentTracker instance;
    private final DatabaseHelper dbHelper;
    private final Handler handler;
    
    // Track currently active content
    private final Map<Integer, TrackingSession> activeSessions;
    
    private ContentTracker(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context.getApplicationContext());
        this.handler = new Handler(Looper.getMainLooper());
        this.activeSessions = new HashMap<>();
    }
    
    public static synchronized ContentTracker getInstance(Context context) {
        if (instance == null && context != null) {
            instance = new ContentTracker(context);
        }
        return instance;
    }
    
    /**
     * Start tracking a content item when a user begins viewing it
     * @param userId User ID
     * @param contentId Content ID
     * @param contentType Type of content ("video", "pdf", etc.)
     */
    public void startTracking(int userId, int contentId, String contentType) {
        Log.d(TAG, "Starting tracking for content ID: " + contentId);
        
        // Check if we already have progress for this content
        ContentProgress progress = dbHelper.getContentProgress(userId, contentId);
        
        if (progress == null) {
            // Create new progress entry
            progress = new ContentProgress();
            progress.setUserId(userId);
            progress.setContentId(contentId);
            progress.setStartTimestamp(new Date());
            progress.setLastAccessTimestamp(new Date());
            progress.setContentType(contentType);
            progress.setTimeSpentSeconds(0);
            progress.setLastPosition(0);
            progress.setCompleted(false);
            
            // Save to database
            long id = dbHelper.saveContentProgress(progress);
            progress.setId((int) id);
            
            Log.d(TAG, "Created new progress record: " + id);
        } else {
            // Update last access time
            progress.setLastAccessTimestamp(new Date());
            dbHelper.updateContentProgress(progress);
            
            Log.d(TAG, "Updated existing progress record: " + progress.getId());
        }
        
        // Start tracking session
        TrackingSession session = new TrackingSession(userId, contentId, progress.getTimeSpentSeconds());
        activeSessions.put(contentId, session);
        
        // Schedule regular updates
        schedulePeriodicUpdate(contentId);
    }
    
    /**
     * Update the current position in the content (video time or PDF page)
     * @param contentId Content ID
     * @param position Current position in content (seconds for video, page number for PDF)
     * @param contentDuration Total content duration/length (seconds for video, pages for PDF)
     */
    public void updatePosition(int userId, int contentId, int position, int contentDuration) {
        Log.d(TAG, "Updating position for content " + contentId + " to " + position);
        
        TrackingSession session = activeSessions.get(contentId);
        if (session == null) {
            // Session doesn't exist, create a new one
            startTracking(userId, contentId, dbHelper.getContent(contentId).getContentType());
            session = activeSessions.get(contentId);
        }
        
        session.setCurrentPosition(position);
        session.setContentDuration(contentDuration);
        
        ContentProgress progress = dbHelper.getContentProgress(userId, contentId);
        if (progress != null) {
            progress.setLastPosition(position);
            dbHelper.updateContentProgress(progress);
            
            // Check for auto-completion
            Content content = dbHelper.getContent(contentId);
            if (content != null && !progress.isCompleted()) {
                checkForAutoCompletion(userId, contentId, position, contentDuration, content.getContentType(), progress);
            }
        }
    }
    
    /**
     * Stop tracking content consumption
     * @param contentId ID of content to stop tracking
     */
    public void stopTracking(int userId, int contentId) {
        TrackingSession session = activeSessions.remove(contentId);
        if (session != null) {
            // Calculate final time spent
            long timeSpentSeconds = session.getElapsedSeconds();
            
            // Update the content progress
            ContentProgress progress = dbHelper.getContentProgress(userId, contentId);
            if (progress != null) {
                progress.setTimeSpentSeconds(progress.getTimeSpentSeconds() + (int)timeSpentSeconds);
                progress.setLastAccessTimestamp(new Date());
                dbHelper.updateContentProgress(progress);
                
                Log.d(TAG, "Stopped tracking content " + contentId + 
                      ". Total time spent: " + progress.getTimeSpentSeconds() + " seconds");
                
                // Check for auto-completion one last time
                Content content = dbHelper.getContent(contentId);
                if (content != null && !progress.isCompleted()) {
                    checkForAutoCompletion(userId, contentId, session.getCurrentPosition(), 
                                         session.getContentDuration(), 
                                         content.getContentType(), progress);
                }
            }
            
            // Cancel scheduled updates
            handler.removeCallbacksAndMessages(contentId);
        }
    }
    
    /**
     * Manually mark content as completed
     * @param userId User ID
     * @param contentId Content ID
     */
    public void markAsCompleted(int userId, int contentId) {
        Log.d(TAG, "Manually marking content " + contentId + " as completed");
        ContentProgress progress = dbHelper.getContentProgress(userId, contentId);
        Content content = dbHelper.getContent(contentId);
        
        if (progress != null && content != null) {
            // Stop tracking if it's active
            if (activeSessions.containsKey(contentId)) {
                stopTracking(userId, contentId);
                progress = dbHelper.getContentProgress(userId, contentId);
            }
            
            progress.setCompleted(true);
            progress.setCompletionTimestamp(new Date());
            dbHelper.updateContentProgress(progress);
            
            // Update user metrics and course progress
            updateMetricsAndProgress(userId, contentId, content, progress);
        }
    }
    
    /**
     * Check if content should be automatically marked as complete
     */
    private void checkForAutoCompletion(int userId, int contentId, int position, 
                                      int contentDuration, String contentType, 
                                      ContentProgress progress) {
        boolean shouldComplete = false;
        
        if ("video".equals(contentType)) {
            // Auto-complete video if within X seconds of the end
            if (contentDuration > 0 && contentDuration - position <= VIDEO_COMPLETION_THRESHOLD) {
                shouldComplete = true;
                Log.d(TAG, "Auto-completing video: position " + position + 
                      " is near end " + contentDuration);
            }
        } else if ("pdf".equals(contentType)) {
            // For PDFs, we consider it complete if the user has reached the last page
            // and spent at least a minimum amount of time
            if (position >= contentDuration && 
                progress.getTimeSpentSeconds() >= PDF_MIN_TIME_FOR_COMPLETION) {
                shouldComplete = true;
                Log.d(TAG, "Auto-completing PDF: reached last page " + position + 
                      " of " + contentDuration);
            }
        }
        
        if (shouldComplete) {
            progress.setCompleted(true);
            progress.setCompletionTimestamp(new Date());
            dbHelper.updateContentProgress(progress);
            
            // Update user metrics and course progress
            Content content = dbHelper.getContent(contentId);
            if (content != null) {
                updateMetricsAndProgress(userId, contentId, content, progress);
            }
        }
    }
    
    /**
     * Update user metrics and overall course progress when content is completed
     */
    private void updateMetricsAndProgress(int userId, int contentId, 
                                         Content content, ContentProgress progress) {
        // Calculate completion rate (actual time vs. expected time)
        int completionRate = 100;  // Default is 100%
        if (content.getExpectedTimeSeconds() > 0) {
            completionRate = (progress.getTimeSpentSeconds() * 100) / 
                            content.getExpectedTimeSeconds();
        }
        
        // Update user metrics
        UserMetrics metrics = dbHelper.getUserMetrics(userId);
        if (metrics == null) {
            metrics = new UserMetrics();
            metrics.setUserId(userId);
            metrics.updateMetrics(completionRate, content.getContentType(), content.getDifficulty());
            dbHelper.saveUserMetrics(metrics);
        } else {
            metrics.updateMetrics(completionRate, content.getContentType(), content.getDifficulty());
            dbHelper.updateUserMetrics(metrics);
        }
        
        // Update module completion status
        dbHelper.updateModuleProgress(userId, content.getModuleId());
        
        // Update course completion status
        dbHelper.updateCourseProgress(userId, content.getCourseId());
        
        // Generate new recommendations based on updated metrics
        RecommendationEngine.getInstance(null).generateRecommendations(userId);
        
        Log.d(TAG, "Updated metrics and progress for user " + userId + 
              ", completion rate: " + completionRate + "%");
    }
    
    /**
     * Schedule periodic updates to save progress data
     */
    private void schedulePeriodicUpdate(final Integer contentId) {
        final Runnable updateTask = new Runnable() {
            @Override
            public void run() {
                TrackingSession session = activeSessions.get(contentId);
                if (session != null) {
                    // Update progress in database
                    ContentProgress progress = dbHelper.getContentProgress(
                            session.getUserId(), contentId);
                    
                    if (progress != null) {
                        long timeSpent = session.getElapsedSeconds();
                        progress.setTimeSpentSeconds(progress.getTimeSpentSeconds() + (int)timeSpent);
                        progress.setLastAccessTimestamp(new Date());
                        progress.setLastPosition(session.getCurrentPosition());
                        dbHelper.updateContentProgress(progress);
                        
                        // Reset timer in session
                        session.resetTimer();
                        
                        Log.d(TAG, "Periodic update for content " + contentId + 
                              ", total time: " + progress.getTimeSpentSeconds());
                    }
                    
                    // Schedule next update
                    handler.postDelayed(this, UPDATE_INTERVAL_MS);
                }
            }
        };
        
        handler.postDelayed(updateTask, UPDATE_INTERVAL_MS);
    }
    
    /**
     * Helper class to track an active content session
     */
    private static class TrackingSession {
        private final int userId;
        private final int contentId;
        private int currentPosition;
        private int contentDuration;
        private long startTimeMs;
        private long totalTimeMs;
        
        TrackingSession(int userId, int contentId, int previousTimeSpent) {
            this.userId = userId;
            this.contentId = contentId;
            this.currentPosition = 0;
            this.contentDuration = 0;
            this.startTimeMs = System.currentTimeMillis();
            this.totalTimeMs = TimeUnit.SECONDS.toMillis(previousTimeSpent);
        }
        
        public int getUserId() {
            return userId;
        }
        
        public int getCurrentPosition() {
            return currentPosition;
        }
        
        public void setCurrentPosition(int position) {
            this.currentPosition = position;
        }
        
        public int getContentDuration() {
            return contentDuration;
        }
        
        public void setContentDuration(int contentDuration) {
            this.contentDuration = contentDuration;
        }
        
        public long getElapsedSeconds() {
            long currentTimeMs = System.currentTimeMillis();
            long sessionTimeMs = currentTimeMs - startTimeMs;
            return TimeUnit.MILLISECONDS.toSeconds(sessionTimeMs);
        }
        
        public void resetTimer() {
            totalTimeMs += System.currentTimeMillis() - startTimeMs;
            startTimeMs = System.currentTimeMillis();
        }
    }
}