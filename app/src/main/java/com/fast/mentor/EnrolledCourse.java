package com.fast.mentor;

import java.io.Serializable;

/**
 * Model class representing an enrolled course with progress information
 */
public class EnrolledCourse implements Serializable {
    
    private String courseId;
    private String title;
    private String instructorName;
    private String thumbnailUrl;
    private int durationHours;
    private float progress;  // 0-100
    private float remainingHours;
    
    // Empty constructor
    public EnrolledCourse() {}

    // Getters and setters
    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public int getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(int durationHours) {
        this.durationHours = durationHours;
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    public float getRemainingHours() {
        return remainingHours;
    }

    public void setRemainingHours(float remainingHours) {
        this.remainingHours = remainingHours;
    }
    
    // Helper methods
    public String getFormattedProgress() {
        return Math.round(progress) + "% Complete";
    }
    
    public String getFormattedTimeRemaining() {
        if (remainingHours < 0.1f) {
            return "Almost done!";
        } else if (remainingHours < 1) {
            return "< 1 hr left";
        } else {
            return String.format("%.1f hrs left", remainingHours);
        }
    }
}