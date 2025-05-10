package com.fast.mentor;

import java.util.List;

public class Module {
    private String moduleId;
    private String weekId;
    private String title;
    private String description;
    private int order;
    private List<ModuleItem> items;

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

    // Getters and Setters
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

    public List<ModuleItem> getItems() {
        return items;
    }

    public void setItems(List<ModuleItem> items) {
        this.items = items;
    }
}