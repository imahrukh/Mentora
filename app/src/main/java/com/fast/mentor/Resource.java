package com.fast.mentor;

/**
 * Represents an additional resource attached to a lesson.
 */
public class Resource {
    private String id;
    private String lessonId;
    private String title;
    private String description;
    private String url;
    private String type; // e.g., "pdf", "image", "link"
    private long createdAt;
    
    /**
     * Default constructor for Firebase
     */
    public Resource() {
        // Required for Firebase
    }
    
    /**
     * Full constructor
     */
    public Resource(String id, String lessonId, String title, String description, 
                   String url, String type, long createdAt) {
        this.id = id;
        this.lessonId = lessonId;
        this.title = title;
        this.description = description;
        this.url = url;
        this.type = type;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getLessonId() {
        return lessonId;
    }
    
    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
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
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}