package com.fast.mentor;

public class ModuleItem {
    private String itemId;
    private String moduleId;
    private String title;
    private String description;
    private String type; // "video", "pdf", "quiz", "assignment", "lab"
    private String contentUrl;
    private int durationInMinutes;
    private int order;
    private int gradeWeight; // Percentage weight (if gradable)
    private boolean isGradable;

    // Empty constructor required for Firestore
    public ModuleItem() {
    }

    public ModuleItem(String itemId, String moduleId, String title, String type, String contentUrl, int order) {
        this.itemId = itemId;
        this.moduleId = moduleId;
        this.title = title;
        this.type = type;
        this.contentUrl = contentUrl;
        this.order = order;
        this.isGradable = false;
    }

    // Getters and Setters
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
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

    public int getDurationInMinutes() {
        return durationInMinutes;
    }

    public void setDurationInMinutes(int durationInMinutes) {
        this.durationInMinutes = durationInMinutes;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getGradeWeight() {
        return gradeWeight;
    }

    public void setGradeWeight(int gradeWeight) {
        this.gradeWeight = gradeWeight;
    }

    public boolean isGradable() {
        return isGradable;
    }

    public void setGradable(boolean gradable) {
        isGradable = gradable;
    }
}