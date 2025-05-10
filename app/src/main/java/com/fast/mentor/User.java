package com.fast.mentor;

public class User {
    private String userId;
    private String fullName;
    private String email;
    private String photoUrl;
    private String createdAt;
    private String lastLoginAt;

    // Empty constructor required for Firestore
    public User() {
    }

    public User(String userId, String fullName, String email) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.createdAt = String.valueOf(System.currentTimeMillis());
        this.lastLoginAt = String.valueOf(System.currentTimeMillis());
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(String lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public void updateLastLogin() {
        this.lastLoginAt = String.valueOf(System.currentTimeMillis());
    }
}