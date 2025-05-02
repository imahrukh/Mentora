package com.fast.mentor;

import java.util.List;

public class Course {
    private String title;
    private String provider;
    private String moduleTitle;
    private String moduleProgress;

    private String description;
    private boolean online;
    private String level;
    private String duration;
    private List<String> skills;

    public Course(String title, String provider, String moduleTitle, String moduleProgress, String description, boolean online, String level, String duration, List<String> skills) {
        this.title = title;
        this.provider = provider;
        this.moduleTitle = moduleTitle;
        this.moduleProgress = moduleProgress;
        this.description = description;
        this.online = online;
        this.level = level;
        this.duration = duration;
        this.skills = skills;
    }

    public String getTitle() { return title; }
    public String getProvider() { return provider; }
    public String getModuleTitle() { return moduleTitle; }
    public String getModuleProgress() { return moduleProgress; }
    public String getDescription() { return description; }
    public boolean isOnline() { return online; }
    public String getLevel() { return level; }
    public String getDuration() { return duration; }
    public List<String> getSkills() { return skills; }
}
