package com.fast.mentor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Represents a user's earned certificate.
 */
public class Certificate {
    private String certificateId;
    private String userId;
    private String courseId;
    private String issuedAt;
    private String certificateUrl;
    private boolean shared;  // new field

    // Transient Course object for UI binding
    private transient Course course;

    public Certificate() {}

    public Certificate(String certificateId, String userId, String courseId) {
        this.certificateId = certificateId;
        this.userId        = userId;
        this.courseId      = courseId;
        this.issuedAt      = String.valueOf(System.currentTimeMillis());
        this.shared        = false;
    }

    // Getters and setters
    public String getCertificateId() { return certificateId; }
    public void setCertificateId(String certificateId) { this.certificateId = certificateId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public String getIssuedAt() { return issuedAt; }
    public void setIssuedAt(String issuedAt) { this.issuedAt = issuedAt; }
    public String getCertificateUrl() { return certificateUrl; }
    public void setCertificateUrl(String certificateUrl) { this.certificateUrl = certificateUrl; }
    public boolean isShared() { return shared; }
    public void setShared(boolean shared) { this.shared = shared; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public String getFormattedDate() {
        try {
            long ts = Long.parseLong(issuedAt);
            Date d = new Date(ts);
            return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(d);
        } catch (NumberFormatException e) {
            return issuedAt;
        }
    }
}
