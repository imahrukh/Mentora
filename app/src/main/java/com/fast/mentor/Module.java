package com.fast.mentor;

import java.util.List;

public class Module {
    private String moduleId;
    private String weekId;
    private String title;
    private String description;
    private int order;
    private List<ContentItem> items;
    private List<Lesson>item;
    public List<ContentItem> getItems() {
        return items;
    }

    // UI-related state
    private boolean expanded = false;  // For expand/collapse state
    private float progressPercentage = 0f; // For module progress (0 - 100)

    // Empty constructor required for Firestore
    public Module() {
    }

    public Module(String moduleId, String weekId, String title, String description, int order) {
        this.moduleId = moduleId;
        this.weekId = weekId;
        this.title = title;
        this.description = description;
        this.order = order;
    }

    // --- Getters and Setters ---

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getWeekId() {
        return weekId;
    }

    public void setWeekId(String weekId) {
        this.weekId = weekId;
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

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }


    public void setItems(List<Lesson> item) {
        this.item = item;
    }

    // --- Progress Methods ---

    public float getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(float progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public String getFormattedProgress() {
        return String.format("%.0f%% Complete", progressPercentage);
    }

    // --- Expand/Collapse Methods ---

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public void toggleExpanded() {
        this.expanded = !this.expanded;
    }
}
