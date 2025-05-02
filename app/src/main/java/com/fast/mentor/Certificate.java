package com.fast.mentor;

public class Certificate {
    private int imageResId;
    private String courseName;
    private String provider;

    public Certificate(int imageResId, String courseName, String provider) {
        this.imageResId = imageResId;
        this.courseName = courseName;
        this.provider = provider;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getProvider() {
        return provider;
    }
}

