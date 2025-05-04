package com.fast.mentor;

public class ProgressUpdateEvent {
    // Can add fields if needed
    private String courseId;
    private int progressPercentage;

    public ProgressUpdateEvent() {}  // Default constructor

    // Optional: Add constructor with parameters
    public ProgressUpdateEvent(String courseId, int progressPercentage) {
        this.courseId = courseId;
        this.progressPercentage = progressPercentage;
    }

    // Getters if using fields
    public String getCourseId() { return courseId; }
    public int getProgressPercentage() { return progressPercentage; }
}
