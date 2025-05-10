package com.fast.mentor;

import java.io.Serializable;

/**
 * Class representing a content item in a course
 * This could be a video lecture, reading material, quiz, or assignment
 */
public class ContentItem implements Serializable {
    
    private String id;
    private String moduleId;
    private String title;
    private String description;
    private String type; // "video", "reading", "quiz", "assignment"
    private String contentUrl;
    private int duration; // duration in minutes (for video) or estimated completion time
    private int orderIndex;
    private boolean isCompleted;
    private boolean isOptional;
    
    // Empty constructor for Firebase
    public ContentItem() {
    }
    
    // Constructor with all fields
    public ContentItem(String id, String moduleId, String title, String description, String type,
                       String contentUrl, int duration, int orderIndex, boolean isCompleted, boolean isOptional) {
        this.id = id;
        this.moduleId = moduleId;
        this.title = title;
        this.description = description;
        this.type = type;
        this.contentUrl = contentUrl;
        this.duration = duration;
        this.orderIndex = orderIndex;
        this.isCompleted = isCompleted;
        this.isOptional = isOptional;
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getModuleId() {
        return moduleId;
    }
    
    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
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
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getContentUrl() {
        return contentUrl;
    }
    
    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    public int getOrderIndex() {
        return orderIndex;
    }
    
    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
    
    public boolean isCompleted() {
        return isCompleted;
    }
    
    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
    
    public boolean isOptional() {
        return isOptional;
    }
    
    public void setOptional(boolean optional) {
        isOptional = optional;
    }
    
    /**
     * Get the appropriate icon resource ID for this content type
     */
    public int getTypeIconResource() {
        // This would return the appropriate drawable resource ID based on content type
        // These resource IDs would need to be defined in the drawable resources
        switch (type) {
            case "video":
                return android.R.drawable.ic_media_play;
            case "reading":
                return android.R.drawable.ic_menu_edit;
            case "quiz":
                return android.R.drawable.ic_menu_help;
            case "assignment":
                return android.R.drawable.ic_menu_upload;
            default:
                return android.R.drawable.ic_menu_info_details;
        }
    }
    
    /**
     * Get formatted duration string (e.g., "10 min")
     */
    public String getFormattedDuration() {
        if (duration < 60) {
            return duration + " min";
        } else {
            int hours = duration / 60;
            int mins = duration % 60;
            if (mins == 0) {
                return hours + " h";
            } else {
                return hours + " h " + mins + " min";
            }
        }
    }
}