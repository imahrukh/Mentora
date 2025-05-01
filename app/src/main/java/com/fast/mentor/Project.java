package com.fast.mentor;

public class Project {
    private int imageResId;
    private String title;
    private String provider;
    private String guidedProjectTitle;

    public Project(int imageResId, String title, String provider, String guidedProjectTitle) {
        this.imageResId = imageResId;
        this.title = title;
        this.provider = provider;
        this.guidedProjectTitle = guidedProjectTitle;
    }

    public int getImageResId() { return imageResId; }
    public String getTitle() { return title; }
    public String getProvider() { return provider; }
    public String getGuidedProjectTitle() { return guidedProjectTitle; }
}

