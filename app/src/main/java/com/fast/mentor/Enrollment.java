package com.fast.mentor;

import java.util.HashMap;
import java.util.Map;

public class Enrollment {
    private String enrollmentId;
    private String userId;
    private String courseId;
    private String enrolledAt;
    private String lastAccessedAt;
    private int progressPercentage;
    private boolean isCompleted;
    private Map<String, Boolean> completedItems; // Map of itemId to completion status

    // Empty constructor required for Firestore
    public Enrollment() {
    }
    private int progress; // 0-100
    private boolean completed;

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }


    public Enrollment(String enrollmentId, String userId, String courseId) {
        this.enrollmentId = enrollmentId;
        this.userId = userId;
        this.courseId = courseId;
        this.enrolledAt = String.valueOf(System.currentTimeMillis());
        this.lastAccessedAt = String.valueOf(System.currentTimeMillis());
        this.progressPercentage = 0;
        this.isCompleted = false;
        this.completedItems = new HashMap<>();
    }

    // Getters and Setters
    public String getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(String enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(String enrolledAt) {
        this.enrolledAt = enrolledAt;
    }

    public String getLastAccessedAt() {
        return lastAccessedAt;
    }

    public void setLastAccessedAt(String lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }

    public int getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(int progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public Map<String, Boolean> getCompletedItems() {
        return completedItems;
    }

    public void setCompletedItems(Map<String, Boolean> completedItems) {
        this.completedItems = completedItems;
    }

    public void markItemCompleted(String itemId) {
        if (completedItems == null) {
            completedItems = new HashMap<>();
        }
        completedItems.put(itemId, true);
    }

    public boolean isItemCompleted(String itemId) {
        if (completedItems == null) {
            return false;
        }
        Boolean completed = completedItems.get(itemId);
        return completed != null && completed;
    }

    public void updateLastAccessed() {
        this.lastAccessedAt = String.valueOf(System.currentTimeMillis());
    }

    public void setId(String id) {
        this.enrollmentId = id;

    }
    public String getId() {
        return enrollmentId;
    }

    public void markAsCompleted() {
        this.isCompleted = true;

    }
}