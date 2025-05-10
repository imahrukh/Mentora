package com.fast.mentor;

import java.util.Date;

/**
 * Tracks user progress for a specific content item
 */
public class ContentProgress {
    private int id;
    private int userId;
    private int contentId;
    private boolean completed;
    private Date startTimestamp;
    private Date completionTimestamp;
    private Date lastAccessTimestamp;
    private int timeSpentSeconds; // total time spent in seconds
    private int lastPosition; // last position (seconds for video, page for PDF)
    private String contentType;  // "video", "pdf", "quiz", "assignment"
    private int attempts; // for quizzes/assignments

    public ContentProgress() {
        // Default constructor
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getContentId() {
        return contentId;
    }

    public void setContentId(int contentId) {
        this.contentId = contentId;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Date getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(Date startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public Date getCompletionTimestamp() {
        return completionTimestamp;
    }

    public void setCompletionTimestamp(Date completionTimestamp) {
        this.completionTimestamp = completionTimestamp;
    }

    public Date getLastAccessTimestamp() {
        return lastAccessTimestamp;
    }

    public void setLastAccessTimestamp(Date lastAccessTimestamp) {
        this.lastAccessTimestamp = lastAccessTimestamp;
    }

    public int getTimeSpentSeconds() {
        return timeSpentSeconds;
    }

    public void setTimeSpentSeconds(int timeSpentSeconds) {
        this.timeSpentSeconds = timeSpentSeconds;
    }

    public int getLastPosition() {
        return lastPosition;
    }

    public void setLastPosition(int lastPosition) {
        this.lastPosition = lastPosition;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }
    
    /**
     * Calculate completion rate as a percentage of expected time
     * @param expectedTimeSeconds expected time to complete in seconds
     * @return completion rate as a percentage
     */
    public int calculateCompletionRate(int expectedTimeSeconds) {
        if (expectedTimeSeconds <= 0) {
            return 100; // avoid division by zero
        }
        return (timeSpentSeconds * 100) / expectedTimeSeconds;
    }
}