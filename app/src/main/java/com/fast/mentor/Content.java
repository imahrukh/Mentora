package com.fast.mentor;

/**
 * Represents a content item (video, PDF, quiz, etc.) in a course
 */
public class Content {
    private int id;
    private int moduleId;
    private int courseId;
    private String title;
    private String description;
    private String contentUrl;
    private String contentType;  // "video", "pdf", "quiz", "assignment"
    private int duration;  // in seconds for video, page count for PDF
    private int difficulty;  // 1-5 scale
    private int expectedTimeSeconds;  // expected time to complete in seconds
    private int displayOrder;  // order in module

    public Content() {
        // Default constructor
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
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

    public String getContentUrl() {
        return contentUrl;
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public int getExpectedTimeSeconds() {
        return expectedTimeSeconds;
    }

    public void setExpectedTimeSeconds(int expectedTimeSeconds) {
        this.expectedTimeSeconds = expectedTimeSeconds;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}