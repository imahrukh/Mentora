package com.fast.mentor;

import java.util.List;

public class Week {
    private String weekId;
    private int weekNumber;
    private String title;
    private String description;
    private List<Module> modules;

    // Empty constructor required for Firestore
    public Week() {
    }

    public Week(String weekId, int weekNumber, String title, String description) {
        this.weekId = weekId;
        this.weekNumber = weekNumber;
        this.title = title;
        this.description = description;
    }

    // Getters and Setters
    public String getWeekId() {
        return weekId;
    }

    public void setWeekId(String weekId) {
        this.weekId = weekId;
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(int weekNumber) {
        this.weekNumber = weekNumber;
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

    public List<Module> getModules() {
        return modules;
    }

    public void setModules(List<Module> modules) {
        this.modules = modules;
    }
}