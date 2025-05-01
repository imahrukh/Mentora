package com.fast.mentor;

public class Course {
    private String title;
    private String provider;
    private String moduleTitle;
    private String moduleProgress;

    public Course(String title, String provider, String moduleTitle, String moduleProgress) {
        this.title = title;
        this.provider = provider;
        this.moduleTitle = moduleTitle;
        this.moduleProgress = moduleProgress;
    }

    public String getTitle() { return title; }
    public String getProvider() { return provider; }
    public String getModuleTitle() { return moduleTitle; }
    public String getModuleProgress() { return moduleProgress; }
}
