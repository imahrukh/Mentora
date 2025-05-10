package com.fast.mentor;

/**
 * Class representing a module in a course
 */
public class CourseModule {
    
    private String id;
    private String courseId;
    private String title;
    private String description;
    private int orderIndex;
    private int durationMinutes;
    private int lessonsCount;
    private int progress; // Progress percentage (0-100)
    
    // Empty constructor for Firebase
    public CourseModule() {
    }
    
    // Constructor with all fields
    public CourseModule(String id, String courseId, String title, String description,
                       int orderIndex, int durationMinutes, int lessonsCount, int progress) {
        this.id = id;
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.orderIndex = orderIndex;
        this.durationMinutes = durationMinutes;
        this.lessonsCount = lessonsCount;
        this.progress = progress;
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getOrderIndex() {
        return orderIndex;
    }
    
    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
    
    public int getDurationMinutes() {
        return durationMinutes;
    }
    
    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
    
    public int getLessonsCount() {
        return lessonsCount;
    }
    
    public void setLessonsCount(int lessonsCount) {
        this.lessonsCount = lessonsCount;
    }
    
    public int getProgress() {
        return progress;
    }
    
    public void setProgress(int progress) {
        this.progress = progress;
    }
    
    /**
     * Get formatted duration string (e.g., "10 min")
     */
    public String getFormattedDuration() {
        if (durationMinutes < 60) {
            return durationMinutes + " min";
        } else {
            int hours = durationMinutes / 60;
            int mins = durationMinutes % 60;
            if (mins == 0) {
                return hours + " h";
            } else {
                return hours + " h " + mins + " min";
            }
        }
    }
}