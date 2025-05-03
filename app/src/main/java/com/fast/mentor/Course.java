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
    private List<Week> weeks;
    private List<String> prerequisites;
    private String difficultyLevel;
    private List<String> relatedCourses;
     public Course() {
         this.title = "";
         this.provider = "";
         this.moduleTitle = "";
         this.moduleProgress = "";
         this.description = "";
         this.online = false;
         this.level = "";
         this.duration = "";
         this.skills = null;
         this.weeks = null;
         this.prerequisites = null;
         this.difficultyLevel = "";
         this.relatedCourses = null;
     }
    public Course(String title, String provider, String moduleTitle, String moduleProgress, String description, boolean online, String level, String duration, List<String> skills, List<Week> weeks, List<String> prerequisites, String difficultyLevel, List<String> relatedCourses) {
        this.title = title;
        this.provider = provider;
        this.moduleTitle = moduleTitle;
        this.moduleProgress = moduleProgress;
        this.description = description;
        this.online = online;
        this.level = level;
        this.duration = duration;
        this.skills = skills;
        this.weeks = weeks;
        this.prerequisites = prerequisites;
        this.difficultyLevel = difficultyLevel;
        this.relatedCourses = relatedCourses;
    }
    public List<String> getPrerequisites() { return prerequisites; }
    public String getDifficultyLevel() { return difficultyLevel; }
    public List<String> getRelatedCourses() { return relatedCourses; }
    public void setPrerequisites(List<String> prerequisites) { this.prerequisites = prerequisites; }
    public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    public void setRelatedCourses(List<String> relatedCourses) { this.relatedCourses = relatedCourses; }
    public void setTitle(String title) { this.title = title; }
    public void setProvider(String provider) { this.provider = provider; }
    public void setModuleTitle(String moduleTitle) { this.moduleTitle = moduleTitle; }
    public void setModuleProgress(String moduleProgress) { this.moduleProgress = moduleProgress; }
    public void setDescription(String description) { this.description = description; }
    public void setOnline(boolean online) { this.online = online; }
    public void setLevel(String level) { this.level = level; }
    public void setDuration(String duration) { this.duration = duration; }
    public void setSkills(List<String> skills) { this.skills = skills; }
    public void setWeeks(List<Week> weeks) { this.weeks = weeks; }

    public String getTitle() { return title; }
    public String getProvider() { return provider; }
    public String getModuleTitle() { return moduleTitle; }
    public String getModuleProgress() { return moduleProgress; }
    public String getDescription() { return description; }
    public boolean isOnline() { return online; }
    public String getLevel() { return level; }
    public String getDuration() { return duration; }
    public List<String> getSkills() { return skills; }
    public List<Week> getWeeks() { return weeks; }

    public Module[] getModules() {
         return null;
    }
}
