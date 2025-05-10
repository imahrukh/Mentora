package com.fast.mentor;

import androidx.annotation.Keep;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Model class for assignment submissions.
 */
@Keep
@IgnoreExtraProperties
public class AssignmentSubmission {
    
    private String id;
    private String userId;
    private String assignmentId;
    private String comment;
    private List<SubmissionFile> files = new ArrayList<>();
    private boolean isGraded = false;
    private int grade = 0;
    private String feedback;
    private @ServerTimestamp Date submittedAt;
    private Date gradedAt;

    /**
     * Default constructor required for Firestore
     */
    public AssignmentSubmission() {
        // Required empty constructor
    }

    /**
     * Constructor with basic fields
     */
    public AssignmentSubmission(String id, String userId, String assignmentId) {
        this.id = id;
        this.userId = userId;
        this.assignmentId = assignmentId;
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<SubmissionFile> getFiles() {
        return files;
    }

    public void setFiles(List<SubmissionFile> files) {
        this.files = files != null ? files : new ArrayList<>();
    }

    public boolean getGraded() {
        return isGraded;
    }

    public void setGraded(boolean graded) {
        isGraded = graded;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public Date getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Date submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Date getGradedAt() {
        return gradedAt;
    }

    public void setGradedAt(Date gradedAt) {
        this.gradedAt = gradedAt;
    }
}