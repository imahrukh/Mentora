package com.fast.mentor;

public class ProgressUpdateEvent {
    // Can add fields if needed
    private String courseId;
    private String moduleId;
    private int progressPercentage;

    public ProgressUpdateEvent() {}  // Default constructor


    public ProgressUpdateEvent(String courseId, String moduleId, int progressPercentage) {
        this.courseId = courseId;
        this.moduleId = moduleId;
        this.progressPercentage = progressPercentage;
    }

    // Getters if using fields
    public String getCourseId() { return courseId; }
    public int getProgressPercentage() { return progressPercentage; }
    public String getModuleId() { return moduleId; }
}
