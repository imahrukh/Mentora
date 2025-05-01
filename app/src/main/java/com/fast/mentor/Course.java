package com.fast.mentor;

public class Course {
    private int imageResId;
    private String title;
    private String provider;

    public Course(int imageResId, String title, String provider) {
        this.imageResId = imageResId;
        this.title = title;
        this.provider = provider;
    }

    public int getImageResId() { return imageResId; }
    public String getTitle() { return title; }
    public String getProvider() { return provider; }
}
