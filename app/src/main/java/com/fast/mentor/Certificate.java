package com.fast.mentor;

public class Certificate {
    private String certificateId;
    private String userId;
    private String courseId;
    private String issuedAt;
    private String certificateUrl;

    // Empty constructor required for Firestore
    public Certificate() {
    }

    public Certificate(String certificateId, String userId, String courseId) {
        this.certificateId = certificateId;
        this.userId = userId;
        this.courseId = courseId;
        this.issuedAt = String.valueOf(System.currentTimeMillis());
    }

    // Getters and Setters
    public String getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(String certificateId) {
        this.certificateId = certificateId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(String issuedAt) {
        this.issuedAt = issuedAt;
    }

    public String getCertificateUrl() {
        return certificateUrl;
    }

    public void setCertificateUrl(String certificateUrl) {
        this.certificateUrl = certificateUrl;
    }
}